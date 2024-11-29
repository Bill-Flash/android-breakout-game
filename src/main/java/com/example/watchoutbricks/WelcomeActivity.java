package com.example.watchoutbricks;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ImageView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

// reference from https://github.com/lingyunzhu/WelcomPage
// layout is also reference from it
public class WelcomeActivity extends Activity {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.iv_entry)
    ImageView mIVEntry;


    private static final int ANIM_TIME = 1800;

    private static final float SCALE_END = 1.15F;

    //background image
    private static final int[] Imgs={
            R.drawable.welcome_p,R.drawable.welcome_l};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splah);
        ButterKnife.bind(this);

        // 2 images for orientation and landscape
        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == configuration.ORIENTATION_LANDSCAPE){
            mIVEntry.setImageResource(Imgs[1]);
        }else {
            mIVEntry.setImageResource(Imgs[0]);
        }

        Observable.timer(800, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> startAnim());
    }

    // the background scaling
    private void startAnim() {

        ObjectAnimator animatorX = ObjectAnimator.ofFloat(mIVEntry, "scaleX", 1f, SCALE_END);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(mIVEntry, "scaleY", 1f, SCALE_END);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(ANIM_TIME).play(animatorX).with(animatorY);
        set.start();

        set.addListener(new AnimatorListenerAdapter()
        {

            @Override
            public void onAnimationEnd(Animator animation)
            {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                WelcomeActivity.this.finish();
            }
        });
    }
}