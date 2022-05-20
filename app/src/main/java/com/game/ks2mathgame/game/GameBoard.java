package com.game.ks2mathgame.game;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.game.ks2mathgame.MainActivity;
import com.game.ks2mathgame.Mode;
import com.game.ks2mathgame.R;
import com.game.ks2mathgame.visuals.Story;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;


public class GameBoard extends AppCompatActivity {

    ChallengeGame game;

    //declare textviews
    private TextView question_text, ansCount_text;

    //declare layouts
    private GridLayout options_grid;  //this layout contains textviews for options
    private LinearLayout hearts_layout;
    public LinearLayout msg_box;

    private Timer timer;
    private Handler handler;

    private char opera;  //for which operator to run this game
    private Mode mode;

    private boolean isOppoListenerSet = false;  //opponent listener set or not
    private boolean isClosedByOppoListener = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        msg_box = findViewById(R.id.msg_box);

        //lets know which topic is this

        opera = MainActivity.selectedTopic.operator;
        mode = MainActivity.selectedMode;

        //create a new game object
        if(mode == Mode.CHALLENGE){
            game = new ChallengeGame(opera);
        }else if(mode == Mode.MULTIPLAYER){
            game = new MultiplayerGame(opera);
        }
        else
            game = new StoryGame(opera, Story.selectedLevel);


        //set the level label to show which level is this
        TextView level_label = findViewById(R.id.level_label);
        level_label.setText("Level "+ Story.selectedLevel);
        if (mode != Mode.STORY)//no need to show level for other modes
            level_label.setText(mode.toString());

        //initialize views
        question_text = findViewById(R.id.question_text);
        ansCount_text = findViewById(R.id.ansCount_text);

        options_grid = findViewById(R.id.options_grid);
        hearts_layout = findViewById(R.id.hearts_layout);
        TextView timer_text = findViewById(R.id.timer_text);

        showNextQuestion();
        //start the timer

        timer = new Timer();
        handler = new Handler();

        /*schedule a timer which starts after 1 second of start of this activity
         and calls the  handler to update UI, handler is always used to update UI thread*/
        final int gameTime = game.getTime();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        timer_text.setText(game.getTime() + " seconds");
                        game.decTime();
                        if (game.getTime() <= gameTime/2 && !isOppoListenerSet && mode == Mode.MULTIPLAYER){
                            setOppoListener();
                            isOppoListenerSet = true;
                        }
                        if(game.timeout()){
                            timer_text.setText(0 + " seconds");
                            closeGameBoard();
                            if(mode == Mode.MULTIPLAYER){
                                sendMyScore();
                            }
                        }
                    }
                });
            }
        }, 500, 1000);

        //set click listeners for all options of the options grid
        for(int i = 0; i < 4; i++){
            //we are confirmed that every child of options_grid is a textview
            //so get every child as textview and set its text equal to the options text
            TextView option_text = (TextView) options_grid.getChildAt(i);

            option_text.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //when this text is pressed, check the answer
                    //as the textview contains the text so get int from int for checking the ans
                    int response = Integer.parseInt(option_text.getText().toString());
                    if(game.getQuestion().checkAns(response))
                    {
                        game.incScore();  //increase score (actual correct answers in game)
                    }
                    else
                    {
                        game.decLives();  //wrong response, then decrease lives by 1
                    }
                    showNextQuestion();
                }
            });
        }
    }

    void showNextQuestion(){
        Question question = game.getNextQuestion();

        //following statement create question string of form ->question \n a + b = ?
        String question_string = "Question "+game.getQuestionCount()+" : \n" + question.a + " " +
                (char)question.operator + " " + question.b + " = ?";
        question_text.setText(question_string);
        ansCount_text.setText("Correct Answer: "+ game.getScore());

        //set new options for next questions
        for(int i = 0; i < question.options.length; i++){
            //we are confirmed that every child of options_grid is a textview
            //so get every child as textview and set its text equal to the options text
            TextView option_text = (TextView) options_grid.getChildAt(i);
            option_text.setText(""+question.options[i]);
        }
        //check progress if switched to next question
        checkProgress();
    }

    private void checkProgress(){
        setHearts();  //set the hearts for every check of progress
        if(game.isGameEnd()){
            closeGameBoard();
        }
    }

    private void setHearts(){
        for(int i = 0; i < hearts_layout.getChildCount(); i++){
            //we are sure that hearts layout contains imageview only, so we typecast
            ImageView heart =  (ImageView) hearts_layout.getChildAt(i);
            if (i < game.getLives()){
                heart.setVisibility(View.VISIBLE);
            }else{
                heart.setVisibility(View.INVISIBLE);
            }
        }
        if (game.isGameEnd())
            closeGameBoard();
    }



    public void setOppoListener(){
        //get the ids of players from previous activity
        Intent intent = getIntent();
        String myID = intent.getStringExtra("myID");
        String opponentID = intent.getStringExtra("opponentID");

        //prepare database
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference roomRef = db.getReference("rooms");
        DatabaseReference oppoIDRef = roomRef.child(opponentID);

        final boolean[] isListenerSet = {false};
        //save my score to database and wait for the opponents score
        oppoIDRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(isListenerSet[0]){
                    Integer oppoScore = snapshot.child("score").getValue(Integer.class);
                    MultiplayerGame multiplayerGame = (MultiplayerGame)game;
                    multiplayerGame.oppoID = opponentID;
                    multiplayerGame.oppoScore = oppoScore;

                    if (game.timeout()){
                        /*
                        As firebase listener is asynchronous, and due to internet any user may have completed game before other
                        So, if the player has received the result earlier, and this player time is not out, then don't close the gameboard.
                        As this condition means game.timeout is true, so both players have finished the game because, the other player sent the score earlier
                        and this player's timeout is now done also
                        */
                        closeGameBoard();
                    }

                    oppoIDRef.removeEventListener(this);
                }else{
                    isListenerSet[0] = true;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void sendMyScore(){
        //get the ids of players from previous activity
        Intent intent = getIntent();
        String myID = intent.getStringExtra("myID");

        //prepare database
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference roomRef = db.getReference("rooms");
        DatabaseReference myIDRef = roomRef.child(myID);

        //send my score to database
        myIDRef.child("score").setValue(game.getScore());
        myIDRef.child("time").setValue(System.currentTimeMillis());
    }

    private void closeGameBoard(){
        timer.cancel();
        game.result(this);
        options_grid.setVisibility(View.INVISIBLE);
    }
}