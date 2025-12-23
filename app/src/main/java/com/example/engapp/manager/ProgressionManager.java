package com.example.engapp.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.engapp.model.Collectible;
import com.example.engapp.model.Planet;
import com.example.engapp.model.UserProgress;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * Manages user progression, planet unlocking, stars, and achievements.
 */
public class ProgressionManager {

    private static ProgressionManager instance;
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;

    private UserProgress userProgress;
    private List<Collectible> collectibles;
    private Map<String, Integer> planetUnlockRequirements;
    private List<ProgressionEventListener> listeners;
    private LessonUnlockManager lessonUnlockManager;

    private boolean cloudSyncReady;
    private boolean cloudSyncRequested;

    private static final String PREFS_NAME = "progression_prefs";
    private static final String KEY_USER_PROGRESS = "user_progress";
    private static final String KEY_COLLECTIBLES = "collectibles";

    // Milestone thresholds
    public static final int STARS_FOR_LEVEL_UP = 50;
    public static final int WORDS_FOR_BADGE = 25;
    public static final int GAMES_FOR_BADGE = 10;

    public interface ProgressionEventListener {
        void onStarsChanged(int totalStars, int addedStars);
        void onLevelUp(int newLevel);
        void onPlanetUnlocked(String planetId, String planetName);
        void onBadgeEarned(Collectible badge);
        void onCollectibleAdded(Collectible collectible);
        void onMilestoneReached(String milestoneType, int value);
    }

    private ProgressionManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.listeners = new ArrayList<>();
        this.lessonUnlockManager = LessonUnlockManager.getInstance(this.context);

        initPlanetRequirements();
        loadData();
        initCloudSync();
    }

    public static synchronized ProgressionManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProgressionManager(context);
        }
        return instance;
    }

    private void initPlanetRequirements() {
        planetUnlockRequirements = new HashMap<>();

        // Galaxy 1: Milky Way (Beginner)
        planetUnlockRequirements.put("coloria_prime", 0);      // Always unlocked
        planetUnlockRequirements.put("toytopia_orbit", 15);   // 15 stars (0 + 15)
        planetUnlockRequirements.put("animania_wild", 30);     // 30 stars (15 + 15)
        planetUnlockRequirements.put("numberia_station", 45); // 45 stars (30 + 15)

        // Galaxy 2: Andromeda (Explorer)
        planetUnlockRequirements.put("citytron_nova", 60);   // 60 stars (45 + 15)
        planetUnlockRequirements.put("foodora_station", 75); // 75 stars (60 + 15)
        planetUnlockRequirements.put("weatheron_sky", 90);    // 90 stars (75 + 15)
        planetUnlockRequirements.put("familia_home", 105);    // 105 stars (90 + 15)

        // Galaxy 3: Nebula Prime (Advanced)
        planetUnlockRequirements.put("robolab", 120);          // 120 stars (105 + 15)
        planetUnlockRequirements.put("timelapse", 135);      // 135 stars (120 + 15)
        planetUnlockRequirements.put("storyverse_galaxy", 150); // 150 stars (135 + 15)
        planetUnlockRequirements.put("natura", 165);          // 165 stars (150 + 15)

        // NEW PLANETS from NEW_PLANETS_IDEA.md
        planetUnlockRequirements.put("artopia_planet", 180);    // 180 stars (165 + 15)
        planetUnlockRequirements.put("playground_park", 195);   // 195 stars (180 + 15)
        planetUnlockRequirements.put("school_academy", 210);    // 210 stars (195 + 15)
        planetUnlockRequirements.put("body_parts_planet", 225); // 225 stars (210 + 15)
        planetUnlockRequirements.put("sports_arena", 240);      // 240 stars (225 + 15)
        planetUnlockRequirements.put("birthday_party", 255);    // 255 stars (240 + 15)
        planetUnlockRequirements.put("ocean_deep", 270);        // 270 stars (255 + 15)

        // Legacy planet keys (for backward compatibility)
        planetUnlockRequirements.put("animal", 0);            // Always unlocked
        planetUnlockRequirements.put("color", 0);             // Same as coloria_prime
        planetUnlockRequirements.put("number", 45);          // Same as numberia_station
        planetUnlockRequirements.put("food", 75);           // Same as foodora_station
        planetUnlockRequirements.put("family", 105);         // Same as familia_home
        planetUnlockRequirements.put("body", 75);            // Same as foodora_station
        planetUnlockRequirements.put("school", 90);          // Same as weatheron_sky
        planetUnlockRequirements.put("nature", 165);          // Same as natura
        planetUnlockRequirements.put("home", 105);           // Same as familia_home
        planetUnlockRequirements.put("action", 135);         // Same as timelapse
        planetUnlockRequirements.put("emotion", 150);        // Same as storyverse_galaxy
        planetUnlockRequirements.put("travel", 165);         // Same as natura

        // Bonus fictional planets
        planetUnlockRequirements.put("crystal_world", 285);  // 285 stars (270 + 15)
        planetUnlockRequirements.put("rainbow_planet", 300); // 300 stars (285 + 15)
        planetUnlockRequirements.put("robot_station", 315);  // 315 stars (300 + 15)
    }

    private void loadData() {
        // Load user progress
        String progressJson = prefs.getString(KEY_USER_PROGRESS, null);
        if (progressJson != null) {
            userProgress = gson.fromJson(progressJson, UserProgress.class);
        } else {
            userProgress = new UserProgress();
            saveUserProgress();
        }

        // Load collectibles
        String collectiblesJson = prefs.getString(KEY_COLLECTIBLES, null);
        if (collectiblesJson != null) {
            Type listType = new TypeToken<ArrayList<Collectible>>(){}.getType();
            collectibles = gson.fromJson(collectiblesJson, listType);
        } else {
            collectibles = new ArrayList<>();
        }
    }

    public void saveUserProgress() {
        String json = gson.toJson(userProgress);
        prefs.edit().putString(KEY_USER_PROGRESS, json).apply();
        scheduleCloudSync();
    }

    private void saveCollectibles() {
        String json = gson.toJson(collectibles);
        prefs.edit().putString(KEY_COLLECTIBLES, json).apply();
    }

    private void initCloudSync() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            cloudSyncReady = true;
            return;
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(this::mergeCloudProgress)
            .addOnFailureListener(e -> cloudSyncReady = true);
    }

    private void mergeCloudProgress(DocumentSnapshot doc) {
        int localStars = userProgress.getTotalStars();
        int localPlanetsUnlocked = userProgress.getPlanetsUnlocked();

        Long cloudStars = doc.getLong("totalStars");
        int remoteStars = cloudStars != null ? cloudStars.intValue() : 0;

        Set<String> localPlanets = lessonUnlockManager.getUnlockedPlanetsCopy();
        Set<String> mergedPlanets = new HashSet<>(localPlanets);
        Object remotePlanets = doc.get("unlockedPlanets");
        if (remotePlanets instanceof List) {
            for (Object item : (List<?>) remotePlanets) {
                if (item instanceof String) {
                    mergedPlanets.add((String) item);
                }
            }
        }

        int mergedStars = Math.max(localStars, remoteStars);
        if (mergedStars != localStars) {
            userProgress.setTotalStars(mergedStars);
        }

        int mergedPlanetsUnlocked = Math.max(localPlanetsUnlocked, mergedPlanets.size());
        if (mergedPlanetsUnlocked != localPlanetsUnlocked) {
            userProgress.setPlanetsUnlocked(mergedPlanetsUnlocked);
        }

        boolean planetsChanged = !mergedPlanets.equals(localPlanets);
        if (planetsChanged) {
            lessonUnlockManager.mergeUnlockedPlanets(mergedPlanets);
        }

        boolean progressChanged = mergedStars != localStars || mergedPlanetsUnlocked != localPlanetsUnlocked;
        if (progressChanged) {
            saveUserProgress();
        }

        checkForNewUnlocks();
        cloudSyncReady = true;

        if (mergedStars != localStars) {
            int added = mergedStars - localStars;
            for (ProgressionEventListener listener : listeners) {
                listener.onStarsChanged(mergedStars, added);
            }
        }

        if (cloudSyncRequested || progressChanged || planetsChanged) {
            cloudSyncRequested = false;
            syncToCloudIfPossible();
        }
    }

    private void scheduleCloudSync() {
        if (!cloudSyncReady) {
            cloudSyncRequested = true;
            return;
        }
        syncToCloudIfPossible();
    }

    private void syncToCloudIfPossible() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("totalStars", userProgress.getTotalStars());
        data.put("unlockedPlanets", new ArrayList<>(lessonUnlockManager.getUnlockedPlanetsCopy()));
        data.put("updatedAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.getUid())
            .set(data, SetOptions.merge());
    }

    public void refreshCloudSync() {
        cloudSyncReady = false;
        cloudSyncRequested = false;
        initCloudSync();
    }

    // Getters
    public UserProgress getUserProgress() {
        return userProgress;
    }

    public int getTotalStars() {
        return userProgress.getTotalStars();
    }

    public int getCurrentLevel() {
        return userProgress.getCurrentLevel();
    }

    public int getWordsLearned() {
        return userProgress.getWordsLearned();
    }

    public int getGamesCompleted() {
        return userProgress.getGamesCompleted();
    }

    public List<Collectible> getCollectibles() {
        return new ArrayList<>(collectibles);
    }

    public List<Collectible> getCollectiblesByCategory(String category) {
        List<Collectible> filtered = new ArrayList<>();
        for (Collectible c : collectibles) {
            if (category.equals(c.getCategory())) {
                filtered.add(c);
            }
        }
        return filtered;
    }

    // Star management
    public void addStars(int amount, String source) {
        int previousStars = userProgress.getTotalStars();
        int previousLevel = userProgress.getCurrentLevel();

        userProgress.addStars(amount);
        saveUserProgress();

        // Create star collectible
        Collectible star = Collectible.createGoldenStar(source);
        star.setValue(amount);
        addCollectible(star);

        // Notify listeners
        for (ProgressionEventListener listener : listeners) {
            listener.onStarsChanged(userProgress.getTotalStars(), amount);
        }

        // Check for level up
        if (userProgress.getCurrentLevel() > previousLevel) {
            for (ProgressionEventListener listener : listeners) {
                listener.onLevelUp(userProgress.getCurrentLevel());
            }
        }

        // Check for new planet unlocks
        checkForNewUnlocks();
    }

    public void addBonusStars(int amount, String source) {
        Collectible rainbowStar = Collectible.createRainbowStar(source);
        rainbowStar.setValue(amount);
        addCollectible(rainbowStar);

        addStars(amount, source);
    }

    // Word learning
    public void recordWordLearned(String word, String wordVi, String planetId) {
        userProgress.setWordsLearned(userProgress.getWordsLearned() + 1);
        saveUserProgress();

        // Create word crystal
        Collectible crystal = Collectible.createWordCrystal(word, wordVi, planetId);
        addCollectible(crystal);

        // Check for word milestones
        int wordsLearned = userProgress.getWordsLearned();
        if (wordsLearned % WORDS_FOR_BADGE == 0) {
            awardWordBadge(wordsLearned);
        }
    }

    // Game completion
    public void recordGameCompleted(String gameType, int starsEarned) {
        userProgress.setGamesCompleted(userProgress.getGamesCompleted() + 1);
        saveUserProgress();

        if (starsEarned > 0) {
            addStars(starsEarned, gameType);
        }

        // Check for game milestones
        int gamesCompleted = userProgress.getGamesCompleted();
        if (gamesCompleted % GAMES_FOR_BADGE == 0) {
            awardGameBadge(gamesCompleted);
        }
        
        // Check for new unlocks after earning stars
        checkForNewUnlocks();
    }

    // Zone/Planet completion - INTEGRATED WITH LessonUnlockManager
    public void recordZoneCompleted(String planetKey, String zoneId, int starsEarned) {
        // Get planet ID from key
        com.example.engapp.database.GameDatabaseHelper dbHelper = 
            com.example.engapp.database.GameDatabaseHelper.getInstance(context);
        com.example.engapp.database.GameDatabaseHelper.PlanetData planet = 
            dbHelper.getPlanetByKey(planetKey);
        
        if (planet != null) {
            // Find scene by zoneId or order
            List<com.example.engapp.database.GameDatabaseHelper.SceneData> scenes = 
                dbHelper.getScenesForPlanet(planet.id);
            
            // Try to find scene by key or use first incomplete scene
            int sceneId = -1;
            for (com.example.engapp.database.GameDatabaseHelper.SceneData scene : scenes) {
                if (scene.sceneKey.equals(zoneId) || scene.sceneType.equals(zoneId)) {
                    sceneId = scene.id;
                    break;
                }
            }
            
            if (sceneId > 0) {
                // Complete lesson using LessonUnlockManager
                boolean newLessonUnlocked = lessonUnlockManager.completeLesson(
                    planet.id, sceneId, starsEarned);
                
                // Update planet stars
                Map<String, Integer> planetStars = userProgress.getPlanetStars();
                int currentPlanetStars = planetStars.getOrDefault(planetKey, 0);
                planetStars.put(planetKey, currentPlanetStars + starsEarned);
                userProgress.setPlanetStars(planetStars);
                saveUserProgress();
                
                if (starsEarned > 0) {
                    addStars(starsEarned, "zone_" + zoneId);
                }
                
                // Check if planet is now completed
                if (lessonUnlockManager.isPlanetCompleted(planet.id)) {
                    recordPlanetCompleted(planetKey);
                }
            }
        }
    }
    
    /**
     * Record lesson completion (new unified method)
     * @param planetId Database planet ID
     * @param sceneId Database scene ID
     * @param starsEarned Stars earned from this lesson
     */
    public void recordLessonCompleted(int planetId, int sceneId, int starsEarned) {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"F\",\"location\":\"ProgressionManager.recordLessonCompleted:300\",\"message\":\"recordLessonCompleted entry\",\"data\":{\"planetId\":" + planetId + ",\"sceneId\":" + sceneId + ",\"starsEarned\":" + starsEarned + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        // Complete lesson using LessonUnlockManager
        boolean newLessonUnlocked = lessonUnlockManager.completeLesson(
            planetId, sceneId, starsEarned);
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"F\",\"location\":\"ProgressionManager.recordLessonCompleted:303\",\"message\":\"completeLesson result\",\"data\":{\"planetId\":" + planetId + ",\"sceneId\":" + sceneId + ",\"newLessonUnlocked\":" + newLessonUnlocked + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        
        // Get planet key for tracking
        com.example.engapp.database.GameDatabaseHelper dbHelper = 
            com.example.engapp.database.GameDatabaseHelper.getInstance(context);
        com.example.engapp.database.GameDatabaseHelper.PlanetData planet = 
            dbHelper.getPlanetById(planetId);
        
        if (planet != null) {
            // Update planet stars
            Map<String, Integer> planetStars = userProgress.getPlanetStars();
            int currentPlanetStars = planetStars.getOrDefault(planet.planetKey, 0);
            planetStars.put(planet.planetKey, currentPlanetStars + starsEarned);
            userProgress.setPlanetStars(planetStars);
            saveUserProgress();
            
            if (starsEarned > 0) {
                addStars(starsEarned, "lesson_" + sceneId);
            }
            
            // Check if planet is now completed
            if (lessonUnlockManager.isPlanetCompleted(planetId)) {
                recordPlanetCompleted(planet.planetKey);
            }
        }
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"F\",\"location\":\"ProgressionManager.recordLessonCompleted:328\",\"message\":\"recordLessonCompleted exit\",\"data\":{\"planetId\":" + planetId + ",\"sceneId\":" + sceneId + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
    }

    public void recordPlanetCompleted(String planetKey) {
        int previousPlanets = userProgress.getPlanetsUnlocked();
        userProgress.setPlanetsUnlocked(previousPlanets + 1);
        saveUserProgress();

        // Award planet completion badge
        awardPlanetBadge(planetKey);

        // Check for buddy unlocks based on planets completed
        checkBuddyUnlocks();
        
        // Check for next planet unlock
        checkForNewUnlocks();
    }

    // Planet unlock checking - INTEGRATED WITH LessonUnlockManager
    public void checkForNewUnlocks() {
        int totalStars = userProgress.getTotalStars();
        boolean unlockedAny = false;

        for (Map.Entry<String, Integer> entry : planetUnlockRequirements.entrySet()) {
            String planetKey = entry.getKey();
            int required = entry.getValue();

            // Check if has enough stars AND not already unlocked
            // Planets with required = 0 are always unlocked
            if ((required == 0 || totalStars >= required) && !isPlanetUnlocked(planetKey)) {
                // Use LessonUnlockManager to check and unlock
                lessonUnlockManager.checkAndUnlockPlanet(planetKey, required, totalStars);
                
                // If unlocked, notify listeners
                if (lessonUnlockManager.isPlanetUnlocked(planetKey)) {
                    unlockedAny = true;
                    String planetName = getPlanetDisplayName(planetKey);
                    for (ProgressionEventListener listener : listeners) {
                        listener.onPlanetUnlocked(planetKey, planetName);
                    }
                }
            }
        }

        if (unlockedAny) {
            scheduleCloudSync();
        }
    }

    /**
     * Map GameDataProvider planet ID to database planet key
     * GameDataProvider uses simple IDs like "animal", "color", "number"
     * Database uses keys like "coloria_prime", "toytopia_orbit", "numberia_station"
     * Mapping dựa trên ý nghĩa/chủ đề, không phải thứ tự
     */
    private String mapPlanetIdToKey(String planetId) {
        // Mapping từ GameDataProvider IDs sang database keys dựa trên chủ đề
        switch (planetId) {
            // Galaxy 1: Milky Way
            case "animal": return "animania_wild";      // Animals -> Animania Wild (50 stars)
            case "color": return "coloria_prime";        // Colors -> Coloria Prime (0 stars - first planet)
            case "number": return "numberia_station";   // Numbers -> Numberia Station (100 stars)
            case "food": return "foodora_station";      // Food -> Foodora Station (75 stars)
            
            // Galaxy 2: Andromeda
            case "family": return "familia_home";       // Family -> Familia Home (360 stars)
            case "body": return "animania_wild";        // Body parts -> Animania Wild (50 stars) - fallback
            case "school": return "robolab";            // School -> RoboLab (450 stars)
            case "nature": return "natura";             // Nature -> Natura (780 stars)
            case "home": return "familia_home";         // Home -> Familia Home (360 stars)
            
            // Galaxy 3: Nebula Prime
            case "action": return "timelapse";          // Actions -> TimeLapse (550 stars)
            case "emotion": return "storyverse_galaxy"; // Emotions -> Storyverse (660 stars)
            case "travel": return "natura";             // Travel -> Natura (780 stars)
            
            default: return planetId; // Nếu không có mapping, giữ nguyên (có thể là database key rồi)
        }
    }

    public boolean isPlanetUnlocked(String planetKey) {
        // Map GameDataProvider ID to database key if needed
        String mappedKey = mapPlanetIdToKey(planetKey);
        // Use LessonUnlockManager for unified unlock checking
        return lessonUnlockManager.isPlanetUnlocked(mappedKey);
    }

    public String normalizePlanetKey(String planetId) {
        return mapPlanetIdToKey(planetId);
    }

    private void unlockPlanet(String planetKey) {
        // Use LessonUnlockManager to unlock
        lessonUnlockManager.unlockPlanet(planetKey);
        
        String planetName = getPlanetDisplayName(planetKey);
        for (ProgressionEventListener listener : listeners) {
            listener.onPlanetUnlocked(planetKey, planetName);
        }
    }

    public int getStarsRequiredForPlanet(String planetId) {
        // Map GameDataProvider ID to database key if needed
        String mappedKey = mapPlanetIdToKey(planetId);
        return planetUnlockRequirements.getOrDefault(mappedKey, 
               planetUnlockRequirements.getOrDefault(planetId, 0));
    }

    public float getPlanetUnlockProgress(String planetId) {
        // Map GameDataProvider ID to database key if needed
        String mappedKey = mapPlanetIdToKey(planetId);
        int required = planetUnlockRequirements.getOrDefault(mappedKey, 
                     planetUnlockRequirements.getOrDefault(planetId, 0));
        if (required == 0) return 1.0f;

        int current = userProgress.getTotalStars();
        return Math.min(1.0f, (float) current / required);
    }
    
    /**
     * Get next unlock target (planet or galaxy)
     * Returns the next item that can be unlocked with current stars
     */
    public UnlockTarget getNextUnlockTarget() {
        int totalStars = userProgress.getTotalStars();
        
        // Check planets first
        for (Map.Entry<String, Integer> entry : planetUnlockRequirements.entrySet()) {
            String planetId = entry.getKey();
            int required = entry.getValue();
            
            if (totalStars < required && !isPlanetUnlocked(planetId)) {
                return new UnlockTarget(planetId, getPlanetDisplayName(planetId), required, "planet");
            }
        }
        
        // All planets unlocked
        return null;
    }
    
    /**
     * Get unlock progress info for display
     */
    public UnlockProgressInfo getUnlockProgressInfo() {
        UnlockTarget next = getNextUnlockTarget();
        if (next == null) {
            return new UnlockProgressInfo(0, 0, "All unlocked! 🎉", null);
        }
        
        int current = userProgress.getTotalStars();
        int remaining = next.requiredStars - current;
        String message = remaining > 0 
            ? "Cần thêm " + remaining + " ⭐ để mở khóa " + next.displayName
            : "Sẵn sàng mở khóa " + next.displayName + "! 🚀";
        
        return new UnlockProgressInfo(current, next.requiredStars, message, next);
    }
    
    /**
     * Helper class for unlock target
     */
    public static class UnlockTarget {
        public String id;
        public String displayName;
        public int requiredStars;
        public String type; // "planet" or "galaxy"
        
        public UnlockTarget(String id, String displayName, int requiredStars, String type) {
            this.id = id;
            this.displayName = displayName;
            this.requiredStars = requiredStars;
            this.type = type;
        }
    }
    
    /**
     * Helper class for unlock progress info
     */
    public static class UnlockProgressInfo {
        public int currentStars;
        public int requiredStars;
        public String message;
        public UnlockTarget target;
        
        public UnlockProgressInfo(int currentStars, int requiredStars, String message, UnlockTarget target) {
            this.currentStars = currentStars;
            this.requiredStars = requiredStars;
            this.message = message;
            this.target = target;
        }
    }

    // Badge awarding
    private void awardWordBadge(int wordsCount) {
        String badgeName = wordsCount + " Words Master";
        String badgeNameVi = "Thành thạo " + wordsCount + " từ";
        String emoji = getWordBadgeEmoji(wordsCount);

        Collectible badge = Collectible.createBadge(badgeName, badgeNameVi, emoji);
        badge.setDescription("Learned " + wordsCount + " words!");
        badge.setDescriptionVi("Đã học " + wordsCount + " từ!");
        addCollectible(badge);

        for (ProgressionEventListener listener : listeners) {
            listener.onBadgeEarned(badge);
            listener.onMilestoneReached("words", wordsCount);
        }
    }

    private void awardGameBadge(int gamesCount) {
        String badgeName = gamesCount + " Games Champion";
        String badgeNameVi = "Nhà vô địch " + gamesCount + " trò chơi";
        String emoji = getGameBadgeEmoji(gamesCount);

        Collectible badge = Collectible.createBadge(badgeName, badgeNameVi, emoji);
        badge.setDescription("Completed " + gamesCount + " games!");
        badge.setDescriptionVi("Đã hoàn thành " + gamesCount + " trò chơi!");
        addCollectible(badge);

        for (ProgressionEventListener listener : listeners) {
            listener.onBadgeEarned(badge);
            listener.onMilestoneReached("games", gamesCount);
        }
    }

    private void awardPlanetBadge(String planetId) {
        String planetName = getPlanetDisplayName(planetId);
        String badgeName = planetName + " Explorer";
        String badgeNameVi = "Nhà thám hiểm " + planetName;

        Collectible badge = Collectible.createBadge(badgeName, badgeNameVi, "🏆");
        badge.setDescription("Completed all zones on " + planetName + "!");
        badge.setDescriptionVi("Hoàn thành tất cả khu vực trên " + planetName + "!");
        badge.setSourcePlanetId(planetId);
        addCollectible(badge);

        for (ProgressionEventListener listener : listeners) {
            listener.onBadgeEarned(badge);
        }
    }

    private void addCollectible(Collectible collectible) {
        collectibles.add(0, collectible);
        saveCollectibles();

        for (ProgressionEventListener listener : listeners) {
            listener.onCollectibleAdded(collectible);
        }
    }

    // Buddy unlock checking
    private void checkBuddyUnlocks() {
        int planetsCompleted = userProgress.getPlanetsUnlocked();
        BuddyManager buddyManager = BuddyManager.getInstance(context);

        if (planetsCompleted >= 3) {
            buddyManager.unlockBuddy("dragon");
        }
        if (planetsCompleted >= 5) {
            buddyManager.unlockBuddy("unicorn");
        }
        if (planetsCompleted >= 7) {
            buddyManager.unlockBuddy("panda");
        }
        if (planetsCompleted >= 9) {
            buddyManager.unlockBuddy("lion");
        }
    }

    // Helper methods
    private String getWordBadgeEmoji(int count) {
        if (count >= 200) return "👑";
        if (count >= 100) return "🏆";
        if (count >= 50) return "🥇";
        if (count >= 25) return "🥈";
        return "🥉";
    }

    private String getGameBadgeEmoji(int count) {
        if (count >= 100) return "🎮";
        if (count >= 50) return "🕹️";
        if (count >= 20) return "🎯";
        return "🎲";
    }

    private String getPlanetDisplayName(String planetId) {
        switch (planetId) {
            case "animal": return "Animal Planet";
            case "color": return "Color World";
            case "number": return "Number Station";
            case "food": return "Food Galaxy";
            case "family": return "Family Planet";
            case "body": return "Body World";
            case "school": return "School Station";
            case "nature": return "Nature Planet";
            case "home": return "Home World";
            case "action": return "Action Galaxy";
            case "emotion": return "Emotion Planet";
            case "travel": return "Travel Station";
            case "crystal_world": return "Crystal World";
            case "rainbow_planet": return "Rainbow Planet";
            case "robot_station": return "Robot Station";
            default: return planetId;
        }
    }

    // Daily streak
    public void recordDailyLogin() {
        String lastDate = prefs.getString("last_login_date", "");
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd",
            java.util.Locale.getDefault()).format(new java.util.Date());

        if (!lastDate.equals(today)) {
            prefs.edit().putString("last_login_date", today).apply();

            // Check if consecutive day
            if (isConsecutiveDay(lastDate, today)) {
                int streak = userProgress.getStreakDays() + 1;
                userProgress.setStreakDays(streak);

                // Award streak bonus
                if (streak % 7 == 0) {
                    addBonusStars(50, "weekly_streak");
                    TravelManager.getInstance(context).addFuel(20);
                } else if (streak % 3 == 0) {
                    addBonusStars(20, "streak_bonus");
                    TravelManager.getInstance(context).addFuel(10);
                }
            } else {
                userProgress.setStreakDays(1);
            }
            saveUserProgress();
        }
    }

    private boolean isConsecutiveDay(String lastDate, String today) {
        if (lastDate.isEmpty()) return false;

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd",
                java.util.Locale.getDefault());
            java.util.Date last = sdf.parse(lastDate);
            java.util.Date current = sdf.parse(today);

            long diffInMillis = current.getTime() - last.getTime();
            long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);

            return diffInDays == 1;
        } catch (Exception e) {
            return false;
        }
    }

    public int getStreakDays() {
        return userProgress.getStreakDays();
    }

    // Listeners
    public void addListener(ProgressionEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(ProgressionEventListener listener) {
        listeners.remove(listener);
    }
}

