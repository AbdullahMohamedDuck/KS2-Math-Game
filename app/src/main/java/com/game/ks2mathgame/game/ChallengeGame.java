package com.game.ks2mathgame.game;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.game.ks2mathgame.MainActivity;

import java.util.HashSet;
import java.util.Random;

class ChallengeGame{
    //Parent class

    protected int time = 60, lives = 3;
    protected int correctAns_count = 0, question_count = 0;
    protected Question question;

    //game operator and level can't be changed in the current game play
    protected final char operator;

    private final HashSet<Integer> wrongSet = new HashSet<Integer>();
    private Random random;


    ChallengeGame(char operator){
        this.operator = operator;
        random = new Random();
    }

    public Question getNextQuestion(){
        question_count++;
        question = new Question(operator, correctAns_count * getDifficulty());
        //if wrong set is not empty it means user has marked some questions wrong
        //so for new question get random wrong digits from and replace at random position of digit of new question in both operands
        //while difficulty will also be increasing

        if (!wrongSet.isEmpty()){
            Log.d("passing a", "   "+question.a);
            question.a = replaceDigit(question.a);
            Log.d("passing b", "   "+question.b);
            question.b = replaceDigit(question.b);
            question.calculateAns();  //recalculate the answer with new numbers
            question.setOptions();
        }

        return question;
    }

    private void addToWrongSet(int a, int b){
        //adds all digits from number 'a' and 'b' to wrong set
        while(a != 0){
            int x = a % 10;
            wrongSet.add(x);
            a /= 10;
            Log.d("adding-------------->", ""+x);
        }

        while(b != 0){
            int x = b % 10;
            wrongSet.add(x);
            b /= 10;
            Log.d("adding-------------->", ""+x);
        }
    }

    private int digits(int num){
        int x = num;
        //counts digits present in num
        int count = 1;  //it is not possible that digit count is zero
        while(num/10 != 0){
            num /= 10;
            count++;
        }
        Log.d("digits in "+x, "   "+count);
        return count;
    }

    private int replaceDigit(int num){
        Log.d("rd passed-->", ""+num);
        //replace a random digit from the current answer with a random chosen number from the wrong set
        int multiplier = 1;
        int newNum = 0;
        //get a digit from wrong set
        int wDigit = (int) wrongSet.toArray()[Math.abs(random.nextInt())%wrongSet.size()];
        int rand = Math.abs(random.nextInt()) % (digits(num));
        int loopFor = digits(num)+1;
        for (int i = 0; i <= loopFor; i++){
            int x = num%10;
            if (i == rand)
            {
                Log.d("replacing with-->", ""+wDigit);
                x = wDigit;  //replace with wrong digit from the set
            }
            num = num/10;
            newNum += x * multiplier;
            multiplier *= 10;
        }
        return newNum;
    }

    public int getLives() {
        return lives;
    }

    public void decLives(){
        //lives are incremented only if the answer is wrong
        //so take numbers from the wrong question and add them to wrong set
        addToWrongSet(question.a, question.b);
        lives--;
    }

    public void decTime(){
        time--;
    }

    public int getTime() {
        return time;
    }

    public int getScore(){
        return correctAns_count;
    }

    public boolean timeout(){
        return  time < 0;
    }

    public Question getQuestion() {
        return question;
    }


    public void incScore(){
        correctAns_count ++;
        time += 3;  //for challenge mode, time needs to be incremented by 3
        //score is incremented only if the ans is correct
        //so if ans is correct, time is incremented by 3 for challenge mode
    }

    public int getQuestionCount(){
        return question_count;
    }

    public boolean isGameEnd(){
        return timeout() || lives < 1;  //challenge game ends only if the time is out or timer runs out
    }

    public void result(GameBoard gameBoard){
        LinearLayout msg_box = gameBoard.msg_box;
        TextView msg_text = (TextView) msg_box.getChildAt(0);  //first child of msg_layout is textview for msg
        Button msg_ok = (Button)  msg_box.getChildAt(2); // second child of msg layout is an ok button

        msg_text.setText("Game Ended\nYour Score: "+correctAns_count);
        msg_box.setVisibility(View.VISIBLE);

        //now add the ok button listener, so when it is pressed it takes the user to respective activity
        msg_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameBoard.startActivity(new Intent(gameBoard, MainActivity.class));
                gameBoard.finishAffinity();
            }
        });  //end of ok listener
    }

    protected int getDifficulty(){
        if (operator == '+' || operator == '-')
            return 5;  // addition and subtraction is easy, so every level should have a greater difference of values
        else
            return 1;  // multiplication and division is difficult so difficulty level is handled accordingly
        /*
        For level 5 minimum value in random generation becomes 5*4 +5 = 25
        so if random generated numbers are  19 and 23, its easy  to add or subtract them
        but if we use same difficulty for multiplication or division its becomes very difficult for KS2 students
        so for multiplication and division the minimum value is lower
        */
    }

}
