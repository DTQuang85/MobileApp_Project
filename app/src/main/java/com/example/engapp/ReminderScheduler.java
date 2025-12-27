package com.example.engapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.example.engapp.database.GameDatabaseHelper.ReminderData;
import java.util.Calendar;

public class ReminderScheduler {

    public static final String EXTRA_REMINDER_ID = "reminder_id";
    public static final String CHANNEL_ID = "study_reminders";

    public static void scheduleReminder(Context context, ReminderData reminder) {
        if (context == null || reminder == null) {
            return;
        }
        if (!reminder.isEnabled) {
            cancelReminder(context, reminder.id);
            return;
        }
        long triggerAt = computeNextTriggerMillis(reminder);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = buildPendingIntent(context, reminder.id);
        if (alarmManager != null) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }

    public static void cancelReminder(Context context, int reminderId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = buildPendingIntent(context, reminderId);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private static PendingIntent buildPendingIntent(Context context, int reminderId) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction("com.example.engapp.REMINDER");
        intent.putExtra(EXTRA_REMINDER_ID, reminderId);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, reminderId, intent, flags);
    }

    private static long computeNextTriggerMillis(ReminderData reminder) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, reminder.hour);
        target.set(Calendar.MINUTE, reminder.minute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        String days = normalizeRepeatDays(reminder.repeatDays);
        if (!hasAnyDay(days)) {
            if (target.getTimeInMillis() <= now.getTimeInMillis()) {
                target.add(Calendar.DAY_OF_YEAR, 1);
            }
            return target.getTimeInMillis();
        }

        int todayIndex = dayToIndex(now.get(Calendar.DAY_OF_WEEK));
        for (int offset = 0; offset < 7; offset++) {
            int checkIndex = (todayIndex + offset) % 7;
            if (days.charAt(checkIndex) == '1') {
                if (offset == 0 && target.getTimeInMillis() <= now.getTimeInMillis()) {
                    continue;
                }
                target.add(Calendar.DAY_OF_YEAR, offset);
                return target.getTimeInMillis();
            }
        }

        target.add(Calendar.DAY_OF_YEAR, 1);
        return target.getTimeInMillis();
    }

    public static String normalizeRepeatDays(String repeatDays) {
        if (repeatDays == null || repeatDays.length() != 7) {
            return "1111111";
        }
        return repeatDays;
    }

    private static boolean hasAnyDay(String repeatDays) {
        if (repeatDays == null) {
            return false;
        }
        for (int i = 0; i < repeatDays.length(); i++) {
            if (repeatDays.charAt(i) == '1') {
                return true;
            }
        }
        return false;
    }

    private static int dayToIndex(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return 0;
            case Calendar.TUESDAY: return 1;
            case Calendar.WEDNESDAY: return 2;
            case Calendar.THURSDAY: return 3;
            case Calendar.FRIDAY: return 4;
            case Calendar.SATURDAY: return 5;
            case Calendar.SUNDAY: return 6;
            default: return 0;
        }
    }
}
