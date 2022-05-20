package com.game.ks2mathgame.visuals;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.game.ks2mathgame.MainActivity;
import com.game.ks2mathgame.R;
import com.game.ks2mathgame.game.GameBoard;

public class Story extends AppCompatActivity {
    //unicodes for some images on button
    private final String
            lock = "\uD83D\uDD12",  //this shows lock image on  using unicode
            unlock = "\uD83D\uDD13";  //this shows unlocked image on button  using unicode

    public static int selectedLevel = 1;  //by default selected level is one
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        TextView topic_label = findViewById(R.id.topic_label);
        topic_label.setText(MainActivity.selectedTopic.topicName);

        int unlockedLevel = MainActivity.selectedTopic.getLevel();

        LinearLayout level_layout = findViewById(R.id.level_layout);
        for(int i = 0; i < level_layout.getChildCount(); i++){
            Button level_btn = (Button) level_layout.getChildAt(i);
            /*create a string according to the current level of selected level from main activity
            * set unlock image if current level is unlocked
            * set lock image if current level is locked
            * */

            String status = "Level " + (i+1) + (i < unlockedLevel? unlock : lock);
            //the text becomes -> Level 1 unlock
            level_btn.setText(status);

            //set click listeners for these buttons, to check which level does user wanted to play
            int finalI = i;
            if(i < MainActivity.selectedTopic.getLevel())  //level is only clickable if is unlocked
                level_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), GameBoard.class);
                        selectedLevel = finalI +1;
                        startActivity(intent);
                    }
                });
        }
    }
}