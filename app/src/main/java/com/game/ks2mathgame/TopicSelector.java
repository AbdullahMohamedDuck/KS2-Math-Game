package com.game.ks2mathgame;

import static com.game.ks2mathgame.MainActivity.topics;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.game.ks2mathgame.game.GameBoard;
import com.game.ks2mathgame.visuals.Multiplayer;
import com.game.ks2mathgame.visuals.Story;

public class TopicSelector extends AppCompatActivity {

    private Intent gameIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic_selector);

        TextView mode_label = findViewById(R.id.mode_label);
        mode_label.setText(MainActivity.selectedMode.toString());

        //if player has selected multiplayer mode, we need to get more information, so move the user to multiplayer activity, from where we'll move them to Gameboard
        if (MainActivity.selectedMode == Mode.MULTIPLAYER)
            gameIntent = new Intent(getApplicationContext(), Multiplayer.class);
        else if (MainActivity.selectedMode == Mode.STORY)
            gameIntent = new Intent(getApplicationContext(), Story.class);
        else
            gameIntent = new Intent(getApplicationContext(), GameBoard.class);
    }

    public void addition_click(View view) {
        MainActivity.selectedTopic = topics[0];
        startActivity(gameIntent);
    }

    public void subtraction_click(View view) {
        MainActivity.selectedTopic = topics[1];
        startActivity(gameIntent);
    }

    public void multiplication_click(View view) {
        MainActivity.selectedTopic = topics[2];
        startActivity(gameIntent);
    }

    public void division_click(View view) {
        MainActivity.selectedTopic = topics[3];
        startActivity(gameIntent);
    }

}