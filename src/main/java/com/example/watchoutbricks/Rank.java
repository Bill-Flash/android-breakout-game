package com.example.watchoutbricks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watchoutbricks.inter.Observer;
import com.example.watchoutbricks.service.BGMService;
import com.example.watchoutbricks.storage.SaveRank;

import java.util.Map;

public class Rank extends AppCompatActivity implements Observer{
    //music
    Intent bgm;
    boolean turnOn;
    //names
    String[] medals;

    //views
    TextView gold;
    TextView silver;
    TextView bronze;

    TextView first;
    TextView second;
    TextView third;

    TextView firstS;
    TextView secondS;
    TextView thirdS;

    ImageView trophy;

    //db
    Map<String, String> db;

    //animation
    Animation jump, rotate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bgm = new Intent(this, BGMService.class);
        turnOn = getIntent().getBooleanExtra("music", true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rank);
        SaveRank.addObserver(this, 1);
        SaveRank.query("bb36837e4d", 0);
        SaveRank.query("cd22d93ff7", 1);
        SaveRank.query("93c2b14458", 2);

        first = findViewById(R.id.first);
        second = findViewById(R.id.second);
        third = findViewById(R.id.third);
        firstS = findViewById(R.id.fScore);
        secondS = findViewById(R.id.sScore);
        thirdS = findViewById(R.id.tScore);
        trophy = findViewById(R.id.trophy);

        // db to store the score of players
        db = SaveRank.retain(this);

        // set medals' name
        setMedals();
        // set Users' name
        setNames();
        // set Users' score
        setScores();

        // to start rotating
        rotate = AnimationUtils.loadAnimation(this, R.anim.little_rotate);
        jump = AnimationUtils.loadAnimation(this, R.anim.little_jump);
        Interpolator interpolator = new CycleInterpolator(0.5f);
        jump.setInterpolator(interpolator);
        trophy.startAnimation(jump);
        
    }

    private void setScores() {
        // set the scores
        firstS.setText(db.get("fScore"));
        secondS.setText(db.get("sScore"));
        thirdS.setText(db.get("tScore"));
    }

    private void setNames() {
        // set the names
        first.setText(db.get("fName"));
        second.setText(db.get("sName"));
        third.setText(db.get("tName"));

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (turnOn){
            this.startService(bgm);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (turnOn){
            this.stopService(bgm);
        }
    }

    private void setMedals(){
        gold = findViewById(R.id.gold);
        silver = findViewById(R.id.silver);
        bronze = findViewById(R.id.bronze);
        //get Resource from the string-array
        medals = getResources().getStringArray(R.array.medal);

        gold.setText(medals[0]);
        silver.setText(medals[1]);
        bronze.setText(medals[2]);
    }

    @Override
    public void update() {
        // db to store the score of players
        db = SaveRank.retain(this);

        // set medals' name
        setMedals();
        // set Users' name
        setNames();
        // set Users' score
        setScores();
    }

    public void rotateAnimation(View v) {
        trophy.startAnimation(rotate);
    }
}