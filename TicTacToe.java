package clients;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
public class TicTacToe implements ActionListener{
    Random random = new Random();
    JFrame frame = new JFrame();
    JPanel title_panel = new JPanel();
    JPanel button_panel = new JPanel();
    JLabel text_field = new JLabel();
    JButton[] buttons = new JButton[9];
    boolean player1_turn;


//  Constructor for TicTacToe class
public TicTacToe(){

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,800);
        frame.getContentPane().setBackground(new Color(50,50,50));
        frame.setLayout(new BorderLayout());
        frame.setVisible(true);

        text_field.setBackground(new Color(25,25,25));
        text_field.setForeground(new Color(25,255,0));
        text_field.setHorizontalAlignment(JLabel.CENTER);
        text_field.setFont(new Font("Ink Free",Font.BOLD,75));
        text_field.setText("Tic-Tac-Toe");
        text_field.setOpaque(true);

//      add text_field to title panel and title panel to our frame
        title_panel.setLayout(new BorderLayout());
        title_panel.setBounds(0,0,800,100);

        button_panel.setLayout(new GridLayout(3,3));
        button_panel.setBackground(new Color(125,140,180));


//      Using a for loop to create the buttons
        for(int i=0; i<9; i++) {
            buttons[i] = new JButton();
            button_panel.add(buttons[i]);
            buttons[i].setFont(new Font("MV Boli",Font.BOLD,120));
            buttons[i].setFocusable(false);
            buttons[i].addActionListener(this);
        }

        title_panel.add(text_field);
        frame.add(title_panel,BorderLayout.NORTH);
        frame.add(button_panel);

        firstTurn();

    }
    @Override
    public void actionPerformed(ActionEvent e){

        for (int i = 0; i < 9; i ++){
            if(e.getSource() == buttons[i]){
                if(player1_turn){
                    if(Objects.equals(buttons[i].getText(), "")){
                        buttons[i].setForeground(new Color(255,0,0));
                        buttons[i].setText("X");
                        player1_turn = false;
                        text_field.setText("O's Turn");
                        check();
                    }
                }
                else {
                    if(Objects.equals(buttons[i].getText(), "")){
                        buttons[i].setForeground(new Color(0,0,255));
                        buttons[i].setText("O");
                        player1_turn = true;
                        text_field.setText("X's Turn");
                        check();
                    }
                }
            }
        }
    }

    public void firstTurn() {
        disableButtons();
//      sleep for 2 seconds, then change title name randomly
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        enableButtons();

        if(random.nextInt(2) == 0){
            player1_turn = true;
            text_field.setText("X's turn");
        }
        else {
            player1_turn = false;
            text_field.setText("O's turn");

        }


    }

    public void check(){
        //check X win conditions
        if(
                (Objects.equals(buttons[0].getText(), "X")) &&
                        (Objects.equals(buttons[1].getText(), "X")) &&
                        (Objects.equals(buttons[2].getText(), "X"))
        ) {
            xWins(0,1,2);
        }
        if(
                (Objects.equals(buttons[3].getText(), "X")) &&
                        (Objects.equals(buttons[4].getText(), "X")) &&
                        (Objects.equals(buttons[5].getText(), "X"))
        ) {
            xWins(3,4,5);
        }
        if(
                (Objects.equals(buttons[6].getText(), "X")) &&
                        (Objects.equals(buttons[7].getText(), "X")) &&
                        (Objects.equals(buttons[8].getText(), "X"))
        ) {
            xWins(6,7,8);
        }
        if(
                (Objects.equals(buttons[0].getText(), "X")) &&
                        (Objects.equals(buttons[3].getText(), "X")) &&
                        (Objects.equals(buttons[6].getText(), "X"))
        ) {
            xWins(0,3,6);
        }
        if(
                (Objects.equals(buttons[1].getText(), "X")) &&
                        (Objects.equals(buttons[4].getText(), "X")) &&
                        (Objects.equals(buttons[7].getText(), "X"))
        ) {
            xWins(1,4,7);
        }
        if(
                (Objects.equals(buttons[2].getText(), "X")) &&
                        (Objects.equals(buttons[5].getText(), "X")) &&
                        (Objects.equals(buttons[8].getText(), "X"))
        ) {
            xWins(2,5,8);
        }
        if(
                (Objects.equals(buttons[0].getText(), "X")) &&
                        (Objects.equals(buttons[4].getText(), "X")) &&
                        (Objects.equals(buttons[8].getText(), "X"))
        ) {
            xWins(0,4,8);
        }
        if(
                (Objects.equals(buttons[2].getText(), "X")) &&
                        (Objects.equals(buttons[4].getText(), "X")) &&
                        (Objects.equals(buttons[6].getText(), "X"))
        ) {
            xWins(2,4,6);
        }
        //check O win conditions
        if(
                (Objects.equals(buttons[0].getText(), "O")) &&
                        (Objects.equals(buttons[1].getText(), "O")) &&
                        (Objects.equals(buttons[2].getText(), "O"))
        ) {
            oWins(0,1,2);
        }
        if(
                (Objects.equals(buttons[3].getText(), "O")) &&
                        (Objects.equals(buttons[4].getText(), "O")) &&
                        (Objects.equals(buttons[5].getText(), "O"))
        ) {
            oWins(3,4,5);
        }
        if(
                (Objects.equals(buttons[6].getText(), "O")) &&
                        (Objects.equals(buttons[7].getText(), "O")) &&
                        (Objects.equals(buttons[8].getText(), "O"))
        ) {
            oWins(6,7,8);
        }
        if(
                (Objects.equals(buttons[0].getText(), "O")) &&
                        (Objects.equals(buttons[3].getText(), "O")) &&
                        (Objects.equals(buttons[6].getText(), "O"))
        ) {
            oWins(0,3,6);
        }
        if(
                (Objects.equals(buttons[1].getText(), "O")) &&
                        (Objects.equals(buttons[4].getText(), "O")) &&
                        (Objects.equals(buttons[7].getText(), "O"))
        ) {
            oWins(1,4,7);
        }
        if(
                (Objects.equals(buttons[2].getText(), "O")) &&
                        (Objects.equals(buttons[5].getText(), "O")) &&
                        (Objects.equals(buttons[8].getText(), "O"))
        ) {
            oWins(2,5,8);
        }
        if(
                (Objects.equals(buttons[0].getText(), "O")) &&
                        (Objects.equals(buttons[4].getText(), "O")) &&
                        (Objects.equals(buttons[8].getText(), "O"))
        ) {
            oWins(0,4,8);
        }
        if(
                (Objects.equals(buttons[2].getText(), "O")) &&
                        (Objects.equals(buttons[4].getText(), "O")) &&
                        (Objects.equals(buttons[6].getText(), "O"))
        ) {
            oWins(2,4,6);
        }

//      what happens when there is a tie
        for (int x=0 ; x<9 ; x++) {
            // if 1 of 9 button texts is blank
            if (buttons[x].getText().isBlank()) {
                // the tie condition is not satisfied .
                break ;
            }
            // if both 9 button texts are not blank .
            if (x == 8) {
                // game finishes .
                // the text color of buttons will be disabled as well, so they will turn gray .
                disableButtons() ;
                text_field.setText("Tie");
            }
        }

    }
//  what would happen if x or o wins
    public void xWins(int a, int b, int c){
        buttons[a].setBackground(Color.GREEN);
        buttons[b].setBackground(Color.GREEN);
        buttons[c].setBackground(Color.GREEN);

        for(int i=0;i<9;i++) {
            buttons[i].setEnabled(false);
        }
        text_field.setText("X wins");
    }
    public void oWins(int a, int b, int c){
        buttons[a].setBackground(Color.GREEN);
        buttons[b].setBackground(Color.GREEN);
        buttons[c].setBackground(Color.GREEN);

        for(int i=0;i<9;i++) {
            buttons[i].setEnabled(false);
        }
        text_field.setText("O wins");

    }

    public void disableButtons(){
        for (int i=0; i < 9; i++){
            buttons[i].setEnabled(false);
        }
    }

    public void enableButtons(){
        for (int i=0; i < 9; i++){
            buttons[i].setEnabled(true);
        }
    }




}