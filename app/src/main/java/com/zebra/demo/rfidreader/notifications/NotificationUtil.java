package com.zebra.demo.rfidreader.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.DeviceDiscoverActivity;
import com.zebra.demo.R;
import com.zebra.demo.rfidreader.common.Constants;
import com.zebra.demo.rfidreader.settings.SettingsDetailActivity;

public class NotificationUtil {

    public static final String CHANNEL_ID = "NotificationChannel";
    public static final String NOTIFICATION_CHANNEL = "Notification Channel";
    private static int NOTIFICATION_ID = 1;
    private static final String RFID_NOTIFICATIONS = "com.zebra.rfid.notifications";
    private static final int SUMMARY_ID = 0;


    public static void displayNotification(Context context, String action, String data) {

        NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    NOTIFICATION_CHANNEL,
                    NotificationManager.IMPORTANCE_HIGH
            );
            mgr.createNotificationChannel(serviceChannel);
        }

        Intent resultIntent = new Intent(context, ActiveDeviceActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (action != null && ((action.equalsIgnoreCase(Constants.ACTION_READER_BATTERY_CRITICAL)) || (action.equalsIgnoreCase(Constants.ACTION_READER_BATTERY_LOW))))
            resultIntent.putExtra(Constants.SETTING_ITEM_ID, R.id.regulatory);
        else
            resultIntent.putExtra(Constants.SETTING_ITEM_ID, R.id.advanced_options);
        resultIntent.putExtra(Constants.FROM_NOTIFICATION, true);

        Intent launcherIntent =new Intent(context, DeviceDiscoverActivity.class);
        launcherIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        launcherIntent.setAction(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        Intent[] intents = {launcherIntent, resultIntent};

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivities(context, 0, intents,  PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivities(context, 0, intents, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_stat_notify_msg);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.app_title))
                .setContentText(data)
                .setLargeIcon(icon)
                .setSmallIcon(R.drawable.ic_stat_notify_msg)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setGroup(RFID_NOTIFICATIONS);
        mgr.notify(NOTIFICATION_ID++, mBuilder.build());

        //adding support for notification group
        Notification summaryNotification =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_stat_notify_msg)
                        .setStyle(new NotificationCompat.InboxStyle())
                        .setGroup(RFID_NOTIFICATIONS)
                        .setGroupSummary(true)
                        .build();
        mgr.notify(SUMMARY_ID, summaryNotification);

    }

    public static void displayNotificationforSettingsDeialActivity(Context context, String action, String data) {

        NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    NOTIFICATION_CHANNEL,
                    NotificationManager.IMPORTANCE_HIGH
            );
            mgr.createNotificationChannel(serviceChannel);
        }

        Intent resultIntent = new Intent(context, SettingsDetailActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (action != null && ((action.equalsIgnoreCase(Constants.ACTION_READER_BATTERY_CRITICAL)) || (action.equalsIgnoreCase(Constants.ACTION_READER_BATTERY_LOW))))
            resultIntent.putExtra(Constants.SETTING_ITEM_ID, R.id.regulatory);
        else
            resultIntent.putExtra(Constants.SETTING_ITEM_ID, R.id.advanced_options);
        resultIntent.putExtra(Constants.FROM_NOTIFICATION, true);

        Intent secondIntent = new Intent(context, ActiveDeviceActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent launcherIntent =new Intent(context, DeviceDiscoverActivity.class);
        launcherIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        launcherIntent.setAction(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        Intent[] intents = {launcherIntent, secondIntent, resultIntent};

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivities(context, 0, intents,  PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivities(context, 0, intents, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_stat_notify_msg);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.app_title))
                .setContentText(data)
                .setLargeIcon(icon)
                .setSmallIcon(R.drawable.ic_stat_notify_msg)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setGroup(RFID_NOTIFICATIONS);;
        mgr.notify(NOTIFICATION_ID++, mBuilder.build());

        //adding support for notification group
        Notification summaryNotification =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_stat_notify_msg)
                        .setStyle(new NotificationCompat.InboxStyle())
                        .setGroup(RFID_NOTIFICATIONS)
                        .setGroupSummary(true)
                        .build();
        mgr.notify(SUMMARY_ID, summaryNotification);

    }
}
