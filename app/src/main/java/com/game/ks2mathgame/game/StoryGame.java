package com.game.ks2mathgame.game;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.game.ks2mathgame.MainActivity;
import com.game.ks2mathgame.account.Signin;
import com.game.ks2mathgame.visuals.Story;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

class StoryGame extends ChallengeGame{
    /*
    Note : Story Game extends the Challenge Game
    */

    private final int level, pass_score = 10;  //level is only visible to story mode game
    StoryGame(char operator, int level) {
        super(operator);
        this.level = level;
    }

    @Override
    public boolean isGameEnd(){
        return timeout() || lives < 1 || correctAns_count >= pass_score;
    }

    @Override
    public void incScore() {
        correctAns_count++;
    }

    @Override
    public void decLives() {
        lives--;
    }

    @Override
    public Question getNextQuestion(){
        question_count++;
        question = new Question(operator, level * getDifficulty()*2);
        //difficulty increase by 20%. getDifficulity() parent return 5 for +,- and 1 for /*.
        //Number is multiplied by level and then by 2to make a bigger range for the random num.
        //5 levels so 1/5*100 = 20% difficulty
        return question;
    }

    @Override
    public void result(GameBoard gameBoard){
        //Toast.makeText(gameBoard.getApplicationContext(), "display called", Toast.LENGTH_SHORT).show();
        LinearLayout msg_box = gameBoard.msg_box;
        TextView msg_text = (TextView) msg_box.getChildAt(0);  //first child of msg_layout is textview for msg
        ImageView msg_img = (ImageView) msg_box.getChildAt(1);  //second child of msg layout is an image view
        Button msg_ok = (Button)  msg_box.getChildAt(2); // third child of msg layout is an ok button

        if (correctAns_count < pass_score)
        {
            msg_text.setText("You Lose!");
            msg_box.setVisibility(View.VISIBLE);
        }
        else{
            msg_text.setText("You Win!");
            if (MainActivity.selectedTopic.isCompleted()){
                //show the reward (extrinsic motivators) to user
                msg_text.setText(MainActivity.selectedTopic.getAccolade());
                msg_img.setImageResource(MainActivity.selectedTopic.hat);
            }
            msg_box.setVisibility(View.VISIBLE);

            //update the unlocked level in shared preferences, so that when ever user opens the app, it gets the latest level result
            SharedPreferences sharedPreferences = gameBoard.getSharedPreferences("userInfo", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("topic"+MainActivity.getTopicID(), level+1);
            editor.apply();

            //update the progress in firebase database, so that if user logs in again, it starts from the previously saved state
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference userRef = db.getReference("users");
            DatabaseReference idRef = userRef.child(Signin.getUserID(sharedPreferences.getString("email", "")));
            DatabaseReference topicRef = idRef.child("topic"+MainActivity.getTopicID());
            topicRef.setValue(level+1);  //level update in firebase complete here

            MainActivity.selectedTopic.setLevel(level+1);  // unlock the next level
        }  //end of else

        //now add the ok button listener, so when it is pressed take the user to respective activity
        msg_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameBoard.startActivity(new Intent(gameBoard, MainActivity.class));
                gameBoard.finishAffinity();
            }
        });  //end of ok listener

    }


}
