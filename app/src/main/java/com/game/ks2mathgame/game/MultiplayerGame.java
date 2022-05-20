package com.game.ks2mathgame.game;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.game.ks2mathgame.MainActivity;

import java.util.Random;

class MultiplayerGame extends ChallengeGame{
    public int oppoScore = 0;
    public String myID, oppoID; // in multiplayer there are two ids which are used, one is for this player and other for the remote player
    private boolean isRunning = true;

    MultiplayerGame(char operator) {
        super(operator);

        //remove lives as it's not counted in multiplayer
        lives = -1;
    }

    @Override
    public void result(GameBoard gameBoard) {

        String msg = "";
        if (getScore() > oppoScore)
            msg = "You Win!\n\nYour Score : "+ getScore() + "\n" +oppoID+"'s Score: "+oppoScore;
        else if (getScore() < oppoScore)
            msg = "You Lose!\n\nYour Score : "+ getScore() + "\n" +oppoID+"'s Score: "+oppoScore;
        else
            msg =  "Match Draw";

        if (oppoID == null)
            msg = "Waiting for Opponent Result";

        LinearLayout msg_box = gameBoard.msg_box;
        TextView msg_text = (TextView) msg_box.getChildAt(0);  //first child of msg_layout is textview for msg
        Button msg_ok = (Button)  msg_box.getChildAt(2); // second child of msg layout is an ok button

        msg_text.setText(msg);
        msg_box.setVisibility(View.VISIBLE);

        //now add the ok button listener, so when it is pressed take the user to respective activity
        msg_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameBoard.startActivity(new Intent(gameBoard, MainActivity.class));
                gameBoard.finishAffinity();
            }
        });
    }

    @Override
    public Question getNextQuestion(){
        question_count++;
        question = new Question(operator, getDifficulty());

        //we can focus on the wrong questions by checking number of correct answers
        //if number of correct answer does not increase, the next question is generated in the previous range
        return question;
    }

    @Override
    public boolean isGameEnd(){
        return !isRunning;
    }

    @Override
    public void decLives() {
        //in multiplayer lives need not to be decreased
    }

    @Override
    protected int getDifficulty() {
        //for online multiplayer create an average question
        Random random = new Random();
        if (operator == '+' || operator == '-')
            return Math.abs(random.nextInt()) % 20;  // addition and subtraction is easy, so every level should have a greater difference of values
        else
            return Math.abs(random.nextInt()) % 10;  // multiplication and division is difficult so difficulty level is handled accordingly
        /*
           For level 5 minimum value in random generation becomes 5*4 +5 = 25
        so if random generated numbers are  19 and 23, its easy  to add or subtract them
        but if we use same difficulty for multiplication or division its becomes very difficult for KS2 students
        so for multiplication and division the minimum value is lower
        */
    }


    @Override
    public void incScore() {
        correctAns_count++;
    }
}
