package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Setup home fragment content
        Button btnStartLearning = view.findViewById(R.id.btnStartLearning);
        if (btnStartLearning != null) {
            btnStartLearning.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), InteractiveGalaxyMapActivity.class);
                startActivity(intent);
            });
        }
        
        return view;
    }
}
