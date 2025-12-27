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
        // Ưu tiên lấy từ intent, nếu không có thì lấy từ TravelManager
        planetId = getIntent().getIntExtra("planet_id", -1);
        
        if (planetId == -1) {
            // Lấy từ TravelManager - current planet
            com.example.engapp.manager.TravelManager travelManager = 
                com.example.engapp.manager.TravelManager.getInstance(this);
            String currentPlanetKey = travelManager.getCurrentPlanetId();
            
            // Chuyển đổi planet key sang planet_id
            GameDatabaseHelper.PlanetData planetData = dbHelper.getPlanetByKey(currentPlanetKey);
            if (planetData != null) {
                planetId = planetData.id;
            } else {
                planetId = 1; // Fallback
            }
        }
        
        // Lấy thông tin planet từ database nếu không có trong intent
        GameDatabaseHelper.PlanetData planetData = dbHelper.getPlanetById(planetId);
        if (planetData != null) {
            planetName = getIntent().getStringExtra("planet_name");
            if (planetName == null) planetName = planetData.name;
            
            planetNameVi = getIntent().getStringExtra("planet_name_vi");
            if (planetNameVi == null) planetNameVi = planetData.nameVi;
            
            planetEmoji = getIntent().getStringExtra("planet_emoji");
            if (planetEmoji == null) planetEmoji = planetData.emoji;
            
            planetColor = getIntent().getStringExtra("planet_color");
            if (planetColor == null) planetColor = normalizeColor(planetData.themeColor, "#4ADE80");

            com.example.engapp.manager.TravelManager.getInstance(this)
                .setCurrentPlanetId(planetData.planetKey);
        } else {
            // Fallback nếu không tìm thấy
            planetName = getIntent().getStringExtra("planet_name");
            planetNameVi = getIntent().getStringExtra("planet_name_vi");
            planetEmoji = getIntent().getStringExtra("planet_emoji");
            planetColor = getIntent().getStringExtra("planet_color");
        }
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
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"D\",\"location\":\"PlanetMapActivity.loadNodes:105\",\"message\":\"loadNodes entry\",\"data\":{\"planetId\":" + planetId + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        nodes = dbHelper.getScenesForPlanet(planetId);
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"D\",\"location\":\"PlanetMapActivity.loadNodes:107\",\"message\":\"loadNodes result\",\"data\":{\"planetId\":" + planetId + ",\"nodeCount\":" + (nodes != null ? nodes.size() : 0) + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            if (nodes != null) {
                for (SceneData node : nodes) {
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"D\",\"location\":\"PlanetMapActivity.loadNodes:108\",\"message\":\"Node data\",\"data\":{\"nodeId\":" + node.id + ",\"nodeName\":\"" + node.name + "\",\"isCompleted\":" + node.isCompleted + ",\"starsEarned\":" + node.starsEarned + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                }
            }
            fw.close();
        } catch (Exception e) {}
        // #endregion
    }

    private void setupUI() {
        // Get planet data from database to show correct info
        GameDatabaseHelper.PlanetData planetData = dbHelper.getPlanetById(planetId);
        
        tvPlanetName.setText(planetName != null ? planetName : "Planet");
        tvPlanetNameVi.setText(planetNameVi != null ? planetNameVi : "");
        tvPlanetEmoji.setText(planetEmoji != null ? planetEmoji : "🌍");

        // Use actual planet data instead of hardcoded values
        if (planetData != null) {
            tvTopicTitle.setText("Topic: " + (planetData.name != null ? planetData.name : "Learning"));
            tvGrammarFocus.setText("Grammar: " + (planetData.grammarFocus != null ? planetData.grammarFocus : "Vocabulary"));
            tvCollectible.setText("Collect: " + (planetData.collectibleName != null ? planetData.collectibleName : "Word Cards") + 
                " " + (planetData.collectibleEmoji != null ? planetData.collectibleEmoji : "📚"));
        } else {
            tvTopicTitle.setText("Topic: " + (planetName != null ? planetName : "Learning"));
            tvGrammarFocus.setText("Grammar: Vocabulary");
            tvCollectible.setText("Collect: Word Cards");
        }

        int completed = 0;
        for (SceneData node : nodes) {
            if (node.isCompleted) completed++;
        }
        int progress = nodes.size() > 0 ? (completed * 100 / nodes.size()) : 0;
        progressPlanet.setProgress(progress);
        tvProgress.setText(progress + "%");

        // Update adapter - create new if doesn't exist, otherwise update data and notify
        if (adapter == null) {
            adapter = new PlanetNodeAdapter(nodes, this);
            rvNodes.setAdapter(adapter);
        } else {
            // Update existing adapter's data
            adapter.updateNodes(nodes);
            adapter.notifyDataSetChanged();
        }
    }

    private String normalizeColor(String color, String fallback) {
        if (color == null) {
            return fallback;
        }
        String trimmed = color.trim();
        if (trimmed.isEmpty()) {
            return fallback;
        }
        if (!trimmed.startsWith("#")) {
            trimmed = "#" + trimmed;
        }
        return trimmed;
    }

    @Override
    public void onNodeClick(SceneData node, int position) {
        // Use LessonUnlockManager to check unlock status
        com.example.engapp.manager.LessonUnlockManager unlockManager = 
            com.example.engapp.manager.LessonUnlockManager.getInstance(this);
        
        if (!unlockManager.isLessonUnlocked(planetId, node.id)) {
            Toast.makeText(this, "Complete previous lesson first!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent;
        String sceneType = node.sceneType;
        
        // Map database scene_key to activity
        switch (sceneType) {
            // Standard types
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
            case "battle":
                intent = new Intent(this, WordBattleActivity.class);
                break;
                
            // Database scene_key types
            case "landing_zone":
                intent = new Intent(this, LearnWordsActivity.class);
                break;
            case "explore_area":
                intent = new Intent(this, ExploreActivity.class);
                break;
            case "dialogue_dock":
                intent = new Intent(this, DialogueActivity.class);
                break;
            case "puzzle_zone":
                intent = new Intent(this, PuzzleGameActivity.class);
                break;
            case "mini_game":
                intent = new Intent(this, SignalDecodeActivity.class);
                break;
            case "boss_gate":
                intent = new Intent(this, BossGateActivity.class);
                break;
                
            default:
                intent = new Intent(this, LearnWordsActivity.class);
        }

        intent.putExtra("scene_id", node.id);
        intent.putExtra("planet_id", planetId);
        intent.putExtra("planet_name", planetName);
        intent.putExtra("planet_name_vi", planetNameVi);
        intent.putExtra("planet_emoji", planetEmoji);
        intent.putExtra("planet_color", planetColor);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"G\",\"location\":\"PlanetMapActivity.onResume:227\",\"message\":\"onResume entry\",\"data\":{\"planetId\":" + planetId + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        
        // Luôn cập nhật planet_id từ TravelManager để đảm bảo đúng hành tinh hiện tại
        com.example.engapp.manager.TravelManager travelManager = 
            com.example.engapp.manager.TravelManager.getInstance(this);
        String currentPlanetKey = travelManager.getCurrentPlanetId();
        
        // Chuyển đổi planet key sang planet_id
        GameDatabaseHelper.PlanetData planetData = dbHelper.getPlanetByKey(currentPlanetKey);
        if (planetData != null && planetData.id != planetId) {
            // Cập nhật planet_id nếu khác
            planetId = planetData.id;
            planetName = planetData.name;
            planetNameVi = planetData.nameVi;
            planetEmoji = planetData.emoji;
            planetColor = normalizeColor(planetData.themeColor, planetColor != null ? planetColor : "#4ADE80");
        }
        
        // Refresh unlock status when returning to planet map
        com.example.engapp.manager.LessonUnlockManager unlockManager = 
            com.example.engapp.manager.LessonUnlockManager.getInstance(this);
        unlockManager.refreshPlanetLessonsUnlockStatus(planetId);
        
        loadNodes();
        setupUI();
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"G\",\"location\":\"PlanetMapActivity.onResume:253\",\"message\":\"onResume exit\",\"data\":{\"planetId\":" + planetId + ",\"nodeCount\":" + (nodes != null ? nodes.size() : 0) + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
    }
}

