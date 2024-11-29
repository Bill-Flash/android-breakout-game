package com.example.watchoutbricks.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.watchoutbricks.inter.Observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

// as a db to store and restore
// also check the score and record the new high mark
public class SaveRank {
    private static final String TAG = "SaveRank";
    private static Player[] players = new Player[3];
    private static List<Observer> observers = new ArrayList<>();

    // by observer pattern
    public static void addObserver(Observer observer, int index){
        observers.add(observer);
    }
    //update
    public static void notifyObserver(){
        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).update();
        }
    }

    // use sharedPreference for saving in the local
    public static void toSave(Context context, String fName, String sName,
                              String tName, String fScore, String sScore, String tScore){
        SharedPreferences sp = context.getSharedPreferences("sp", Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        // Creating an editor to edit
        SharedPreferences.Editor ed = sp.edit();
        onUpdate(fName, fScore, "bb36837e4d", context);
        onUpdate(sName, sScore, "cd22d93ff7", context);
        onUpdate(tName, tScore, "93c2b14458", context);

        ed.putString("fName" , players[0].getName());
        ed.putString("sName" , players[1].getName());
        ed.putString("tName" , players[2].getName());
        ed.putString("fScore", players[0].getScore());
        ed.putString("sScore", players[1].getScore());
        ed.putString("tScore", players[2].getScore());

        // remember to commit it
        ed.commit();


    }

    // the instruction from the documentation of Bomb website: http://doc.bmob.cn/data/android/develop_doc/
    // if query successfully update the observers
    public static void query(String objectId, int index){
        BmobQuery<Player> bmobQuery = new BmobQuery<>();
        bmobQuery.getObject(objectId, new QueryListener<Player>() {
            @Override
            public void done(Player player, BmobException e) {
                if (e == null) {
                    players[index] = player;
                }else {
                    players[index] = new Player();
                    players[index].setName("null");
                    players[index].setScore("null");
                }
                notifyObserver();
            }
        });
    }

    // update the Bmob database, cloud DATABASE
    static void onUpdate(String fName, String fScore, String id, Context context){
        Player player = new Player();
        player.setScore(fScore);
        player.setName(fName);
        player.update(id, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Toast.makeText(context, "Upload to the Cloud Database", Toast.LENGTH_SHORT);
                }else {
                    Toast.makeText(context, "Not upload to the Cloud Database", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    // query the database
    public static Map<String, String> retain(Context context){
        Map<String, String> read = new HashMap<>();
        SharedPreferences sp = context.getSharedPreferences("sp", Context.MODE_PRIVATE);

        String fName;
        String sName;
        String tName;
        String fScore;
        String sScore;
        String tScore;
        if (players[0] == null||players[1] == null||players[2] == null){
            fName = sp.getString("fName", null);
            sName = sp.getString("sName", null);
            tName = sp.getString("tName", null);
            fScore = sp.getString("fScore", null);
            sScore = sp.getString("sScore", null);
            tScore = sp.getString("tScore", null);
        }else {
            fName = players[0].getName();
            sName = players[1].getName();
            tName = players[2].getName();
            fScore =players[0].getScore();
            sScore =players[1].getScore();
            tScore =players[2].getScore();

        }

        read.put("fName", fName);
        read.put("sName", sName);
        read.put("tName", tName);
        read.put("fScore",fScore);
        read.put("sScore",sScore);
        read.put("tScore",tScore);

        return read;
    }

    //check if quantify
    public static boolean toRecord(int newScore, Context context){
        String lowestScore = retain(context).get("tScore");
        int tScore;
        try{
            // convert to int
            tScore = Integer.parseInt(lowestScore);
        }catch (NumberFormatException e){
            // in case no score or wrong value got
            tScore = 0;
        }
        if (newScore>tScore) {
            return true;
        }
        else{
            return false;
        }
    }

    // record the new score
    public static String recordScore(int newScore, String newName, Context context){
        Map<String, String> db = retain(context);
        String s = newName;
        int count = 2;
        String[] scores = {db.get("fScore"), db.get("sScore"), db.get("tScore"), null};
        // calculate the rank
        for (int i = 2; i >= 0; i--) {
            int currentScore;
            try{
                // convert to int
                currentScore = Integer.parseInt(scores[i]);
            }catch (NumberFormatException e){
                // in case no score or wrong value got
                currentScore = 0;
            }
            if (newScore>currentScore){
                // get the rank
                count = i;
            }
        }

        String[] names = {db.get("fName"), db.get("sName"), db.get("tName"), null};

        // insert now
        for (int i = 2; i >= 0; i--) {
            String tempN;
            String tempS;
            if(count<=i){
                tempN = names[i];
                tempS = scores[i];

                names[i] = newName;
                scores[i] = newScore+"";

                names[i+1] = tempN;
                scores[i+1] = tempS;
            }
        }
        toSave(context, names[0], names[1], names[2], scores[0], scores[1], scores[2]);

        s = String.format("Congratulations! %s You're No.%s", newName, count+1);
        return s;
    }

}
