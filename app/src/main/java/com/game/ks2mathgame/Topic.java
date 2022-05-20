package com.game.ks2mathgame;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class Topic {

    public static final int levelsTotal = 5;
    //total five levels for each topic,
    //number of levels are same for each topic so it relates to class not object so its static

    public final String topicName;
    public final int hat;   //this hat is rewarded on level complete
    public final char operator;
    private int level = 1; //by default level is one

    public Topic(String topicName, int hat, char operator){
        this.topicName = topicName;
        this.hat = hat;
        this.operator = operator;
    }

    public void setLevel(int level) {
        if (level <= levelsTotal+1)
            this.level = level;
    }

    public int getLevel(){
        return level;
    }

    public String getAccolade(){
        return "Completed "+levelsTotal+" Levels of "+topicName;
    }

    public boolean isCompleted(){
        return level > levelsTotal;
    }
}
