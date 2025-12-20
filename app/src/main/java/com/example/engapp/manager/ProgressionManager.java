package com.example.engapp.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.engapp.model.Collectible;
import com.example.engapp.model.Planet;
import com.example.engapp.model.UserProgress;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        initPlanetRequirements();
        loadData();
    }

    public static synchronized ProgressionManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProgressionManager(context);
        }
        return instance;
    }

    private void initPlanetRequirements() {
        planetUnlockRequirements = new HashMap<>();

        // Prehistoric Era - Easy start
        planetUnlockRequirements.put("animal", 0);      // Always unlocked
        planetUnlockRequirements.put("color", 20);      // 20 stars
        planetUnlockRequirements.put("number", 50);     // 50 stars

        // Medieval Era - Medium
        planetUnlockRequirements.put("food", 100);      // 100 stars
        planetUnlockRequirements.put("family", 150);    // 150 stars
        planetUnlockRequirements.put("body", 200);      // 200 stars

        // Modern Era - Medium-Hard
        planetUnlockRequirements.put("school", 280);    // 280 stars
        planetUnlockRequirements.put("nature", 360);    // 360 stars
        planetUnlockRequirements.put("home", 450);      // 450 stars

        // Future Era - Hard
        planetUnlockRequirements.put("action", 550);    // 550 stars
        planetUnlockRequirements.put("emotion", 660);   // 660 stars
        planetUnlockRequirements.put("travel", 780);    // 780 stars

        // Bonus fictional planets
        planetUnlockRequirements.put("crystal_world", 900);
        planetUnlockRequirements.put("rainbow_planet", 1000);
        planetUnlockRequirements.put("robot_station", 1100);
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
    }

    private void saveCollectibles() {
        String json = gson.toJson(collectibles);
        prefs.edit().putString(KEY_COLLECTIBLES, json).apply();
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
    }

    // Zone/Planet completion
    public void recordZoneCompleted(String planetId, String zoneId, int starsEarned) {
        Map<String, Integer> planetStars = userProgress.getPlanetStars();
        int currentPlanetStars = planetStars.getOrDefault(planetId, 0);
        planetStars.put(planetId, currentPlanetStars + starsEarned);
        userProgress.setPlanetStars(planetStars);
        saveUserProgress();

        if (starsEarned > 0) {
            addStars(starsEarned, "zone_" + zoneId);
        }
    }

    public void recordPlanetCompleted(String planetId) {
        int previousPlanets = userProgress.getPlanetsUnlocked();
        userProgress.setPlanetsUnlocked(previousPlanets + 1);
        saveUserProgress();

        // Award planet completion badge
        awardPlanetBadge(planetId);

        // Check for buddy unlocks based on planets completed
        checkBuddyUnlocks();
    }

    // Planet unlock checking
    public void checkForNewUnlocks() {
        int totalStars = userProgress.getTotalStars();

        for (Map.Entry<String, Integer> entry : planetUnlockRequirements.entrySet()) {
            String planetId = entry.getKey();
            int required = entry.getValue();

            if (totalStars >= required && !isPlanetUnlocked(planetId)) {
                unlockPlanet(planetId);
            }
        }
    }

    public boolean isPlanetUnlocked(String planetId) {
        // Check if planet is in unlocked list (stored in SharedPreferences)
        String unlockedPlanets = prefs.getString("unlocked_planets", "animal");
        return unlockedPlanets.contains(planetId);
    }

    private void unlockPlanet(String planetId) {
        String unlockedPlanets = prefs.getString("unlocked_planets", "animal");
        if (!unlockedPlanets.contains(planetId)) {
            unlockedPlanets += "," + planetId;
            prefs.edit().putString("unlocked_planets", unlockedPlanets).apply();

            String planetName = getPlanetDisplayName(planetId);
            for (ProgressionEventListener listener : listeners) {
                listener.onPlanetUnlocked(planetId, planetName);
            }
        }
    }

    public int getStarsRequiredForPlanet(String planetId) {
        return planetUnlockRequirements.getOrDefault(planetId, 0);
    }

    public float getPlanetUnlockProgress(String planetId) {
        int required = getStarsRequiredForPlanet(planetId);
        if (required == 0) return 1.0f;

        int current = userProgress.getTotalStars();
        return Math.min(1.0f, (float) current / required);
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

