package com.example.watchoutbricks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watchoutbricks.inter.EndGame;
import com.example.watchoutbricks.inter.SoundEffect;
import com.example.watchoutbricks.service.GameMusicService;

import java.util.Timer;
import java.util.TimerTask;

import top.androidman.SuperButton;

public class Game extends AppCompatActivity implements View.OnClickListener, EndGame,
        SoundEffect, SensorEventListener {
    //sensor part
    private SensorManager sensorManager;
    private float timeStamp;
    private float mAngle[] = new float[3];
    private static final float NS2S = 1.0f / 1000000000.0f;

    //music
    private Intent bgm;
    private boolean turnOn, shoot;
    //time&score
    private final int UPDATE = 1;
    private static final String TAG = "Game";
    // instantiate screen width and height
    int xWidth, xHeight;
    // Buttons
    SuperButton stop;
    ImageButton music;
    //Game view
    GameView gameView;
    boolean isHard = false;
    //Level
    private int level;
    //time&score
    TextView time, score, levelView;
    private Handler handler;
    private Timer timer;
    private TimerTask timerTask;
    //music
    private SoundPool soundPool;
    private int voiceId1, voiceId2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //basic
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.game);

        //level
        level = getIntent().getIntExtra("level", 1);
        if (level==3){
            isHard = true;
        }

        //view&listener
        time = findViewById(R.id.textTime);
        score = findViewById(R.id.textScore);
        stop = findViewById(R.id.stop);
        music = findViewById(R.id.music);
        levelView = findViewById(R.id.textLevel);
        stop.setOnClickListener(this);
        music.setOnClickListener(this);



        // to adopt SDK version for music and sound effect
        bgm = new Intent(this, GameMusicService.class);
        turnOn = getIntent().getBooleanExtra("music", true);
        if (Build.VERSION.SDK_INT >= 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setMaxStreams(1);
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            builder.setAudioAttributes(attrBuilder.build());
            soundPool = builder.build();
        } else {
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
        voiceId1 = soundPool.load(this, R.raw.shoot1, 1);
        voiceId2 = soundPool.load(this, R.raw.shoot2, 1);



        //Initiate the game
        initData();
        //Time&Score
        getTimeAndScore();
        if (isHard){
            //sensor
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }

    }

    private void getTimeAndScore(){
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case UPDATE:
                        switch (msg.what) {
                            case UPDATE:
                                updateScore();
                                updateTime();
                                break;
                            default:
                                break;
                        }
                }
            }
        };
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(UPDATE);
            }
        };
        timer.schedule(timerTask, 100, 500);
    }


    private void updateScore() {
         score.setText("Score: "+gameView.getScore());
    }
    private void updateTime(){
        int t = gameView.getTime();
        int hour = t/3600;
        int min = (t%3600)/60;
        int sec = t%60;
        time.setText(String.format("Time:  %02d:%02d:%02d", hour, min, sec));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (turnOn){
            this.startService(bgm);
        }else{
            music.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_volume_off_24));
        }
        if (isHard){
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR),SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void initData(){
        final int[] layoutHeight = {0};
        // get the screen height
        int height = getScreenHeight(this);
        int width = getScreenWidth(this);
        RelativeLayout layout = findViewById(R.id.layoutMenu);
        layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                layoutHeight[0] = layout.getMeasuredHeight();
                // set the game width
                xWidth = width;
                // set the game height
                xHeight = (int) (height - layoutHeight[0]*1.5);
                initView();
            }
        });

    }
    // initiate the game view
    @SuppressLint("SetTextI18n")
    public void initView(){
        // get, instantiate and set the area of game view
        LinearLayout layoutGame = (LinearLayout) findViewById(R.id.layoutGame);
        gameView = new GameView(this);
        gameView.setLevel(level);
        gameView.setEndGame(this);
        gameView.setSoundEffect(this);
        gameView.setLayoutParams(new ViewGroup.LayoutParams(xWidth,xHeight));
        layoutGame.removeAllViews();
        layoutGame.addView(gameView);

        switch (level){
            case 1:
                levelView.setText(getResources().getString(R.string.level)+": "+getResources().getString(R.string.simple));
                break;
            case 2:
                levelView.setText(getResources().getString(R.string.level)+": "+getResources().getString(R.string.normal));
                break;
            default:
                levelView.setText(getResources().getString(R.string.level)+": "+getResources().getString(R.string.hard));
        }
    }


    // to get the height of the screen, get from the utils
    public  static int getScreenHeight(@NonNull Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.
                WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }
    // to get the height of the screen, get from the utils
    public  static int getScreenWidth(@NonNull Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.
                WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to prevent double Alerts
        if (gameView.gameThread.getPause()&&gameView.gameThread.getRunning()){
            stop(stop);
        }
        if (turnOn){
            this.stopService(bgm);
        }
        if (isHard){
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stop:
                stop(v);
                break;
            case R.id.music:
                clickMusic(v);
                break;
            default:
                break;
        }
    }

    //stop and start music and effect
    private void clickMusic(View v) {
        if (turnOn){
            this.stopService(bgm);
            music.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_volume_off_24));
        }else {
            this.startService(bgm);
            music.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_volume_up_24));
        }
        turnOn = !turnOn;
        v.startAnimation(AnimationUtils.loadAnimation(this,R.anim.shake));
    }

    //sound effect
    @Override
    public void shoot() {
        if (!turnOn){ return; }
        int voiceId;
        if (shoot){
            voiceId = voiceId1;
        }else {
            voiceId = voiceId2;
        }
        shoot =!shoot;
        soundPool.play(voiceId, 1, 1, 1, 0, 1);
    }

    // to stop the game and do the next
    private void stop(View v) {
        v.setClickable(false);
        gameView.pause();
        AlertDialog.Builder builder =new AlertDialog.Builder(v.getContext());
        builder.setTitle("Notice")
                .setMessage("You have stopped it!")
                .setCancelable(false)
                .setPositiveButton("Resume", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        gameView.resume();
                        v.setClickable(true);
                    }
                })
                .setNeutralButton("Homepage", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        v.setClickable(true);
                        endGame();
                    }
                })
                .setNegativeButton("Restart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        gameView.restart();
                        v.setClickable(true);
                    }
                })
                .create()
                .show();
    }

    //end game
    @Override
    public void endGame() {
        this.finish();
    }

    //to Rank
    @Override
    public void endToRank() {
        this.finish();
        startActivity(new Intent(this, Rank.class));
    }

    // to left or right
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR){
            if (timeStamp != 0 && gameView.gameThread.getRunning()&&(gameView.gameThread.getPause()
                    &&gameView.gameThread.getStart())){
                final float dT = (event.timestamp - timeStamp) * NS2S;
                if (event.values[0]*mAngle[0] < 0){
                    mAngle[0] = 0;
                }
                // for minus precision
                mAngle[0] += event.values[0] * dT- 0.00528;
                mAngle[1] += event.values[1] * dT;
                mAngle[2] += event.values[2] * dT;

                float angelX = (float) Math.toDegrees(mAngle[0]);
                float angelY = (float) Math.toDegrees(mAngle[1]);
                float angelZ = (float) Math.toDegrees(mAngle[2]);

                if (angelX>2){
                    gameView.moveRight();
                    mAngle[0] = 0;
                }else if (angelX<-3){
                    gameView.moveLeft();
                    mAngle[0] = 0;
                }

            }
            timeStamp = event.timestamp;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}

