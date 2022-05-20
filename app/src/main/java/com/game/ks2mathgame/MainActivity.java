package com.game.ks2mathgame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.game.ks2mathgame.account.Signin;
import com.game.ks2mathgame.game.GameBoard;
import com.game.ks2mathgame.visuals.GameCharacter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    public static Topic topics[] = {
            new Topic("Addition",  R.drawable.brown_hat, '+'),
            new Topic("Subtraction",  R.drawable.green_hat, '-'),
            new Topic("Multiplication",  R.drawable.blue_hat, '*'),
            new Topic("Division",  R.drawable.red_hat, '/'),
    };
    //create a default topic object so every class knows which topic is currently selected by user
    public static Topic selectedTopic = topics[0];  //by default first topic
    public static Mode selectedMode = Mode.STORY;  //by default story mode

    private Intent topicSelectorIntent;

    FirebaseDatabase db;
    DatabaseReference roomRef, myIDRef;

    ValueEventListener challengeListener;
    final boolean[] isListenerSet = {false};  //this is used to skip first data change event fired, because first data change is always fired, even if value is not changed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        if (email == null){  //if email null, it means the player is not logged in yet so take the user to login screen
            finish();
            startActivity(new Intent(getApplicationContext(), Signin.class));
            return;
        }

        //get the levels progress from shared preferences, for each topic
        for (int i = 0; i < topics.length; i++){
            int lvl = sharedPreferences.getInt("topic"+i, 1);  //if game is played for the first time, default value 1 is returned
            topics[i].setLevel(lvl);
        }

        topicSelectorIntent = new Intent(this, TopicSelector.class);
        //add a multiplayer challenge listener
        db = FirebaseDatabase.getInstance();
        email = Signin.getUserID(email);
        roomRef = db.getReference("rooms");
        myIDRef = roomRef.child(email);
        challengeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(isListenerSet[0]){
                    String opponent = snapshot.child("opponent").getValue(String.class);

                    //get the topic in which topic player received challenge
                    Integer challengedTopic = snapshot.child("topic").getValue(Integer.class);
                    if(challengedTopic != null){
                        setSelectedTopic(challengedTopic);
                        //set the selected topic to that topic

                        //show the alert for challenge
                        showChallengeAlert(opponent);
                    }
                }else{
                    isListenerSet[0] = true;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        myIDRef.addValueEventListener(challengeListener);
    }

    public void story_click(View view) {
        selectedMode = Mode.STORY;
        startActivity(topicSelectorIntent);
    }

    public void challenge_click(View view) {
        selectedMode = Mode.CHALLENGE;
        startActivity(topicSelectorIntent);
    }

    public void character_click(View view) {
        Intent intent = new Intent(getApplicationContext(), GameCharacter.class);
        startActivity(intent);
    }

    public void multiplayer_click(View view) {
        selectedMode = Mode.MULTIPLAYER;
        myIDRef.removeEventListener(challengeListener); //if this person is going to vs some other person, then this person does not need to listen for the challenge
        startActivity(topicSelectorIntent);
    }


    public void profile_click(View view) {
        startActivity(new Intent(getApplicationContext(), Signin.class));
    }

    private void showChallengeAlert(String opponent){
        myIDRef.removeEventListener(challengeListener);  //remove the listener, when player has received challenge
        isListenerSet[0] = false;
        //we setup the listener again if the player declines this challenge, so that user can listen for next challenges

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(opponent+ " challenged you in Multiplayer Mode in topic, "+selectedTopic.topicName);
        alertDialogBuilder.setPositiveButton("Accept",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        handleChallenge(opponent, "accept");
                        selectedMode = Mode.MULTIPLAYER;
                        Intent intent = new Intent(getApplicationContext(), GameBoard.class);
                        intent.putExtra("myID", getMyId());  //room id is required in multiplayer mode
                        intent.putExtra("opponentID", opponent);
                        startActivity(intent);
                    }
                });

        alertDialogBuilder.setNegativeButton("Decline",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //decline the challenge but keep listening to more challenges
                handleChallenge(opponent, "decline");
                myIDRef.addValueEventListener(challengeListener);  //set listener for new challenge after declining the other one
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private String getMyId(){
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "default@mail.com");
        email = Signin.getUserID(email);
        return email;
    }

    private void handleChallenge(String opponent, String msg){
        DatabaseReference oppoRef = roomRef.child(opponent);
        oppoRef.child("msg").setValue(msg);  //tell opponent that challenge accepted
        oppoRef.child("opponent").setValue(getMyId());
        oppoRef.child("time").setValue(System.currentTimeMillis());

    }

    private static void setSelectedTopic(int topicID){
        selectedTopic = topics[topicID];
    }

    public static int getTopicID() {
        //returns the position at which selected topic exists in topics
        int i = 0;
        for (i = 0; i < topics.length; i++){
            if (selectedTopic == topics[i])
                return i;
        }
        return i;
    }

}