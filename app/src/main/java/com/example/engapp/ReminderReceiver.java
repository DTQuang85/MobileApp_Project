package com.example.engapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.ReminderData;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderId = intent.getIntExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1);
        if (reminderId <= 0) {
            return;
        }

        GameDatabaseHelper dbHelper = GameDatabaseHelper.getInstance(context);
        ReminderData reminder = dbHelper.getReminderById(reminderId);
        if (reminder == null || !reminder.isEnabled) {
            return;
        }

        showNotification(context, reminder);
        ReminderScheduler.scheduleReminder(context, reminder);
    }

    private void showNotification(Context context, ReminderData reminder) {
        NotificationManager manager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                ReminderScheduler.CHANNEL_ID,
                "Study Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, SpaceshipHubActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, openIntent, flags);

        String title = reminder.label != null && !reminder.label.trim().isEmpty()
            ? reminder.label
            : "Study Reminder";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_rocket)
            .setContentTitle(title)
            .setContentText("Time to practice English")
            .setContentIntent(contentIntent)
            .setAutoCancel(true);

        manager.notify(reminder.id, builder.build());
    }
}
