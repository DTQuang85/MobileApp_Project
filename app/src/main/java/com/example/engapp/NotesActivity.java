package com.example.engapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.NoteData;
import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends AppCompatActivity implements NotesAdapter.NoteActionListener {

    private EditText etNote;
    private Button btnSave;
    private RecyclerView rvNotes;
    private ImageView btnBack;

    private GameDatabaseHelper dbHelper;
    private NotesAdapter adapter;
    private List<NoteData> notes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        overridePendingTransition(R.anim.fade_scale_in, 0);

        dbHelper = GameDatabaseHelper.getInstance(this);

        etNote = findViewById(R.id.etNote);
        btnSave = findViewById(R.id.btnSaveNote);
        rvNotes = findViewById(R.id.rvNotes);
        btnBack = findViewById(R.id.btnBack);

        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotesAdapter(notes, this);
        rvNotes.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveNote());

        loadNotes();
    }

    private void saveNote() {
        String content = etNote.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Note is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        dbHelper.addNote(content);
        etNote.setText("");
        loadNotes();
    }

    private void loadNotes() {
        notes = dbHelper.getAllNotes();
        if (notes == null) {
            notes = new ArrayList<>();
        }
        adapter.updateNotes(notes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    @Override
    public void onDelete(NoteData note) {
        dbHelper.deleteNote(note.id);
        loadNotes();
    }
}
