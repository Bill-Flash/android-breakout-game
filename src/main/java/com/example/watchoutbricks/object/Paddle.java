package com.example.watchoutbricks.object;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.example.watchoutbricks.GameView;


public class Paddle {
    //basic
    private int x, y;
    private GameView gameView;
    private Rect rectangle;
    private int xSpeed;

    Paint paint = new Paint();
    Bitmap bmp;

    public Paddle(GameView gameView, Rect rectangle, Bitmap bmp){
        this.gameView = gameView;
        this.rectangle = rectangle;
        this.bmp = bmp;
        paint.setColor(Color.YELLOW);
        initPaddle();
    }

    //initialize the paddle
    private void initPaddle(){
        x = (gameView.getWidth() - rectangle.width())/2;
        y = gameView.getHeight() - rectangle.height();
        xSpeed = gameView.getWidth()/16;
        rectangle.set(x, y, x+ rectangle.width(), y+ rectangle.height());
    }

    //left or right
    public void move(int i){
        if (!checkBoundary(i)){
            x += i*xSpeed;
            rectangle.set(x, y, x+rectangle.width(), y+rectangle.height());
        }
    }

    //boundary in case of out problem
    private boolean checkBoundary(int i){
        return (x + i*xSpeed < 0 || x + rectangle.width() + i*xSpeed > gameView.getWidth());
    }

    //draw paddle
    public void onDraw(Canvas canvas){
        RectF rectF = new RectF(rectangle.left,rectangle.top,rectangle.right,rectangle.bottom);
        canvas.drawBitmap(bmp, rectF.left, rectF.top - gameView.getHeight()/8, paint);
//        canvas.drawRoundRect(rectF,20.5f, 10.5f, paint);
    }

    //get the paddle
    public Rect getRectangle(){
        return new Rect(rectangle);
    }
}
