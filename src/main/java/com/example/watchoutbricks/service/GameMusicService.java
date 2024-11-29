package com.example.watchoutbricks.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.watchoutbricks.R;

//Game BGM
public class GameMusicService extends Service{
    private MediaPlayer mediaPlayer;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //start
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaPlayer==null){
            mediaPlayer = MediaPlayer.create(this, R.raw.game);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    //end
    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}

