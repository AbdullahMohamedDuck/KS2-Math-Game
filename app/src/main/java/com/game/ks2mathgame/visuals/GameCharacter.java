package com.game.ks2mathgame.visuals;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.game.ks2mathgame.MainActivity;
import com.game.ks2mathgame.R;
import com.game.ks2mathgame.Topic;

import java.util.ArrayList;

public class GameCharacter extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_character);

        ImageView equippedHat = findViewById(R.id.equipped_hat);
        TextView accolade = findViewById(R.id.accolade);

        //if user has equipped something, load it from saved memory in shared references
        SharedPreferences sharedPreferences = getSharedPreferences("character", MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        String accoladeSaved = sharedPreferences.getString("accolade", "Beginner");
        int hatSaved = sharedPreferences.getInt("hat", 0);

        Spinner accoladeSpinner = findViewById(R.id.accoladeSpinner);
        ArrayList<String> availableAccolades = new ArrayList<String>();
        availableAccolades.add("Beginner");

        //add the earned accolades and hats to the Array String to use in spinner
        LinearLayout mini_hats_layout = findViewById(R.id.mini_hats_layout);
        int i = 0;  //for counting topics
        for(Topic topic : MainActivity.topics){
            //EVERY CHILD OF MINI HATS LAYOUT IS AN IMAGEVIEW
            ImageView hat = (ImageView) mini_hats_layout.getChildAt(i++);

            //if user has unlocked this hat and level, the item is clickable and can be equipped
            if (topic.isCompleted())
            {
                availableAccolades.add(topic.getAccolade());  //add the accolade to available accolades to be shown in spinner
                //make the hat image opaque, so user learns that it is available for equipping
                hat.setAlpha(1.0f);
                hat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        equippedHat.setImageResource(topic.hat);
                        spEditor.putInt("hat", topic.hat);  //save in shared preferences
                        spEditor.apply();
                    }
                });
            }
        }
        //end of preparing earned rewards

        //Prepare spinner for selecting an accolade
        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(),
                android.R.layout.simple_spinner_item, availableAccolades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accoladeSpinner.setAdapter(adapter);
        accoladeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0){
                    String equippedAccolade = availableAccolades.get(i);  //select which accolade is selected
                    accolade.setText(equippedAccolade);  //set the selected accolade
                    spEditor.putString("accolade", equippedAccolade);  //save in shared prefernces
                    spEditor.apply();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        }); //end of spinner click listener


        //set the saved equipment to character
        Toast.makeText(getApplicationContext(), ""+accoladeSaved, Toast.LENGTH_LONG).show();
        accolade.setText(accoladeSaved);
        if (hatSaved != 0){
            equippedHat.setImageResource(hatSaved);
        }
    }  //on create ends here!
}