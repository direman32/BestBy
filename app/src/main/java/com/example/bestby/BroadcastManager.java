package com.example.bestby;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class BroadcastManager extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent it = new Intent(context, MainActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotification(context, it, "", "Items expiring soon", "Log in to see what items have expired");
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotification(Context context, Intent intent, CharSequence ticker, CharSequence title, CharSequence descricao) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel("MAIN", "name", NotificationManager.IMPORTANCE_DEFAULT));
        //PendingIntent p = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "MAIN");
        builder.setTicker(ticker);
        builder.setContentTitle(title);
        builder.setContentText(descricao);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        //builder.setContentIntent(p);
        Notification n = builder.build();
        //create the notification
        n.vibrate = new long[]{150, 300, 150, 400};
        n.flags = Notification.FLAG_AUTO_CANCEL;
        nm.notify(R.drawable.ic_launcher_foreground, n);
        //create a vibration
        try {
            Uri som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone toque = RingtoneManager.getRingtone(context, som);
            toque.play();
        } catch (Exception e) {
        }
    }
}