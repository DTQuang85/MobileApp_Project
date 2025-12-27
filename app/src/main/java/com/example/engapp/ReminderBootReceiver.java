package com.example.engapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.ReminderData;
import java.util.List;

public class ReminderBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : null;
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            GameDatabaseHelper dbHelper = GameDatabaseHelper.getInstance(context);
            List<ReminderData> reminders = dbHelper.getAllReminders();
            if (reminders != null) {
                for (ReminderData reminder : reminders) {
                    if (reminder.isEnabled) {
                        ReminderScheduler.scheduleReminder(context, reminder);
                    }
                }
            }
        }
    }
}
