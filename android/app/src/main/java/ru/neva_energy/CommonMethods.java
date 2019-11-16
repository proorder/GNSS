package ru.neva_energy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class CommonMethods {
    static String CHANNEL_ID = "pos_status";

    static void deleteNotification(Context context) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.deleteNotificationChannel(CHANNEL_ID);
    }

    static void updateNotification(Context context, String name, String description, String status) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setContentTitle(name)
                        .setContentText(description);

        switch (status) {
            case "No position":
                builder.setSmallIcon(R.mipmap.icon_np);
                builder.setColor(context.getResources().getColor(R.color.notification_disable));
                break;
            case "Autonom":
                builder.setSmallIcon(R.mipmap.icon_at);
                builder.setColor(context.getResources().getColor(R.color.notification_autonom));
                break;
            case "DGPS":
                builder.setSmallIcon(R.mipmap.icon_dg);
                builder.setColor(context.getResources().getColor(R.color.notification_dgps));
                break;
            case "GPS PPS":
                builder.setSmallIcon(R.mipmap.icon_pp);
                builder.setColor(context.getResources().getColor(R.color.notification_float));
                break;
            case "FIXED RTK":
                builder.setSmallIcon(R.mipmap.icon_fx);
                builder.setColor(context.getResources().getColor(R.color.notification_enable));
                break;
            case "Float RTK":
                builder.setSmallIcon(R.mipmap.icon_fl);
                builder.setColor(context.getResources().getColor(R.color.notification_autonom));
                break;
            case "Extrapolation":
                builder.setSmallIcon(R.mipmap.icon_ex);
                builder.setColor(context.getResources().getColor(R.color.notification_autonom));
                break;
            case "Manual":
                builder.setSmallIcon(R.mipmap.icon_mn);
                builder.setColor(context.getResources().getColor(R.color.notification_autonom));
                break;
            case "Simulation":
                builder.setSmallIcon(R.mipmap.icon_sm);
                builder.setColor(context.getResources().getColor(R.color.notification_autonom));
                break;
            default:
                break;
        }

        Notification notification = builder.build();

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
