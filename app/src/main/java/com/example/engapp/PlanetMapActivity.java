package com.example.engapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.adapter.PlanetNodeAdapter;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.SceneData;
import java.util.List;

public class PlanetMapActivity extends AppCompatActivity implements PlanetNodeAdapter.OnNodeClickListener {

    private RecyclerView rvNodes;
    private ProgressBar progressPlanet;
    private TextView tvProgress, tvPlanetName, tvPlanetNameVi, tvPlanetEmoji;
    private TextView tvTopicTitle, tvGrammarFocus, tvCollectible;
    private ImageButton btnBack;

    private GameDatabaseHelper dbHelper;
    private List<SceneData> nodes;
    private PlanetNodeAdapter adapter;

    private int planetId;
    private String planetName, planetNameVi, planetEmoji, planetColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planet_map);

        dbHelper = GameDatabaseHelper.getInstance(this);

        getIntentData();
        initViews();
        loadNodes();
        setupUI();
    }

    private void getIntentData() {
        planetId = getIntent().getIntExtra("planet_id", 1);
        planetName = getIntent().getStringExtra("planet_name");
        planetNameVi = getIntent().getStringExtra("planet_name_vi");
        planetEmoji = getIntent().getStringExtra("planet_emoji");
        planetColor = getIntent().getStringExtra("planet_color");
    }

    private void initViews() {
        rvNodes = findViewById(R.id.rvNodes);
        progressPlanet = findViewById(R.id.progressPlanet);
        tvProgress = findViewById(R.id.tvProgress);
        tvPlanetName = findViewById(R.id.tvPlanetName);
        tvPlanetNameVi = findViewById(R.id.tvPlanetNameVi);
        tvPlanetEmoji = findViewById(R.id.tvPlanetEmoji);
        tvTopicTitle = findViewById(R.id.tvTopicTitle);
        tvGrammarFocus = findViewById(R.id.tvGrammarFocus);
        tvCollectible = findViewById(R.id.tvCollectible);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());

        rvNodes.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadNodes() {
        nodes = dbHelper.getScenesForPlanet(planetId);
    }

    private void setupUI() {
        tvPlanetName.setText(planetName != null ? planetName : "Planet");
        tvPlanetNameVi.setText(planetNameVi != null ? planetNameVi : "");
        tvPlanetEmoji.setText(planetEmoji != null ? planetEmoji : "🌍");

        tvTopicTitle.setText("Topic: " + (planetName != null ? planetName : "Learning"));
        tvGrammarFocus.setText("Grammar: Vocabulary");
        tvCollectible.setText("Collect: Word Cards");

        int completed = 0;
        for (SceneData node : nodes) {
            if (node.isCompleted) completed++;
        }
        int progress = nodes.size() > 0 ? (completed * 100 / nodes.size()) : 0;
        progressPlanet.setProgress(progress);
        tvProgress.setText(progress + "%");

        adapter = new PlanetNodeAdapter(nodes, this);
        rvNodes.setAdapter(adapter);
    }

    @Override
    public void onNodeClick(SceneData node, int position) {
        if (position > 0 && !nodes.get(position - 1).isCompleted) {
            Toast.makeText(this, "Complete previous level first!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent;
        switch (node.sceneType) {
            case "learn":
                intent = new Intent(this, LearnWordsActivity.class);
                break;
            case "guess_name":
                intent = new Intent(this, GuessNameGameActivity.class);
                break;
            case "listen_choose":
                intent = new Intent(this, ListenChooseGameActivity.class);
                break;
            case "match":
                intent = new Intent(this, MatchGameActivity.class);
                break;
            case "sentence":
                intent = new Intent(this, SentenceActivity.class);
                break;
            default:
                intent = new Intent(this, LearnWordsActivity.class);
        }

        intent.putExtra("scene_id", node.id);
        intent.putExtra("planet_id", planetId);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNodes();
        setupUI();
    }
}
