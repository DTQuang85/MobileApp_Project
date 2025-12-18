package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.engapp.data.GameDataProvider;
import com.example.engapp.model.Planet;
import com.example.engapp.model.Zone;

public class ZoneActivity extends AppCompatActivity {

    private TextView tvZoneName, tvZoneNameVi, tvZoneEmoji, tvWordCount;
    private ImageView btnBack;
    private CardView cardLearnWords, cardGuessName, cardListenChoose, cardMatch, cardSentence;

    private Planet currentPlanet;
    private Zone currentZone;
    private String planetId;
    private int zoneIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone);

        planetId = getIntent().getStringExtra("planet_id");
        zoneIndex = getIntent().getIntExtra("zone_index", 0);

        if (planetId == null) {
            finish();
            return;
        }

        initViews();
        loadZone();
        setupClickListeners();
    }

    private void initViews() {
        tvZoneName = findViewById(R.id.tvZoneName);
        tvZoneNameVi = findViewById(R.id.tvZoneNameVi);
        tvZoneEmoji = findViewById(R.id.tvZoneEmoji);
        tvWordCount = findViewById(R.id.tvWordCount);
        btnBack = findViewById(R.id.btnBack);

        cardLearnWords = findViewById(R.id.cardLearnWords);
        cardGuessName = findViewById(R.id.cardGuessName);
        cardListenChoose = findViewById(R.id.cardListenChoose);
        cardMatch = findViewById(R.id.cardMatch);
        cardSentence = findViewById(R.id.cardSentence);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadZone() {
        currentPlanet = GameDataProvider.getPlanetById(planetId);

        if (currentPlanet == null || currentPlanet.getZones() == null ||
            zoneIndex >= currentPlanet.getZones().size()) {
            finish();
            return;
        }

        currentZone = currentPlanet.getZones().get(zoneIndex);

        tvZoneName.setText(currentZone.getName());
        tvZoneNameVi.setText(currentZone.getNameVi());
        tvZoneEmoji.setText(currentZone.getEmoji());

        int wordCount = currentZone.getWords() != null ? currentZone.getWords().size() : 0;
        tvWordCount.setText(wordCount + " từ vựng cần học");
    }

    private void setupClickListeners() {
        // Learn Words
        cardLearnWords.setOnClickListener(v -> {
            Intent intent = new Intent(this, LearnWordsActivity.class);
            intent.putExtra("planet_id", planetId);
            intent.putExtra("zone_index", zoneIndex);
            startActivity(intent);
        });

        // Guess Name Game
        cardGuessName.setOnClickListener(v -> {
            Intent intent = new Intent(this, GuessNameGameActivity.class);
            intent.putExtra("planet_id", planetId);
            intent.putExtra("zone_index", zoneIndex);
            startActivity(intent);
        });

        // Listen & Choose Game
        cardListenChoose.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListenChooseGameActivity.class);
            intent.putExtra("planet_id", planetId);
            intent.putExtra("zone_index", zoneIndex);
            startActivity(intent);
        });

        // Match Game
        cardMatch.setOnClickListener(v -> {
            Intent intent = new Intent(this, MatchGameActivity.class);
            intent.putExtra("planet_id", planetId);
            intent.putExtra("zone_index", zoneIndex);
            startActivity(intent);
        });

        // Sentence Learning
        cardSentence.setOnClickListener(v -> {
            Intent intent = new Intent(this, SentenceActivity.class);
            intent.putExtra("planet_id", planetId);
            intent.putExtra("zone_index", zoneIndex);
            startActivity(intent);
        });
    }
}

