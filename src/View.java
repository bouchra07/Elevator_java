import javax.swing.*;
import java.awt.*;

public class View implements Runnable{
    JFrame frame;
    JPanel allPanel;
    JLabel[] title;
    Font titlefont;
    Outside outbutton;
    Elevator[] elevator;
    int[] flag;

    public View() {
        frame = new JFrame("Elevator");
        allPanel = new JPanel();
        allPanel.setBackground(Color.WHITE);
        title = new JLabel[4];
        titlefont = new Font("titlefont",1,15);
        outbutton = new Outside();
        elevator = new Elevator[2];
        flag = new int[outbutton.updown.length];

        allPanel.setLayout(null);
        for(int i = 0; i < 3; i++) {
            if(i < 2){
                title[i] = new JLabel("Elevator "+(i+1), JLabel.CENTER);
                title[i].setBounds(i*(180+50)+15*2, 10, 180, 20);
            }
            else {
                title[i] = new JLabel("Outside Buttons");
                title[i].setBounds(i*(180+50)+15*3, 10, 180, 20);
            }

            title[i].setFont(titlefont);

            allPanel.add(title[i]);
        }

        for(int i = 0; i < 2; i++) {
            elevator[i] = new Elevator();
            elevator[i].panel.setBounds(i*(180+50)+15, 15+20, 180, 450);
            allPanel.add(elevator[i].panel);
        }

        outbutton.panel.setBounds((180+50)*2, 15+20, 240, 270);
        allPanel.add(outbutton.panel);

        frame.setBounds(250, 15, 730, 500);
        frame.getContentPane().add(allPanel);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public void run() {
        while(true) {
            analyseRequest();
            for(int i = 0; i < outbutton.answerelevator.length; i++) {
                if(outbutton.answerelevator[i] != -1) {
                    if(elevator[outbutton.answerelevator[i]].to[outbutton.floorTobutton[i]-1]) flag[i] = 1;
                    if(flag[i]==1 && !elevator[outbutton.answerelevator[i]].to[outbutton.floorTobutton[i]-1]){
                        flag[i] = 0;
                        outbutton.updown[i].setBackground(Color.WHITE);
                        outbutton.pushed[i] = 0;
                        outbutton.answerelevator[i] = -1;
                    }
                }
            }
        }
    }

    public void analyseRequest() {
        for(int i = 0; i < outbutton.updown.length; i++) {
            if(outbutton.pushed[i] == 1) {
                int requestfloor = outbutton.floorTobutton[i];
                int requeststate = outbutton.stateTobutton[i];

                double[] score = new double[elevator.length];
                for(int j = 0; j < elevator.length; j++)
                    score[j] = 100;

                if(requeststate == 1)
                    for(int j = 0; j < elevator.length; j++) {
                        if(elevator[j].state==requeststate && requestfloor-elevator[j].onfloor>0)
                            score[j] = 1 + (requestfloor - elevator[j].onfloor) * 0.05;
                        else if(elevator[j].state == 0)
                            score[j] = 2 + (requestfloor - elevator[j].onfloor) * 0.05;
                    }
                else
                    for(int j = 0; j < elevator.length; j++) {
                        if(elevator[j].state==requeststate && requestfloor-elevator[j].onfloor<0)
                            score[j] = 1 + (elevator[j].onfloor - requestfloor) * 0.05;
                        else if(elevator[j].state == 0)
                            score[j] = 2 + (elevator[j].onfloor - requestfloor) * 0.05;
                    }

                int elevatorindex = -1;
                double minscore = 100;
                for(int j = 0; j < elevator.length; j++) {
                    if(score[j] < minscore) {
                        minscore = score[j];
                        elevatorindex = j;
                    }
                }

                if(elevatorindex == -1) continue;

                elevator[elevatorindex].outsideRequest[requestfloor-1] = true;
                outbutton.answerelevator[i] = elevatorindex;
                outbutton.pushed[i] = 2;
            }
        }
    }

    public static void main(String[] args) {
        View view = new View();
        new Thread(view).start();
        for(int i = 0; i < 2; i++)
            new Thread(view.elevator[i]).start();
        new Thread(view.outbutton).start();
    }
}
