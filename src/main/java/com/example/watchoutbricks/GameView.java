package com.example.watchoutbricks;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.watchoutbricks.dialog.RecordDialog;
import com.example.watchoutbricks.inter.EndGame;
import com.example.watchoutbricks.inter.SoundEffect;
import com.example.watchoutbricks.object.Ball;
import com.example.watchoutbricks.object.Brick;
import com.example.watchoutbricks.object.Paddle;
import com.example.watchoutbricks.storage.SaveRank;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * part of copied from http://edu4java.com/en/androidgame/androidgame2.html
 */
public class GameView extends SurfaceView{
    //thread
    public GameThread gameThread;

    //game manipulation & sound: interface
    public EndGame endGame;
    public SoundEffect soundEffect;

    //holder for drawing
    SurfaceHolder holder;

    //objects in game
    Ball ball;
    Paddle paddle;
    private boolean[][] maps;
    private List<Brick> bricks = null, droppingBricks = null;

    //time
    private long lastClick;
    private int currentScore;
    private int time, lastTime;
    private long startTime, pauseStartTime, pauseTime, times;

    //level
    private int level;
    private boolean canTouch = true;

    //the speed for adding a row
    private static final int FREQUENCY = 10;
    //path
    Paint pathPaint;
    int backgroundColor;
    //BitMap
    Bitmap bitmapBall, bitmapPaddle;

    public GameView(Context context) {
        super(context);
        // get Resource
        pathPaint = new Paint();
        pathPaint.setColor(Color.argb(100,95,45,45));
        pathPaint.setAntiAlias(true);
        pathPaint.setStrokeWidth(10.0f);
        pathPaint.setPathEffect(new DashPathEffect(new float[]{4,4}, 0));
        backgroundColor = getResources().getColor(R.color.match4, null);
        // get BitMap: once is OK
        bitmapBall = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
        bitmapPaddle = BitmapFactory.decodeResource(getResources(), R.drawable.paddle);

        //thread to update and draw
        gameThread = new GameThread(this);

        //draw
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (!gameThread.getRunning()){
                    initView();
                    // triggered by the UP ACTION to start shooting
                    gameThread.setRunning(true);
                    gameThread.start();
                }else if (!gameThread.getPause()){// when the game re-enter
                    fresh();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                pause();
            }
        });
    }

    // new data
    private void initView() {
        times = 0;
        time = 0;
        startTime = 0;
        currentScore = 0;
        maps = new boolean[5][30];
        randomCreateBrick(10);
        bitmapBall = changeBitmapSize(bitmapBall, getWidth() / 40, getWidth() / 40);
        ball = new Ball(this, getWidth() / 80,bitmapBall);
        bitmapPaddle = changeBitmapSize(bitmapPaddle, getWidth()/8, 400);
        Rect rectangle = new Rect();
        rectangle.set(0, 0, getWidth() / 8, 25);
        paddle = new Paddle(this, rectangle, bitmapPaddle);
        droppingBricks = new ArrayList<>();
        fresh();
    }

    //changed by my
    //new Width and Height is the size I want, by using API
    //reference from https://blog.csdn.net/nbaqqqq/article/details/78470870
    private Bitmap changeBitmapSize(Bitmap bitmap, int newWidth, int newHeight) {
        // bitmap size
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scaleWidth=((float) newWidth)/width;
        float scaleHeight=((float) newHeight)/height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);

        bitmap=Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,true);
        bitmap.getWidth();
        bitmap.getHeight();
        return bitmap;
    }

    // refresh the view when pause or enter the activity
    private void fresh(){
        Canvas c = holder.lockCanvas();
        draw(c);
        holder.unlockCanvasAndPost(c);
    }

    //start path
    private void drawPath(float stopX, float stopY){
        Canvas c = holder.lockCanvas();
        draw(c);
        c.drawLine((float) ball.getX(),(float) ball.getY(),stopX,stopY, pathPaint);
        holder.unlockCanvasAndPost(c);
    }

    //initialize the data
    private void initData(){
        startTime = System.currentTimeMillis();
        lastTime = 0;
        pauseTime = 0;
        pauseStartTime = 0;
        gameThread.setStart(true);
        times = 0;
    }

    //initial bricks
    private void randomCreateBrick(int j) {
        // to produce j different location bricks
        bricks = new ArrayList<>();
        Random rdn = new Random();
        Point[] positions = new Point[j];
        int count = 0;
        while (count < j) {
            int num = rdn.nextInt((maps.length * maps[0].length) / 4);
            boolean flag = true;
            for (int i = 0; i < count; i++) {
                if (num == (positions[i].x * maps.length + positions[i].y)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                positions[count] = new Point(num / maps.length, num % maps.length);
                count++;
            }
        }
        for (Point p : positions
        ) {
            bricks.add(createBricks(p, false));
        }
    }

    //the top new bricks
    private void randomAddTempBrick(int j) {
        // to produce the new bricks from the top
        Random rdn = new Random();
        Point[] positions = new Point[j];
        int count = 0;
        while (count < j) {
            int num = rdn.nextInt((maps.length));
            boolean flag = true;
            for (int i = 0; i < count; i++) {
                if (num == (positions[i].x * maps.length + positions[i].y)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                positions[count] = new Point(num / maps.length, num % maps.length);
                count++;
            }
        }
        for (Point p : positions
        ) {
            bricks.add(createBricks(p, true));
        }
    }

    //create the bricks
    private Brick createBricks(Point point, boolean b) {
        return new Brick(this, point, maps, b);
    }

    //collision and move function are contained here
    protected void update() {
        if (ball.isAlive()) {
            //two kinds of bricks to update
            for (int i = bricks.size() - 1; i >= 0; i--) {
                Brick brick = bricks.get(i);
                if (brick.isCollision(ball)) {
                    ball.setUp();
                    bricks.remove(brick);
                    currentScore += 20;
                    soundEffect.shoot();
                    break;
                }
                if (brick.isTouchBottom()){
                    ball.setAlive(false);
                }
            }

            switch (level){
                //hard
                case 3:
                case 2:
                    for (int i = 0; i < droppingBricks.size(); i++) {
                        Brick brick = droppingBricks.get(i);
                        if (brick.isCollision(ball)) {
                            ball.setUp();
                            droppingBricks.remove(brick);
                            currentScore += 20;
                            soundEffect.shoot();
                            break;
                        }
                        if (brick.isTouchBottom()){
                            ball.setAlive(false);
                        }
                    }
                    //dropping
                    times++;
                    if (times%2==0){
                        smallDrop();
                    }
                    // 3sec 1 brick = 100 to create a dropping 500 = 15s
                    if (times%500==0){
                        Random random = new Random();
                        Brick b = bricks.remove(random.nextInt(bricks.size()));
                        droppingBricks.add(b);
                        b.setShaking();
                    }
                //normal
                case 1:
                    // every 10 seconds create new bricks
                    int period = time/FREQUENCY;
                    if (period!=lastTime){
                        lastTime = period;
                        moveBricks();
                        randomAddTempBrick(maps.length);
                    }
                    break;
            }

            //check ball and update
            ball.bounce(paddle);
            ball.update();


        } else {
            // gameover!
            pause();
            gameThread.setStart(false);
            getHandler().post(() -> createDialog());
        }
    }

    //for the dropping bricks
    private void smallDrop() {
        for (int i = 0; i < droppingBricks.size(); i++) {
            Brick brick = droppingBricks.get(i);
            if (brick.getDroppinng()){
                brick.smallDrop();
            }
        }
    }

    //iterate the bricks
    private void moveBricks() {
        for (int i = bricks.size() - 1; i >= 0; i--) {
            Brick brick = bricks.get(i);
            brick.drop();
        }
    }

    //game is pause
    public void pause() {
        if (gameThread.isAlive()) {
            gameThread.setPause(false);
            pauseStartTime = System.currentTimeMillis();
        }
    }

    //restart it
    public void restart() {
        if (gameThread.isAlive()) {
            gameThread.setPause(true);
            gameThread.setStart(false);
        }
        initView();
    }

    // resume the game
    public void resume() {
        if (gameThread.isAlive()) {
            gameThread.setPause(true);
            pauseTime += System.currentTimeMillis()-pauseStartTime;
        }
    }

    //draw the view
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor( backgroundColor);
            ball.onDraw(canvas);
            paddle.onDraw(canvas);
            for (Brick brick : bricks) {
                brick.onDraw(canvas);
            }
            for (Brick brick :
                    droppingBricks) {
                brick.onDraw(canvas);
            }
            if (!gameThread.getStart()){
                TextPaint paint = new TextPaint();
                paint.setColor(getResources().getColor(R.color.purple_500));
                paint.setTextSize(100);
                paint.setAntiAlias(true);
                pathPaint.setStrokeWidth(10.0f);
                String hint = getResources().getString(R.string.hint);
                String hint1 = "";
                if (!canTouch){
                    hint1 = getResources().getString(R.string.shake);
                }
                canvas.drawText(hint, getWidth()/5,
                        getHeight()/3, paint);
                canvas.drawText(hint1, getWidth()/4,
                        getHeight()/2, paint);
            }
        }
    }

    //touch mode
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        if (!gameThread.getStart()){
            float y = event.getY();
            if (event.getAction() == MotionEvent.ACTION_MOVE){
                drawPath(x,y);
            }
            if (event.getAction() == MotionEvent.ACTION_UP){
                initData();
                synchronized (getHolder()){
                    ball.setV(x,y);
                }
                gameThread.setStart(true);
            }
        }else if (canTouch){
            // control the minimal interval for player
            if (System.currentTimeMillis() - lastClick > 50) {
                lastClick = System.currentTimeMillis();
                synchronized (getHolder()) {
                    if (x <= getWidth() / 2) {
                        moveLeft();
                    } else {
                        moveRight();
                    }
                }
            }
        }
        return true;
    }

    // left or right
    public void moveLeft(){
        paddle.move(-1);
    }
    public void moveRight(){
        paddle.move(1);
    }

    // if gameover the dialog will been shown
    public void createDialog() {
        if (SaveRank.toRecord(currentScore, getContext())) {
            RecordDialog dialog = new RecordDialog(this, getContext(), new RecordDialog.DataListener() {
                @Override
                public void record(String name, boolean flag) {
                    if (flag){
                        String message = SaveRank.recordScore(currentScore, name, getContext());
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                        resume();
                        gameThread.setRunning(false);
                        endGame.endToRank();
                    }else {
                        Toast.makeText(getContext(), "Invalid username!!", Toast.LENGTH_LONG).show();
                        restart();
                    }
                }
            });
            dialog.show();
        }else{
            AlertDialog.Builder builder =new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.over)
                    .setCancelable(false)
                    .setPositiveButton(R.string.restart, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            restart();
                        }
                    })
                    .setNegativeButton(R.string.homepage, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            resume();
                            gameThread.setRunning(false);
                            endGame.endGame();
                        }
                    })
                    .create()
                    .show();
        }
    }

    //interface for callback the activity
    public void setEndGame(EndGame endGame){
        this.endGame = endGame;
    }
    public void setSoundEffect(SoundEffect soundEffect){
        this.soundEffect = soundEffect;
    }

    //timer for activity to get
    public int getScore() {
        return currentScore;
    }
    public int getTime() {
        if (gameThread.getPause()&&gameThread.getStart()){
            time = (int) ((System.currentTimeMillis()-startTime-pauseTime)/1000);
        }
        return time;
    }

    public void setLevel(int level){
        this.level = level;
        if (level==3){
            canTouch = false;
        }
    }

}
