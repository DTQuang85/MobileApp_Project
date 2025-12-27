package com.example.engapp;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.ReminderData;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RemindersActivity extends AppCompatActivity implements ReminderAdapter.ReminderActionListener {

    private RecyclerView rvReminders;
    private Button btnAddReminder;
    private ImageView btnBack;

    private GameDatabaseHelper dbHelper;
    private ReminderAdapter adapter;
    private List<ReminderData> reminders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        overridePendingTransition(R.anim.fade_scale_in, 0);

        dbHelper = GameDatabaseHelper.getInstance(this);

        rvReminders = findViewById(R.id.rvReminders);
        btnAddReminder = findViewById(R.id.btnAddReminder);
        btnBack = findViewById(R.id.btnBack);

        rvReminders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(reminders, this);
        rvReminders.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnAddReminder.setOnClickListener(v -> showReminderDialog(null));

        requestNotificationPermissionIfNeeded();
        loadReminders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
    }

    private void loadReminders() {
        reminders = dbHelper.getAllReminders();
        if (reminders == null) {
            reminders = new ArrayList<>();
        }
        adapter.updateReminders(reminders);
    }

    private void showReminderDialog(ReminderData existing) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_reminder_edit, null);
        EditText etLabel = view.findViewById(R.id.etReminderLabel);
        TextView tvTime = view.findViewById(R.id.tvReminderTime);
        CheckBox cbMon = view.findViewById(R.id.cbMon);
        CheckBox cbTue = view.findViewById(R.id.cbTue);
        CheckBox cbWed = view.findViewById(R.id.cbWed);
        CheckBox cbThu = view.findViewById(R.id.cbThu);
        CheckBox cbFri = view.findViewById(R.id.cbFri);
        CheckBox cbSat = view.findViewById(R.id.cbSat);
        CheckBox cbSun = view.findViewById(R.id.cbSun);

        int[] hour = new int[]{existing != null ? existing.hour : 20};
        int[] minute = new int[]{existing != null ? existing.minute : 0};
        tvTime.setText(formatTime(hour[0], minute[0]));
        tvTime.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(this, (picker, h, m) -> {
                hour[0] = h;
                minute[0] = m;
                tvTime.setText(formatTime(h, m));
            }, hour[0], minute[0], true);
            dialog.show();
        });

        if (existing != null) {
            etLabel.setText(existing.label);
            boolean[] days = parseRepeatDays(existing.repeatDays);
            cbMon.setChecked(days[0]);
            cbTue.setChecked(days[1]);
            cbWed.setChecked(days[2]);
            cbThu.setChecked(days[3]);
            cbFri.setChecked(days[4]);
            cbSat.setChecked(days[5]);
            cbSun.setChecked(days[6]);
        } else {
            cbMon.setChecked(true);
            cbTue.setChecked(true);
            cbWed.setChecked(true);
            cbThu.setChecked(true);
            cbFri.setChecked(true);
            cbSat.setChecked(true);
            cbSun.setChecked(true);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(existing == null ? "Add Reminder" : "Edit Reminder")
            .setView(view)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create();

        dialog.setOnShowListener(d -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                boolean[] days = new boolean[]{
                    cbMon.isChecked(), cbTue.isChecked(), cbWed.isChecked(),
                    cbThu.isChecked(), cbFri.isChecked(), cbSat.isChecked(), cbSun.isChecked()
                };
                if (!hasAnyDaySelected(days)) {
                    Toast.makeText(this, "Select at least one day", Toast.LENGTH_SHORT).show();
                    return;
                }

                String label = etLabel.getText().toString().trim();
                String repeatDays = buildRepeatDays(days);

                ReminderData reminder = existing != null ? existing : new ReminderData();
                reminder.label = label.isEmpty() ? "Study Reminder" : label;
                reminder.hour = hour[0];
                reminder.minute = minute[0];
                reminder.repeatDays = repeatDays;
                reminder.isEnabled = true;

                if (existing == null) {
                    long id = dbHelper.addReminder(reminder);
                    reminder.id = (int) id;
                } else {
                    dbHelper.updateReminder(reminder);
                }

                ReminderScheduler.scheduleReminder(this, reminder);
                loadReminders();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private String formatTime(int hour, int minute) {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }

    private boolean[] parseRepeatDays(String repeatDays) {
        String normalized = ReminderScheduler.normalizeRepeatDays(repeatDays);
        boolean[] days = new boolean[7];
        for (int i = 0; i < 7; i++) {
            days[i] = normalized.charAt(i) == '1';
        }
        return days;
    }

    private String buildRepeatDays(boolean[] days) {
        StringBuilder builder = new StringBuilder();
        for (boolean day : days) {
            builder.append(day ? '1' : '0');
        }
        return builder.toString();
    }

    private boolean hasAnyDaySelected(boolean[] days) {
        for (boolean day : days) {
            if (day) {
                return true;
            }
        }
        return false;
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    2001
                );
            }
        }
    }

    @Override
    public void onToggle(ReminderData reminder, boolean enabled) {
        reminder.isEnabled = enabled;
        dbHelper.updateReminder(reminder);
        if (enabled) {
            ReminderScheduler.scheduleReminder(this, reminder);
        } else {
            ReminderScheduler.cancelReminder(this, reminder.id);
        }
        loadReminders();
    }

    @Override
    public void onDelete(ReminderData reminder) {
        ReminderScheduler.cancelReminder(this, reminder.id);
        dbHelper.deleteReminder(reminder.id);
        loadReminders();
    }

    @Override
    public void onEdit(ReminderData reminder) {
        showReminderDialog(reminder);
    }
}
