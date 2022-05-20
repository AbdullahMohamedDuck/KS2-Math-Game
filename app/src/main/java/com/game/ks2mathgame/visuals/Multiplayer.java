package com.game.ks2mathgame.visuals;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.game.ks2mathgame.MainActivity;
import com.game.ks2mathgame.R;
import com.game.ks2mathgame.account.Signin;
import com.game.ks2mathgame.game.GameBoard;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Multiplayer extends AppCompatActivity {

    private FirebaseDatabase db;
    private DatabaseReference roomRef;
    private DatabaseReference allRoomsRef;
    private final ArrayList<String> roomsList = new ArrayList<String>();  //for storing all rooms from firebase, to challenge a random player

    private Button challenge_player_btn, random_challenge_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);

        //prepare firebase database
        db = FirebaseDatabase.getInstance();
        roomRef = db.getReference("rooms");
        //temporary

        //initialize views
        EditText email_input = findViewById(R.id.email_input);
        challenge_player_btn = findViewById(R.id.challenge_player_btn);
        random_challenge_btn= findViewById(R.id.random_challenge_btn);

        challenge_player_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                challenge_player_btn.setEnabled(false);  //button is now disabled, so that user can't challenge multiple times at once
                String playerID = Signin.getUserID(email_input.getText().toString());
                sendChallenge(playerID);
            }
        });

        //click listner for random challenge
        random_challenge_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                random_challenge_btn.setEnabled(false);
                allRoomsRef = db.getReference("rooms");
                allRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        roomsList.clear();  //clear the previous rooms list
                        //get all children of rooms reference and store them to list
                        Iterable<DataSnapshot> onlineRooms = snapshot.getChildren();
                        String mId = getMyId();
                        for(DataSnapshot ds: onlineRooms){
                            String key = ds.getKey();
                            if (key != null && !key.equals(mId)) {  //can't challenge self
                                roomsList.add(key);
                            }
                        }

                        int n_of_rooms = roomsList.size();
                        if (n_of_rooms != 0){  //if there exists rooms then send challenge
                            Random random = new Random();
                            String randomPlayerID = roomsList.get(Math.abs(random.nextInt()) % n_of_rooms);
                            Toast.makeText(getApplicationContext(), "Challenge sent to "+randomPlayerID, Toast.LENGTH_SHORT).show();
                            sendChallenge(randomPlayerID);
                            challenge_player_btn.setEnabled(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Server Error!", Toast.LENGTH_SHORT).show();
                    }
                });
//    }
            }
        });
    }

    private String getMyId(){
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "default@mail.com");
        email = Signin.getUserID(email);
        return email;
    }

    private void challengeAcceptListener(){
        DatabaseReference myPlayerIDRef = roomRef.child(getMyId());
        final boolean[] isListenerSet = {false};


        //if user accepts the challenge, my room's opponent will be updated, which is the player whom I challenged
        myPlayerIDRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListenerSet[0]){
                    //opponent send a message to players document in database,
                    //which we use to know is challenge accepted or declined!
                    String msg = snapshot.child("msg").getValue(String.class);

                    if(msg.equals("accept"))
                    {
                        //if challenge is accepted, wait for the opponentID to be updated in database
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                myPlayerIDRef.child("opponent").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String opponentID = snapshot.getValue(String.class);
                                        Toast.makeText(getApplicationContext(), "Challenge Accepted by "+ opponentID, Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(getApplicationContext(), GameBoard.class);
                                        intent.putExtra("myID", getMyId());
                                        intent.putExtra("opponentID", opponentID);
                                        finish();
                                        startActivity(intent);
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                            }
                        }, 2000);  //2 secs are enough to wait for data to update in database
                    }else{
                        //if challenge declined by user
                        Toast.makeText(getApplicationContext(), "Challenge Declined!", Toast.LENGTH_LONG).show();
                        findViewById(R.id.progress_view).setVisibility(View.GONE);  //remove waitign for player box
                        challenge_player_btn.setEnabled(true);  //button is again available to challenge another player
                        random_challenge_btn.setEnabled(true);  //button is again available to challenge another player
                    }

                    myPlayerIDRef.removeEventListener(this);  //remove the listener for the player, to again start listner for next person
                }else{
                    isListenerSet[0] = true;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }
    private void sendChallenge(String playerID){
        //update the opponent's opponent, which is me (challenger)
        //so that he will receive a challenge, because he has his room listener
        DatabaseReference playerIDRef = roomRef.child(playerID);
        DatabaseReference oppoRef = playerIDRef.child("opponent");
        DatabaseReference timeRef = playerIDRef.child("time");
        DatabaseReference topicRef = playerIDRef.child("topic");

        //lets challenge the player having playerID
        oppoRef.setValue(getMyId());  //who challenged the playerID
        topicRef.setValue(MainActivity.getTopicID());  //set id of topic in which player is challenged
        timeRef.setValue(System.currentTimeMillis());  //when playerID was challenged
        //so who challenged playerID at what time

        //now check if the user accepted the challenge
        findViewById(R.id.progress_view).setVisibility(View.VISIBLE);  //shows that player was challenged, now waiting for the result
        challengeAcceptListener();
    }
}