package com.example.engapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.database.GameDatabaseHelper.ReminderData;
import java.util.ArrayList;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    public interface ReminderActionListener {
        void onToggle(ReminderData reminder, boolean enabled);
        void onDelete(ReminderData reminder);
        void onEdit(ReminderData reminder);
    }

    private List<ReminderData> reminders = new ArrayList<>();
    private final ReminderActionListener listener;

    public ReminderAdapter(List<ReminderData> reminders, ReminderActionListener listener) {
        if (reminders != null) {
            this.reminders = reminders;
        }
        this.listener = listener;
    }

    public void updateReminders(List<ReminderData> newReminders) {
        reminders = newReminders != null ? newReminders : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        ReminderData reminder = reminders.get(position);
        holder.bind(reminder);
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel;
        TextView tvTime;
        TextView tvDays;
        TextView btnDelete;
        SwitchCompat switchEnabled;

        ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvReminderLabel);
            tvTime = itemView.findViewById(R.id.tvReminderTime);
            tvDays = itemView.findViewById(R.id.tvReminderDays);
            btnDelete = itemView.findViewById(R.id.btnDeleteReminder);
            switchEnabled = itemView.findViewById(R.id.switchReminder);
        }

        void bind(ReminderData reminder) {
            String label = reminder.label != null && !reminder.label.trim().isEmpty()
                ? reminder.label
                : "Study Reminder";
            tvLabel.setText(label);
            tvTime.setText(String.format("%02d:%02d", reminder.hour, reminder.minute));
            tvDays.setText(formatRepeatDays(reminder.repeatDays));

            switchEnabled.setOnCheckedChangeListener(null);
            switchEnabled.setChecked(reminder.isEnabled);
            switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggle(reminder, isChecked);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(reminder);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(reminder);
                }
            });
        }
    }

    private String formatRepeatDays(String repeatDays) {
        String days = ReminderScheduler.normalizeRepeatDays(repeatDays);
        String[] names = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (days.charAt(i) == '1') {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(names[i]);
            }
        }
        if (builder.length() == 0) {
            return "One-time";
        }
        if (builder.toString().equals("Mon, Tue, Wed, Thu, Fri, Sat, Sun")) {
            return "Every day";
        }
        return builder.toString();
    }
}
