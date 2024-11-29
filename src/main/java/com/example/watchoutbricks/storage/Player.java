package com.example.watchoutbricks.storage;

import cn.bmob.v3.BmobObject;

//User(name, score), No.1-3 have fixed id
public class Player extends BmobObject {
    private String name;
    private String score;

    public String getName() {
        return name;
    }

    public String getScore() {
        return score;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
