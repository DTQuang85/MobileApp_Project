package com.example.engapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.database.GameDatabaseHelper.NoteData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    public interface NoteActionListener {
        void onDelete(NoteData note);
    }

    private List<NoteData> notes = new ArrayList<>();
    private final NoteActionListener listener;
    private final SimpleDateFormat dateFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public NotesAdapter(List<NoteData> notes, NoteActionListener listener) {
        if (notes != null) {
            this.notes = notes;
        }
        this.listener = listener;
    }

    public void updateNotes(List<NoteData> newNotes) {
        notes = newNotes != null ? newNotes : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteData note = notes.get(position);
        holder.bind(note);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        TextView tvDate;
        TextView btnDelete;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvNoteContent);
            tvDate = itemView.findViewById(R.id.tvNoteDate);
            btnDelete = itemView.findViewById(R.id.btnDeleteNote);
        }

        void bind(NoteData note) {
            tvContent.setText(note.content);
            tvDate.setText(formatDate(note.updatedAt));
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(note);
                }
            });
        }
    }

    private String formatDate(String timestamp) {
        try {
            long millis = Long.parseLong(timestamp);
            return dateFormat.format(new Date(millis));
        } catch (Exception e) {
            return "";
        }
    }
}
