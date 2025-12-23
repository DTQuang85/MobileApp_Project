package com.example.engapp.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.engapp.database.GameDatabaseHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Quản lý hệ thống mở khóa bài học (lesson/node) và hành tinh
 * 
 * QUY TẮC MỞ KHÓA:
 * 1. Lesson đầu tiên của mỗi planet luôn được mở khóa
 * 2. Lesson tiếp theo được mở khóa khi lesson trước đó hoàn thành (is_completed = 1)
 * 3. Planet được mở khóa khi:
 *    - Đạt đủ số sao yêu cầu (từ ProgressionManager)
 *    - VÀ tất cả lessons của planet trước đó đã hoàn thành (nếu có)
 * 4. Galaxy được mở khóa khi đạt đủ số sao yêu cầu
 */
public class LessonUnlockManager {

    private static LessonUnlockManager instance;
    private Context context;
    private GameDatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private Gson gson;

    private static final String PREFS_NAME = "lesson_unlock_prefs";
    private static final String KEY_UNLOCKED_LESSONS = "unlocked_lessons"; // Set<String> "planetId_sceneId"
    private static final String KEY_COMPLETED_LESSONS = "completed_lessons"; // Set<String> "planetId_sceneId"
    private static final String KEY_UNLOCKED_PLANETS = "unlocked_planets_set"; // Set<String> planet keys
    private static final String KEY_UNLOCKED_GALAXIES = "unlocked_galaxies_set"; // Set<String> galaxy keys

    private LessonUnlockManager(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = GameDatabaseHelper.getInstance(this.context);
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        
        // Initialize first lesson of first planet
        initializeFirstLesson();
        
        // Initialize first planet (animal) - always unlocked
        initializeFirstPlanet();
    }
    
    /**
     * Khởi tạo planet đầu tiên (animal) - luôn được mở khóa
     */
    private void initializeFirstPlanet() {
        Set<String> unlockedPlanets = getUnlockedPlanets();
        if (!unlockedPlanets.contains("animal")) {
            unlockPlanet("animal");
        }
    }

    public static synchronized LessonUnlockManager getInstance(Context context) {
        if (instance == null) {
            instance = new LessonUnlockManager(context);
        }
        return instance;
    }

    /**
     * Khởi tạo lesson đầu tiên của planet đầu tiên
     */
    private void initializeFirstLesson() {
        Set<String> unlockedLessons = getUnlockedLessons();
        if (unlockedLessons.isEmpty()) {
            // Unlock first scene of first planet (planet_id = 1, order_index = 1)
            unlockLesson(1, 1);
        }
    }

    // ============ LESSON UNLOCK METHODS ============

    /**
     * Kiểm tra xem một lesson (scene) có được mở khóa không
     * @param planetId ID của planet
     * @param sceneId ID của scene/lesson
     * @return true nếu lesson đã được mở khóa
     */
    public boolean isLessonUnlocked(int planetId, int sceneId) {
        String key = planetId + "_" + sceneId;
        return getUnlockedLessons().contains(key);
    }

    /**
     * Kiểm tra xem một lesson có được mở khóa không (dựa trên orderIndex)
     * @param planetId ID của planet
     * @param orderIndex Thứ tự của lesson (1-based)
     * @return true nếu lesson đã được mở khóa
     */
    public boolean isLessonUnlockedByOrder(int planetId, int orderIndex) {
        List<GameDatabaseHelper.SceneData> scenes = dbHelper.getScenesForPlanet(planetId);
        if (scenes == null || orderIndex < 1 || orderIndex > scenes.size()) {
            return false;
        }
        
        // Find scene with matching orderIndex
        for (GameDatabaseHelper.SceneData scene : scenes) {
            if (scene.orderIndex == orderIndex) {
                return isLessonUnlocked(planetId, scene.id);
            }
        }
        return false;
    }

    /**
     * Mở khóa một lesson
     * @param planetId ID của planet
     * @param sceneId ID của scene/lesson
     */
    public void unlockLesson(int planetId, int sceneId) {
        String key = planetId + "_" + sceneId;
        Set<String> unlocked = getUnlockedLessons();
        if (unlocked.add(key)) {
            saveUnlockedLessons(unlocked);
            
            // Also update database
            dbHelper.updateSceneUnlockStatus(sceneId, true);
        }
    }

    /**
     * Đánh dấu một lesson đã hoàn thành và mở khóa lesson tiếp theo
     * @param planetId ID của planet
     * @param sceneId ID của scene/lesson vừa hoàn thành
     * @param starsEarned Số sao đạt được
     * @return true nếu có lesson mới được mở khóa
     */
    public boolean completeLesson(int planetId, int sceneId, int starsEarned) {
        // Mark lesson as completed
        String key = planetId + "_" + sceneId;
        Set<String> completed = getCompletedLessons();
        boolean wasAlreadyCompleted = completed.contains(key);
        
        if (!wasAlreadyCompleted) {
            completed.add(key);
            saveCompletedLessons(completed);
            
            // Update database
            dbHelper.updateSceneProgress(sceneId, starsEarned);
        }

        // Unlock next lesson in the same planet
        boolean newLessonUnlocked = unlockNextLesson(planetId, sceneId);
        
        // Check if planet is completed (all lessons done)
        checkPlanetCompletion(planetId);
        
        return newLessonUnlocked;
    }

    /**
     * Mở khóa lesson tiếp theo trong cùng planet
     * @param planetId ID của planet
     * @param completedSceneId ID của scene vừa hoàn thành
     * @return true nếu có lesson mới được mở khóa
     */
    private boolean unlockNextLesson(int planetId, int completedSceneId) {
        List<GameDatabaseHelper.SceneData> scenes = dbHelper.getScenesForPlanet(planetId);
        if (scenes == null || scenes.isEmpty()) {
            return false;
        }

        // Find completed scene's orderIndex
        int completedOrder = -1;
        for (GameDatabaseHelper.SceneData scene : scenes) {
            if (scene.id == completedSceneId) {
                completedOrder = scene.orderIndex;
                break;
            }
        }

        if (completedOrder == -1) {
            return false;
        }

        // Find next scene (orderIndex = completedOrder + 1)
        int nextOrder = completedOrder + 1;
        for (GameDatabaseHelper.SceneData scene : scenes) {
            if (scene.orderIndex == nextOrder) {
                // Unlock next lesson
                if (!isLessonUnlocked(planetId, scene.id)) {
                    unlockLesson(planetId, scene.id);
                    return true;
                }
                break;
            }
        }

        return false;
    }
    
    /**
     * Refresh unlock status for all lessons in a planet
     * Useful when returning to planet map to ensure UI shows correct status
     */
    public void refreshPlanetLessonsUnlockStatus(int planetId) {
        refreshPlanetLessons(planetId);
    }

    /**
     * Kiểm tra và cập nhật trạng thái mở khóa của tất cả lessons trong một planet
     * Dựa trên quy tắc: lesson N được mở khóa khi lesson N-1 đã hoàn thành
     */
    public void refreshPlanetLessons(int planetId) {
        List<GameDatabaseHelper.SceneData> scenes = dbHelper.getScenesForPlanet(planetId);
        if (scenes == null || scenes.isEmpty()) {
            return;
        }

        // Sort by orderIndex
        scenes.sort((a, b) -> Integer.compare(a.orderIndex, b.orderIndex));

        // First lesson is always unlocked
        if (!scenes.isEmpty()) {
            unlockLesson(planetId, scenes.get(0).id);
        }

        // Unlock subsequent lessons if previous one is completed
        for (int i = 1; i < scenes.size(); i++) {
            GameDatabaseHelper.SceneData previousScene = scenes.get(i - 1);
            GameDatabaseHelper.SceneData currentScene = scenes.get(i);

            if (isLessonCompleted(planetId, previousScene.id)) {
                unlockLesson(planetId, currentScene.id);
            }
        }
    }

    /**
     * Kiểm tra xem một lesson đã hoàn thành chưa
     */
    public boolean isLessonCompleted(int planetId, int sceneId) {
        String key = planetId + "_" + sceneId;
        return getCompletedLessons().contains(key);
    }

    /**
     * Lấy số lesson đã hoàn thành trong một planet
     */
    public int getCompletedLessonsCount(int planetId) {
        List<GameDatabaseHelper.SceneData> scenes = dbHelper.getScenesForPlanet(planetId);
        if (scenes == null) {
            return 0;
        }

        int count = 0;
        for (GameDatabaseHelper.SceneData scene : scenes) {
            if (isLessonCompleted(planetId, scene.id)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Kiểm tra xem một planet đã hoàn thành tất cả lessons chưa
     */
    public boolean isPlanetCompleted(int planetId) {
        List<GameDatabaseHelper.SceneData> scenes = dbHelper.getScenesForPlanet(planetId);
        if (scenes == null || scenes.isEmpty()) {
            return false;
        }

        for (GameDatabaseHelper.SceneData scene : scenes) {
            if (!isLessonCompleted(planetId, scene.id)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Kiểm tra và xử lý khi planet hoàn thành
     */
    private void checkPlanetCompletion(int planetId) {
        if (isPlanetCompleted(planetId)) {
            // Planet completed - could trigger rewards, badges, etc.
            // This is handled by ProgressionManager
        }
    }

    // ============ PLANET UNLOCK METHODS ============

    /**
     * Kiểm tra xem một planet có được mở khóa không
     * Planet được mở khóa khi:
     * 1. Đạt đủ số sao yêu cầu (checked by ProgressionManager)
     * 2. VÀ được đánh dấu là unlocked trong LessonUnlockManager
     */
    public boolean isPlanetUnlocked(String planetKey) {
        return getUnlockedPlanets().contains(planetKey);
    }

    /**
     * Mở khóa một planet
     * @param planetKey Key của planet (ví dụ: "animal", "color")
     */
    public void unlockPlanet(String planetKey) {
        Set<String> unlocked = getUnlockedPlanets();
        if (unlocked.add(planetKey)) {
            saveUnlockedPlanets(unlocked);
            
            // Unlock first lesson of the planet
            GameDatabaseHelper.PlanetData planet = dbHelper.getPlanetByKey(planetKey);
            if (planet != null) {
                refreshPlanetLessons(planet.id);
            }
        }
    }

    /**
     * Kiểm tra và mở khóa planet dựa trên số sao
     * Được gọi bởi ProgressionManager sau khi user đạt đủ sao
     * ĐÃ ĐƠN GIẢN HÓA: Chỉ cần đủ sao là mở khóa, không cần hoàn thành planet trước
     */
    public void checkAndUnlockPlanet(String planetKey, int requiredStars, int currentStars) {
        // Check if already unlocked
        if (isPlanetUnlocked(planetKey)) {
            return;
        }

        // Check if has enough stars - ĐƠN GIẢN: chỉ cần đủ sao
        if (currentStars >= requiredStars) {
            unlockPlanet(planetKey);
        }
    }

    /**
     * Kiểm tra xem planet trước đó đã hoàn thành chưa
     */
    private boolean isPreviousPlanetCompleted(String planetKey) {
        // Get planet order
        GameDatabaseHelper.PlanetData planet = dbHelper.getPlanetByKey(planetKey);
        if (planet == null) {
            return false;
        }

        // First planet in galaxy is always available
        if (planet.orderIndex == 1) {
            return true;
        }

        // Find previous planet in same galaxy
        List<GameDatabaseHelper.PlanetData> allPlanets = dbHelper.getAllPlanets();
        for (GameDatabaseHelper.PlanetData p : allPlanets) {
            if (p.galaxyId == planet.galaxyId && p.orderIndex == planet.orderIndex - 1) {
                return isPlanetCompleted(p.id);
            }
        }

        return false;
    }

    // ============ GALAXY UNLOCK METHODS ============

    /**
     * Kiểm tra xem một galaxy có được mở khóa không
     */
    public boolean isGalaxyUnlocked(String galaxyKey) {
        return getUnlockedGalaxies().contains(galaxyKey);
    }

    /**
     * Mở khóa một galaxy
     */
    public void unlockGalaxy(String galaxyKey) {
        Set<String> unlocked = getUnlockedGalaxies();
        if (unlocked.add(galaxyKey)) {
            saveUnlockedGalaxies(unlocked);
        }
    }

    // ============ HELPER METHODS ============

    private Set<String> getUnlockedLessons() {
        String json = prefs.getString(KEY_UNLOCKED_LESSONS, "[]");
        Type type = new TypeToken<Set<String>>(){}.getType();
        Set<String> set = gson.fromJson(json, type);
        return set != null ? set : new HashSet<>();
    }

    private void saveUnlockedLessons(Set<String> lessons) {
        String json = gson.toJson(lessons);
        prefs.edit().putString(KEY_UNLOCKED_LESSONS, json).apply();
    }

    private Set<String> getCompletedLessons() {
        String json = prefs.getString(KEY_COMPLETED_LESSONS, "[]");
        Type type = new TypeToken<Set<String>>(){}.getType();
        Set<String> set = gson.fromJson(json, type);
        return set != null ? set : new HashSet<>();
    }

    private void saveCompletedLessons(Set<String> lessons) {
        String json = gson.toJson(lessons);
        prefs.edit().putString(KEY_COMPLETED_LESSONS, json).apply();
    }

    private Set<String> getUnlockedPlanets() {
        String json = prefs.getString(KEY_UNLOCKED_PLANETS, "[]");
        Type type = new TypeToken<Set<String>>(){}.getType();
        Set<String> set = gson.fromJson(json, type);
        return set != null ? set : new HashSet<>();
    }

    private void saveUnlockedPlanets(Set<String> planets) {
        String json = gson.toJson(planets);
        prefs.edit().putString(KEY_UNLOCKED_PLANETS, json).apply();
    }

    private Set<String> getUnlockedGalaxies() {
        String json = prefs.getString(KEY_UNLOCKED_GALAXIES, "[]");
        Type type = new TypeToken<Set<String>>(){}.getType();
        Set<String> set = gson.fromJson(json, type);
        return set != null ? set : new HashSet<>();
    }

    private void saveUnlockedGalaxies(Set<String> galaxies) {
        String json = gson.toJson(galaxies);
        prefs.edit().putString(KEY_UNLOCKED_GALAXIES, json).apply();
    }

    /**
     * Reset tất cả progress (for testing/debugging)
     */
    public void resetAllProgress() {
        prefs.edit()
            .remove(KEY_UNLOCKED_LESSONS)
            .remove(KEY_COMPLETED_LESSONS)
            .remove(KEY_UNLOCKED_PLANETS)
            .remove(KEY_UNLOCKED_GALAXIES)
            .apply();
        initializeFirstLesson();
    }

    /**
     * Unlock tất cả planets và lessons - CHỈ DÙNG CHO TEST
     * Gọi method này để unlock tất cả nội dung cho việc kiểm thử
     */
    public void unlockAllForTesting() {
        // Unlock tất cả planets
        List<GameDatabaseHelper.PlanetData> allPlanets = dbHelper.getAllPlanets();
        Set<String> unlockedPlanets = new HashSet<>();
        for (GameDatabaseHelper.PlanetData planet : allPlanets) {
            unlockedPlanets.add(planet.planetKey);
            // Unlock tất cả lessons của mỗi planet
            List<GameDatabaseHelper.SceneData> scenes = dbHelper.getScenesForPlanet(planet.id);
            if (scenes != null) {
                for (GameDatabaseHelper.SceneData scene : scenes) {
                    unlockLesson(planet.id, scene.id);
                }
            }
        }
        saveUnlockedPlanets(unlockedPlanets);

        // Unlock tất cả galaxies
        Set<String> unlockedGalaxies = new HashSet<>();
        unlockedGalaxies.add("beginner");
        unlockedGalaxies.add("explorer");
        unlockedGalaxies.add("advanced");
        saveUnlockedGalaxies(unlockedGalaxies);
    }
}

