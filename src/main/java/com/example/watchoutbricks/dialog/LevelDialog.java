package com.example.watchoutbricks.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.watchoutbricks.Game;
import com.example.watchoutbricks.R;

public class LevelDialog extends Dialog {

    boolean turnOn;
    Button simple, normal, hard;

    public LevelDialog(@NonNull Context context, boolean turnOn) {
        super(context);
        setContentView(R.layout.level);
        setCancelable(true);

        simple = findViewById(R.id.simple);
        normal = findViewById(R.id.normal);
        hard = findViewById(R.id.hard);

        //btns action
        simple.setOnClickListener(v -> {
            context.startActivity(new Intent(context, Game.class)
                    .putExtra("music", turnOn)
                    .putExtra("level", 1));
            dismiss();
        });
        normal.setOnClickListener(v -> {
            context.startActivity(new Intent(context, Game.class)
                    .putExtra("music", turnOn)
                    .putExtra("level", 2));
            dismiss();
        });
        hard.setOnClickListener(v -> {
            context.startActivity(new Intent(context, Game.class)
                    .putExtra("music", turnOn)
                    .putExtra("level", 3));
            dismiss();

        });
    }

}
