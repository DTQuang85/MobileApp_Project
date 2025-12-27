package com.example.engapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class ProfileFragment extends Fragment {
    private CircleImageView ivAvatar;
    private TextView tvName, tvEmail;
    private Button btnLogout;
    private TextView tvXp, tvWordsLearned, tvGamesCompleted;
    private ViewGroup planetProgressContainer;
    private Button btnNotes, btnReminders;
    private FirebaseAuth auth;
    private com.example.engapp.database.GameDatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        dbHelper = com.example.engapp.database.GameDatabaseHelper.getInstance(requireContext());
        
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnLogout = view.findViewById(R.id.btnLogout);
        tvXp = view.findViewById(R.id.tvXp);
        tvWordsLearned = view.findViewById(R.id.tvWordsLearned);
        tvGamesCompleted = view.findViewById(R.id.tvGamesCompleted);
        planetProgressContainer = view.findViewById(R.id.planetProgressContainer);
        btnNotes = view.findViewById(R.id.btnNotes);
        btnReminders = view.findViewById(R.id.btnReminders);

        loadUserInfo();
        loadProgressStats();

        btnLogout.setOnClickListener(v -> showLogoutDialog());
        btnNotes.setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), NotesActivity.class));
            }
        });
        btnReminders.setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), RemindersActivity.class));
            }
        });

        return view;
    }

    private void loadUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // Hiển thị tên
            String displayName = user.getDisplayName();
            tvName.setText(displayName != null ? displayName : "User");

            // Hiển thị email
            String email = user.getEmail();
            tvEmail.setText(email != null ? email : "No email");

            // Load avatar
            String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.avatar_circle_bg)
                        .error(R.drawable.avatar_circle_bg)
                        .into(ivAvatar);
            }
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    auth.signOut();
                    Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                    
                    // Chuyển về LoginActivity
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void loadProgressStats() {
        com.example.engapp.database.GameDatabaseHelper.UserProgressData progress =
            dbHelper.getUserProgress();
        int xp = progress != null ? progress.experiencePoints : 0;
        tvXp.setText("XP: " + xp);

        com.example.engapp.manager.ProgressionManager progressionManager =
            com.example.engapp.manager.ProgressionManager.getInstance(requireContext());
        tvWordsLearned.setText("Words learned: " + progressionManager.getWordsLearned());
        tvGamesCompleted.setText("Games completed: " + progressionManager.getGamesCompleted());

        if (planetProgressContainer != null) {
            planetProgressContainer.removeAllViews();
            List<com.example.engapp.database.GameDatabaseHelper.PlanetData> planets =
                dbHelper.getAllPlanets();
            android.content.Context context = getContext();
            if (context != null && planets != null) {
                for (com.example.engapp.database.GameDatabaseHelper.PlanetData planet : planets) {
                    List<com.example.engapp.database.GameDatabaseHelper.SceneData> scenes =
                        dbHelper.getScenesForPlanet(planet.id);
                    int completed = 0;
                    for (com.example.engapp.database.GameDatabaseHelper.SceneData scene : scenes) {
                        if (scene.isCompleted) completed++;
                    }
                    int percent = scenes.size() > 0 ? (completed * 100 / scenes.size()) : 0;
                    TextView row = new TextView(context);
                    String name = planet.nameVi != null && !planet.nameVi.isEmpty()
                        ? planet.nameVi
                        : planet.name;
                    row.setText(name + ": " + percent + "%");
                    row.setTextColor(0xFFFFFFFF);
                    row.setTextSize(14f);
                    row.setPadding(0, 6, 0, 6);
                    planetProgressContainer.addView(row);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo();
        loadProgressStats();
    }
}
