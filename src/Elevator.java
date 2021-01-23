import javax.swing.*;
import java.awt.*;

public class Elevator implements Runnable{
    JPanel panel;
    JLabel elevatorstate;
    JLabel[] floors;
    JButton[] floorButtons;
    static final Font font = new Font("font",1,16);
    int state;
    int onfloor;
    int destination;
    boolean[] to;
    boolean[] outsideRequest;
    int velocity = 50;

    ImageIcon openIcon;
    ImageIcon closeIcon;
    ImageIcon openingIcon;
    ImageIcon closingIcon;
    JLabel door;
    JButton close;
    JButton open;
    int doorstate;
    long latency = 1000;
    long keepOpenTime = 2000;
    long closeopenTime = 2000;

    ButtonListener l;

    Elevator() {
        l = new ButtonListener();
        panel = new JPanel();
        floors = new JLabel[6];
        elevatorstate = new JLabel("1", JLabel.CENTER);
        floorButtons = new JButton[6];
        to = new boolean[6];
        for(int i = 0; i < 6; i++)
            to[i] = false;
        outsideRequest = new boolean[6];
        for(int i = 0; i < 6; i++)
            outsideRequest[i] = false;
        state = 0;
        onfloor = 1;
        destination = 1;

        String basePath = this.getClass().getResource("").getPath();
        closeIcon = new ImageIcon(basePath + "img/closed.jpg");
        openIcon = new ImageIcon(basePath + "img/opened.jpg");
        openingIcon = new ImageIcon(basePath + "img/opening.jpg");
        closingIcon = new ImageIcon(basePath + "img/closing.jpg");
        door = new JLabel(closeIcon);
        close = new JButton("> <");
        open = new JButton("< >");
        panel = new JPanel();
        doorstate = -2;

        layout();
    }

    public void run() {
        try {
            while(true) {
                traverseTo();
                if(checkTo()) {
                    findNearest();
                    if(state == 1)
                        elevatorstate.setText("▲ " + String.valueOf(onfloor));
                    else
                        elevatorstate.setText("▼ " + String.valueOf(onfloor));
                    move();
                }
                else {
                    state = 0;
                    elevatorstate.setText(String.valueOf(onfloor));
                    doorClosedAtState0();
                }
                Thread.yield();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void layout() {
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);


        for(int i = 0; i < floors.length; i++) {
            floors[i] = new JLabel(String.valueOf(i+1),JLabel.CENTER);
            floors[i].setBorder(BorderFactory.createLineBorder(Color.black));
            floors[i].setBackground(Color.white);
            floors[i].setForeground(Color.black);
            floors[i].setOpaque(true);
            floors[i].setFont(font);
            floors[i].setBounds(0, (6-i-1)*45, 45, 45);
            panel.add(floors[i]);
        }
        floors[0].setBackground(Color.PINK);
        panel.setBackground(Color.WHITE);


        door.setBounds(45+15, 0, (60*2), 60*2);
        panel.add(door);

        elevatorstate.setBorder(BorderFactory.createLineBorder(Color.black));
        elevatorstate.setFont(font);
        elevatorstate.setBounds(45+15, 15+60*2, (60*2), 30);
        panel.add(elevatorstate);

        JPanel subPanel = new JPanel();
        subPanel.setLayout(null);
        subPanel.setBackground(Color.WHITE);

        subPanel.setBounds(45+15, 60*2+30+15, 60*2, 15*2+60*(6/2+1));
        for(int i = 0; i < floorButtons.length; i++) {
            if(i < 6) {
                floorButtons[i] = new JButton(String.valueOf(i+1));
                if(i < 6/2)
                    floorButtons[i].setBounds(0, 15+((6/2)-i-1)*60, 60, 60);
                else
                    floorButtons[i].setBounds(60, 15+(6-i-1)*60, 60, 60);
            }
            floorButtons[i].addActionListener(l);
            floorButtons[i].setFont(font);
            floorButtons[i].setBackground(Color.WHITE);
            floorButtons[i].setForeground(Color.BLACK);
            subPanel.add(floorButtons[i]);
        }
        close.setBounds(0, 15+6/2*60, 60, 60);
        close.addActionListener(l);
        close.setFont(font);
        close.setBackground(Color.WHITE);
        close.setForeground(Color.BLACK);
        subPanel.add(close);

        open.setBounds(60, 15+6/2*60, 60, 60);
        open.addActionListener(l);
        open.setFont(font);
        open.setBackground(Color.WHITE);
        open.setForeground(Color.BLACK);
        subPanel.add(open);

        panel.add(subPanel);
    }

    private void findNearest() {
        int upNear;
        int downNear;
        for(upNear = onfloor; upNear < 6+1 && to[upNear-1] == false; upNear++);
        for(downNear = onfloor; downNear > 0 && to[downNear-1] == false  ; downNear--);

        if(upNear == 6+1) {
            state = -1;
            destination = downNear;
        }
        else if(downNear == 0) {
            state = 1;
            destination = upNear;
        }
        else if(state == 1)
            destination = upNear;
        else if(state == -1)
            destination = downNear;
        else if((upNear-onfloor) < (onfloor-downNear)) {
            state = 1;
            destination = upNear;
        }
        else {
            state = -1;
            destination = downNear;
        }
    }

    private void move() {
        long pretime = System.currentTimeMillis();

        for(int i = 1; onfloor < destination; i++, onfloor++) {
            if(checkNewTo()==1 || checkOutsideRequest()) return;
            while(true) {
                clearDoorButton();
                long passtime = (System.currentTimeMillis()-pretime)/1000;
                if(passtime*velocity/45 >= i)
                    break;
            }
            floors[onfloor-1].setBackground(Color.WHITE);
            floors[onfloor].setBackground(Color.PINK);
            elevatorstate.setText("▲ " + String.valueOf(onfloor+1));
        }

        for(int i = 1; onfloor > destination; i++, onfloor--) {
            if(checkNewTo()==1 || checkOutsideRequest()) return;

            while(true) {
                clearDoorButton();
                long passtime = (System.currentTimeMillis()-pretime)/1000;
                if(passtime*velocity/45 >= i)
                    break;
            }
            floors[onfloor-1].setBackground(Color.WHITE);
            floors[onfloor-2].setBackground(Color.PINK);
            elevatorstate.setText("▼ " + String.valueOf(onfloor-1));
        }

        floorButtons[destination-1].setBackground(Color.WHITE);
        to[destination-1] = false;

        long firsttime = System.currentTimeMillis();
        while(System.currentTimeMillis()-firsttime < latency);
        doorstate = 1;
        controlDoor();
    }

    private boolean checkOutsideRequest() {
        for(int i = 0; i < 6; i++)
            if(outsideRequest[i] == true) return true;
        return false;
    }

    public void traverseTo() {
        if(floorButtons[onfloor-1].getBackground() == Color.PINK)
            floorButtons[onfloor-1].setBackground(Color.WHITE);

        for(int i = 0; i < 6; i++) {
            if(outsideRequest[i] == true) {
                to[i] = true;
                outsideRequest[i] = false;
            }

            if(floorButtons[i].getBackground() == Color.PINK && to[i] == false)
                to[i] = true;
        }
    }

    private int checkNewTo() {
        if(floorButtons[onfloor-1].getBackground()==Color.PINK)
            floorButtons[onfloor-1].setBackground(Color.WHITE);

        for(int i = 0; i < 6; i++) {
            if(floorButtons[i].getBackground()==Color.PINK && to[i]==false) {
                if(onfloor < i+1) return 1;
                else return -1;
            }
        }
        return 0;
    }

    private boolean checkTo() {
        for(int i = 0; i < 6; i++)
            if(to[i] == true) return true;
        return false;
    }

    private int checkDoorButton() {
        if(open.getBackground() == Color.PINK) return 1;
        if(close.getBackground() == Color.PINK) return 2;
        return 0;
    }

    private void clearDoorButton() {
        if(checkDoorButton() == 1)
            open.setBackground(Color.WHITE);
        else if(checkDoorButton() == 2)
            close.setBackground(Color.WHITE);
        else return;
    }

    private void doorClosedAtState0() {
        if(checkDoorButton() == 1) {
            open.setBackground(Color.WHITE);
            doorstate = 1;
            controlDoor();
        }
        else if(checkDoorButton() == 2)
            close.setBackground(Color.WHITE);
    }

    private void controlDoor() {
        while(doorstate != -2) {
            if(doorstate == 1) opening();
            if(doorstate == 2) opened();
            if(doorstate == -1) closing();
        }
        closed();
    }

    private void opening() {
        door.setIcon(openingIcon);
        long now = System.currentTimeMillis();
        while(System.currentTimeMillis()-now < closeopenTime) {
            if(to[onfloor-1] == true)
                to[onfloor-1] = false;
            if(checkDoorButton() == 1)
                open.setBackground(Color.WHITE);
            else if(checkDoorButton() == 2) {
                close.setBackground(Color.WHITE);
                closing();
                return;
            }
        }
        doorstate = 2;
    }

    private void opened() {
        door.setIcon(openIcon);
        long now = System.currentTimeMillis();
        while(System.currentTimeMillis()-now < keepOpenTime) {
            if(to[onfloor-1] == true) {
                to[onfloor-1] = false;
                opened();
                return;
            }
            if(checkDoorButton() == 1) {
                open.setBackground(Color.WHITE);
                opened();
                return;
            }
            else if(checkDoorButton() == 2) {
                close.setBackground(Color.WHITE);
                closing();
                return;
            }
        }
        doorstate = -1;
    }

    private void closing() {
        door.setIcon(closingIcon);
        long now = System.currentTimeMillis();
        while(System.currentTimeMillis()-now < closeopenTime) {
            if(to[onfloor-1] == true) {
                to[onfloor-1] = false;
                opening();
                return;
            }
            if(checkDoorButton() == 1) {
                open.setBackground(Color.WHITE);
                opening();
                return;
            }
            else if(checkDoorButton() == 2)
                close.setBackground(Color.WHITE);
        }
        doorstate = -2;
    }

    private void closed() {
        door.setIcon(closeIcon);
    }

}