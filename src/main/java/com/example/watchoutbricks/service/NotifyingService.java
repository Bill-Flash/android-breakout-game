package com.example.watchoutbricks.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.watchoutbricks.MainActivity;
import com.example.watchoutbricks.R;

import java.util.Timer;
import java.util.TimerTask;

public class NotifyingService extends Service {
    static Timer timer = null;
    static int day = 0;
    Bitmap icon;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //cancel the notification
    public static void cleanNotification() {
        NotificationManager notificationManager = (NotificationManager) MainActivity
                .getContext().getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    //add the notification
    public static void addNotification(int delayTime){
        Intent intent = new Intent(MainActivity.getContext(), NotifyingService.class);
        intent.putExtra("delayTime", delayTime);
        MainActivity.getContext().startService(intent);
    }

    //start
    public int onStartCommand(final Intent intent, int flags, int startId) {
        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
        long period = 24 * 60 * 60 * 1000; // 24hours * 60 min * 60 sec * 1000 mill
        int delay = intent.getIntExtra("delayTime", 0);
        day = 1;
        if (null == timer) {
            timer = new Timer();
        }
        // start the first day
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                NotificationManager notificationManager = (NotificationManager) NotifyingService.this
                        .getSystemService(NOTIFICATION_SERVICE);
                // to adopt the Android
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    NotificationChannel channel = new NotificationChannel("dayNotify","dayNotification",
                            NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(channel);
                }
                NotificationCompat.Builder builder = new NotificationCompat.Builder(
                        NotifyingService.this, "dayNotify");
                // jump to the homepage
                Intent notificationIntent = new Intent(NotifyingService.this,
                        MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(
                        NotifyingService.this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
                builder.setContentIntent(contentIntent)
                        .setLargeIcon(icon)
                        .setSmallIcon(R.drawable.ball)
                        .setTicker(getResources().getString(R.string.app_name))
                        .setContentText(String.format(getResources().getString(R.string.day), day))
                        .setContentTitle("Waiting for you...")
                        .setAutoCancel(true)
                        .setVibrate(new long[]{0,300,500,700})
                        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_ALL)
                        .setLights(Color.BLUE,2000,1000);
                Notification notification = builder.build();
                notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;
                notificationManager.notify((int) System.currentTimeMillis(), notification);
                day++;
            }
        }, delay, period);

        return super.onStartCommand(intent, flags, startId);
    }

    //stop
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
