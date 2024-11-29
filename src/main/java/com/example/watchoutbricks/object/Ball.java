package com.example.watchoutbricks.object;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.example.watchoutbricks.GameView;

public class Ball{
    // its coordination
    private int x = 0, y = 0, xDx = 15, yDy = 15;
    private int radius;
    private GameView gameView;
    private Bitmap bmp;
    boolean begin;
    private boolean alive = true;
    boolean up = true, right = true; //to ensure whether it is positive with coordinate increasing
    Paint paint = new Paint();
    //constructor
    public Ball(GameView gameView, int radius, Bitmap bmp){
        this.gameView = gameView;
        this.radius = radius;
        this.bmp = bmp;
        begin = false;
        initBall();
//        paint.setAntiAlias(true);
    }
    //initialize ball
    private void initBall(){
        x = (gameView.getWidth())/2;
        y = gameView.getHeight() - radius - 20;
        xDx = gameView.getWidth()/110;
        yDy = gameView.getHeight()/110;
        paint.setColor(Color.rgb(245,209,169));
    }
    //get properties
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public int getRadius(){
        return radius;
    }
    public int getxDx() {
        return xDx;
    }
    public int getyDy() {
        return yDy;
    }
    public Rect getRect(){
        return new Rect(x-radius, y-radius,
                x+radius, y+radius);
    }
    public boolean isAlive(){
        return alive;
    }

    //update the physical properties
    public void update(){
        // with walls
        if (x > gameView.getWidth() - radius - xDx) {
            setRight();
        }
        if (x - radius + xDx< 0) {
            setRight();
        }
        // game over when it touches the base line
        if (y > gameView.getHeight() - radius - yDy) {
            alive = false;
        }
        if (y - radius + yDy<= 0) {
            setUp();
        }
        x += xDx;
        y += yDy;
    }
    //set direction
    protected boolean setRight(){
        right = !right;
        xDx = -xDx;
        return right;
    }
    public boolean setUp(){
        up = !up;
        yDy = -yDy;
        return up;
    }

    //with paddle
    public void bounce(Paddle p) {
        Rect r = p.getRectangle();
        if(r.intersects(getRect(), r)) {
            //1/3 left paddle
            Rect leftPaddle = new Rect(r.left,r.top,r.width()/3+r.left,r.bottom);
            Rect rightPaddle = new Rect(((2*r.width())/3)+r.left,r.top,r.right,r.bottom);
            if(r.intersects(getRect(), leftPaddle)) {
                if(right) {//the left one third paddle touch the ball from left
                    //111
                    angle(true);
                }else {
                    angle(false);
                }
            }else if(r.intersects(getRect(), rightPaddle)) {
                if(!right) {
                    //111
                    angle(true);
                }else {
                    angle(false);
                }
            }
            goUp();
        }
    }

    //up if touch with paddle
    private boolean goUp(){
        up = true;
        yDy = -Math.abs(yDy);
        return up;
    }

    //draw
    public void onDraw(Canvas canvas){
        canvas.drawBitmap(bmp,x-radius, y-radius, paint);
//        canvas.drawCircle(x , y, radius, paint);
    }

    //for start shoot
    private double getV() {
        return Math.sqrt(xDx*xDx+yDy*yDy);
    }
    private double getDegree() {
        double tgA = ((float) Math.abs(yDy)) / ((float)Math.abs(xDx));
        return Math.toDegrees(Math.atan(tgA));
    }

    // the fingerX,Y
    public void setV(float dx, float dy){
        float v = (float) getV();
        v = (float) (v/1.5);
        float x1 = dx - this.x;
        int i = 1;
        if (x1<0){
            i *= -1;
        }
        float y1 = this.y - dy;
        this.xDx = (int) x1;
        this.yDy = (int) y1;
        double degree = getDegree();
        double r = Math.toRadians(degree);
        this.xDx = (int) (Math.cos(r) * v)*i;
        this.yDy = (int) Math.abs(Math.sin(r) * v)*-1;
    }
    public void setAlive(boolean b) {
        this.alive = b;
    }

    //from left bounce at left 1/3 paddle, angle will larger than before;
    //from right bounce at right 1/3 paddle, angle will larger than before;
    //from left bounce at right 1/3 paddle, angle will smaller than before;
    //from right bounce at left 1/3 paddle, angle will smaller than before;
    public void angle(boolean a) {//change the angular amount, but the scalar of the velocity doesn't change
        double minD = 30;		//the minimum of the degree
        double maxD = 70;		//the maximum of the degree
        double degree = getDegree();
        double gama = (Math.random()*20)+(double)10;		//the interval of changing angular [10,30]
        if(a!=true) {
            degree-=gama;
        }else {
            degree+=gama;
        }

        if(degree<minD) { //to prevent it from exceed minD or MaxD
            degree = minD;
        }else if(degree>maxD){
            degree = maxD;
        }
        double r = Math.toRadians(degree);
        double flag = 1;
        if (xDx<0){
            flag = -1;
        }
        double v = getV();
        yDy = (int) Math.floor(v*Math.sin(r));
        xDx = (int) Math.ceil(v*Math.cos(r)*flag);
        Log.d("TAG", "setV: "+getV()+"  "+v);

    }
}
