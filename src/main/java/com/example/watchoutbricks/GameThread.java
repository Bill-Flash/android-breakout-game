package com.example.watchoutbricks;

import android.graphics.Canvas;

/**
 * keep updating, part of copied from lecture note and http://edu4java.com/en/androidgame/androidgame2.html
 */
public class GameThread extends Thread{
    static final long FPS = 30;
    private GameView view;
    private boolean running, start, pause;


    public GameThread(GameView view){
        this.view = view;
        running = false;
        pause = true;
        start = false;
    }
    public boolean getRunning(){
        return running;
    }
    public boolean getPause(){
        return pause;
    }
    public void setRunning(boolean running){
        this.running = running;
    }
    public void setPause(boolean pause){
        this.pause = pause;
    }
    public void setStart(boolean start) {
        this.start = start;
    }
    public boolean getStart(){
        return start;
    }

    @Override
    public void run() {
        long ticksPS = 1000/FPS;
        long time = System.currentTimeMillis();
        long startTime;
        long sleepTime;
        while (running){
            // to pause the game and init the game
            while(!pause || !start){
                try {
                    sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Canvas c = null;
            startTime = System.currentTimeMillis();
            try {
                c = view.getHolder().lockCanvas();
                synchronized (view.getHolder()) {
                    view.update();
                    view.draw(c);
                }
            }finally {
                if (c != null){
                    view.getHolder().unlockCanvasAndPost(c);
                }
            }
            sleepTime = ticksPS - (System.currentTimeMillis()-startTime);
            try {
                if (sleepTime > 0){
                    sleep(sleepTime);
                }else {
                    sleep(10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
