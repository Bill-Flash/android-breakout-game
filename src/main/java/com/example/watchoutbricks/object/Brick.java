package com.example.watchoutbricks.object;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

import com.example.watchoutbricks.GameView;

import java.util.Random;

public class Brick {
    //basic
    private GameView gameView;
    private int x, y, last;
    private int width, height;
    private Point position;
    private Paint paint;
    private final int margin = 5;
    private final int top = 10;
    private boolean isRandom, isShaking, isDropping;

    //shaking times
    private int times = 0;

    //calculate the touch area
    private Rect rect;

    public Brick(GameView gameView, Point position, boolean[][] map, boolean b){
        this.gameView = gameView;
        width = gameView.getWidth()/map.length;
        height = gameView.getHeight()/map[0].length;
        this.position = position;
        this.isRandom = b;
        isDropping = false;
        isShaking = false;
        initData();
    }

    //initialize the brick
    private void initData(){
        x = position.y*width;
        y = position.x*(height+margin)+top;
        paint = new Paint();
        int py = position.y;
        //fixed bricks initially
        if (isRandom){
            py = new Random().nextInt(5);
        }
        switch (py){
            case (1):
                paint.setColor(Color.rgb(134, 215, 252));
                break;
            case (2):
                paint.setColor(Color.rgb(134, 252, 187));
                break;
            case (3):
                paint.setColor(Color.rgb(226, 252, 134));
                break;
            case (4):
                paint.setColor(Color.rgb(252, 134, 134));
                break;
            case (0):
                paint.setColor(Color.rgb(134, 142, 252));
                break;
        }
        rect = new Rect();
        rect.set(x, y, x+width, y+height);
    }

    //draw
    public void onDraw(Canvas canvas){
        // is chosen to drop
        if (isShaking){
            if (times<20){
                shake();
            }else {
                isShaking = !isShaking;
                isDropping = true;
            }
        }
        RectF rectF = new RectF(rect.left,rect.top,rect.right,rect.bottom);
        canvas.drawRoundRect(rectF, height, height, paint);
    }

    //automatically shake 20 times if been chosen
    private void shake(){
        times++;
        x = x - (-1)^times*3;
        Random r = new Random();
        paint.setColor(Color.rgb(r.nextInt(255),r.nextInt(255),r.nextInt(255)));
        rect.set(x, y, x+width, y+height);
        if (times==20){
            paint.setColor(Color.WHITE);
            x = last;
        }
    }

    //collision function
    public boolean isCollision(Ball ball){
        // return true if is touched
        Rect rectangle = ball.getRect();
        return rectangle.intersects(rectangle, rect);
    }

    //check which ver or hor
    public boolean isVertical(Ball ball) {
        //return true if vertical
        Rect rect1 = new Rect(rect);
        Rect rect2 = ball.getRect();
        rect2.inset(ball.getxDx(), ball.getyDy());
        rect1.intersect(rect2);
        if(rect1.contains(ball.getX(), ball.getY()-ball.getRadius())
                || rect1.contains(ball.getX(), ball.getY()+ball.getRadius())){
            ball.setUp();
            return true;
        }else {
            ball.setRight();
            return false;
        }
    }

    //add row
    public void drop() {
        y+=height+5;
        rect.set(x, y, x+width, y+height);
    }

    //if touches GAMEOVER!
    public boolean isTouchBottom() {
        return ((y+height)>gameView.getHeight());
    }

    //slowly dropping
    public void smallDrop() {
        y+=2;
        rect.set(x, y, x+width, y+height);
    }

    //interface for dropping
    public boolean getDroppinng() {
        return isDropping;
    }

    //interface for start shaking and dropping
    public void setShaking() {
        isShaking = true;
        last = x;
    }
}
