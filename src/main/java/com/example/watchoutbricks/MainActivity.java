package com.example.watchoutbricks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watchoutbricks.dialog.LevelDialog;
import com.example.watchoutbricks.inter.Observer;
import com.example.watchoutbricks.service.BGMService;
import com.example.watchoutbricks.service.NotifyingService;
import com.example.watchoutbricks.storage.SaveRank;

import java.util.Map;

import top.androidman.SuperButton;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        Observer{
    private static final String TAG = "MainActivity";
    //view
    SuperButton start, rank;
    ImageButton music;
    ImageView ball;
    TextView hScore;

    //db
    String highest;
    Map<String, String> db;
    private static Context context;

    //music
    Intent bgm;
    private boolean turnOn;

    //animation
    Animation rotate, jump;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //basic
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //music
        bgm = new Intent(this, BGMService.class);
        turnOn = true;
        music = findViewById(R.id.imageButton);
        // for homepage to update the rank in-time.
        SaveRank.addObserver(this, 0);
        context = this;

        //view and listener
        start = findViewById(R.id.start);
        rank = findViewById(R.id.rank);
        hScore = findViewById(R.id.hScore);
        ball = findViewById(R.id.littleBall);
        start.setOnClickListener(this);
        rank.setOnClickListener(this);
        music.setOnClickListener(this);


        // to get the highest score
        setHighest();

        // to stop the notification
        NotifyingService.cleanNotification();

        // to start rotating
        rotate = AnimationUtils.loadAnimation(this,R.anim.infinite_rotate);
        jump = AnimationUtils.loadAnimation(this, R.anim.little_jump);
        Interpolator interpolator = new CycleInterpolator(0.5f);
        rotate.setInterpolator(interpolator);
        jump.setInterpolator(interpolator);
        ball.startAnimation(rotate);
        start.startAnimation(jump);
        rank.startAnimation(jump);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // if leave to notify player
        NotifyingService.addNotification(24 * 60 * 60 * 1000);
        if (turnOn){
            this.stopService(bgm);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setHighest();
        start.setClickable(true);
        rank.setClickable(true);
        if (turnOn){
            this.startService(bgm);
        }
    }

    public static Context getContext(){
        return context;
    }

    public void onClick(View view) {
        // click Listeners
        switch (view.getId()) {
            case R.id.start:
                view.startAnimation(jump);
                onClickStart(view);
                view.setClickable(false);
                break;
            case R.id.rank:
                view.startAnimation(jump);
                onClickRank(view);
                view.setClickable(false);
                break;
            case R.id.imageButton:
                onClickMusic(view);
                break;
            default:
                break;
        }

    }

    //for music
    private void onClickMusic(View view) {
        if (turnOn){
            this.stopService(bgm);
            music.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_volume_off_24));
        }else {
            this.startService(bgm);
            music.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_volume_up_24));
        }
        turnOn = !turnOn;
        view.startAnimation(AnimationUtils.loadAnimation(this,R.anim.shake));
    }

    //intents for jumping
    private void onClickRank(View view) {
        startActivity(new Intent(this, Rank.class).putExtra("music", turnOn));
        //jump to the Rank page
    }

    private void onClickStart(View view) {
        LevelDialog dialog = new LevelDialog(context, turnOn);
        dialog.show();
        //jump to the Game page
    }

    //update score
    private void setHighest(){
        db = SaveRank.retain(this);
        highest = db.get("fScore");

        // the string value gets and adds the score
        if (highest==null){
            hScore.setText(getResources().getText(R.string.highest));
        }else{
            hScore.setText(getResources().getText(R.string.highest)+" "+highest);
        }
    }

    //oBSERver parttern to update score
    @Override
    public void update() {
        setHighest();
    }

}