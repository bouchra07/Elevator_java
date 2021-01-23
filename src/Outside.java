import java.awt.*;

import javax.swing.*;

public class Outside implements Runnable{
    JPanel panel;
    JLabel[] floors;
    JButton[] updown;
    int[] floorTobutton;
    int[] stateTobutton;
    int[] pushed;
    int[] answerelevator;
    static final Font font = new Font("font",1,16);
    ButtonListener l;

    Outside(){
        panel = new JPanel();
        floors = new JLabel[6];
        updown = new JButton[6*2-2];
        pushed = new int[6*2-2];
        for(int i = 0; i < pushed.length; i++)
            pushed[i] = 0;

        floorTobutton = new int[6*2-2];
        for(int i = 0; i < floorTobutton.length; i++)
            if(i % 2 == 0)
                floorTobutton[i] = 6-i/2;
            else
                floorTobutton[i] = 6-(i+1)/2;

        stateTobutton = new int[6*2-2];
        for(int i = 0; i < stateTobutton.length; i++)
            if(i % 2 == 0)
                stateTobutton[i] = -1;
            else
                stateTobutton[i] = 1;

        answerelevator = new int[6*2-2];
        for(int i = 0; i < answerelevator.length; i++)
            answerelevator[i] = -1;

        l = new ButtonListener();
        layout();
    }

    private void layout(){
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);

        for(int i = 0; i < floors.length; i++) {
            floors[i] = new JLabel(String.valueOf(i+1),JLabel.CENTER);
            floors[i].setBorder(BorderFactory.createLineBorder(Color.black));
            floors[i].setBackground(Color.white);
            floors[i].setForeground(Color.black);
            floors[i].setFont(font);
            floors[i].setBounds(0, (6-i-1)*45, 45, 45);
            panel.add(floors[i]);
        }

        for(int i = 0; i < updown.length; i++) {
            if(i % 2 == 0) {
                updown[i] = new JButton("▼");
                if(i == 0)
                    updown[i].setBounds(45*2, 0, 45*2, 45);
                else
                    updown[i].setBounds(45*3, (i/2)*45, 45*2, 45);
            }
            else {
                updown[i] = new JButton("▲");
                if(i == updown.length-1)
                    updown[i].setBounds(45*2, 45*(6-1), 45*2, 45);
                else
                    updown[i].setBounds(45, (i/2+1)*45, 45*2, 45);
            }
            updown[i].addActionListener(l);
            updown[i].setBackground(Color.white);
            updown[i].setForeground(Color.black);
            updown[i].setFont(font);
            panel.add(updown[i]);
        }
    }

    public void run() {
        while(true) {
            if(checkNewButton())
                try {
                    traverseButton();
                } catch(Exception e) {
                    System.exit(0);
                }
        }
    }

    private boolean checkNewButton() {
        for(int i = 0; i < updown.length; i++) {
            if(updown[i].getBackground()==Color.PINK && pushed[i]==0)
                return true;
        }
        return false;
    }

    private void traverseButton() {
        for(int i = 0; i < updown.length; i++) {
            if(updown[i].getBackground()==Color.PINK && pushed[i]==0)
                pushed[i] = 1;
        }
    }
}
