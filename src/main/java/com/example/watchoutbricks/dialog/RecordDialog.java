package com.example.watchoutbricks.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;

import com.example.watchoutbricks.GameView;
import com.example.watchoutbricks.R;


public class RecordDialog extends Dialog{

    // for the username
    public interface DataListener {
        public void record(String name, boolean flag);
    }
    DataListener listener;

    //view
    private EditText username;
    private Button sure, deny, btn;

    //for callback
    private GameView gameView;
    //constructor
    public RecordDialog(GameView gameView, Context context, final DataListener listener) {
        super(context);
        this.listener = listener;
        setContentView(R.layout.username);
        setCancelable(false);
        this.gameView = gameView;
        username = findViewById(R.id.username);
        sure = findViewById(R.id.sure);
        deny = findViewById(R.id.deny);
        btn = findViewById(R.id.button);

        //btn action
        btn.setOnClickListener(v -> {
            gameView.resume();
            gameView.gameThread.setRunning(false);
            gameView.endGame.endGame();
        });
        deny.setOnClickListener(view -> {
            dismiss();
            gameView.restart();
        });
        sure.setOnClickListener(view -> {
            String name = username.getText().toString();
            if (!name.equals("")){
                listener.record(name, true);
                dismiss();
                gameView.resume();
                gameView.gameThread.setRunning(false);
                gameView.endGame.endGame();
            }else {
                listener.record(name, false);
                dismiss();
            }
        });
    }
}
