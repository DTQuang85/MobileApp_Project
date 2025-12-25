package com.example.engapp.manager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.example.engapp.model.Planet;
import com.example.engapp.model.SpaceshipData;
import com.example.engapp.model.TravelLog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages spaceship travel between planets including animations and logging.
 */
public class TravelManager {

    private static TravelManager instance;
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;
    private Handler handler;

    private SpaceshipData spaceshipData;
    private List<TravelLog> travelLogs;
    private String currentPlanetId;
    private boolean isTraveling;

    private List<TravelEventListener> listeners;

    private static final String PREFS_NAME = "travel_prefs";
    private static final String KEY_SPACESHIP_DATA = "spaceship_data";
    private static final String KEY_TRAVEL_LOGS = "travel_logs";
    private static final String KEY_CURRENT_PLANET = "current_planet";

    // Travel phases
    public static final int PHASE_PRELAUNCH = 0;
    public static final int PHASE_TAKEOFF = 1;
    public static final int PHASE_TRAVELING = 2;
    public static final int PHASE_APPROACH = 3;
    public static final int PHASE_LANDING = 4;
    public static final int PHASE_COMPLETE = 5;

    // Default travel durations in milliseconds
    public static final long DURATION_PRELAUNCH = 1500;
    public static final long DURATION_TAKEOFF = 2000;
    public static final long DURATION_TRAVEL = 3500;
    public static final long DURATION_APPROACH = 2000;
    public static final long DURATION_LANDING = 2000;

    public interface TravelEventListener {
        void onTravelPhaseChanged(int phase, String phaseName);
        void onTravelProgress(float progress); // 0.0 to 1.0
        void onTravelComplete(Planet destination);
        void onTravelCancelled();
        void onFuelChanged(int currentFuel, int maxFuel);
    }

    private TravelManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.handler = new Handler(Looper.getMainLooper());
        this.listeners = new ArrayList<>();
        this.isTraveling = false;

        loadData();
    }

    public static synchronized TravelManager getInstance(Context context) {
        if (instance == null) {
            instance = new TravelManager(context);
        }
        return instance;
    }

    private void loadData() {
        // Load spaceship data
        String spaceshipJson = prefs.getString(KEY_SPACESHIP_DATA, null);
        if (spaceshipJson != null) {
            spaceshipData = gson.fromJson(spaceshipJson, SpaceshipData.class);
        } else {
            spaceshipData = new SpaceshipData();
            saveSpaceshipData();
        }

        // Apply fuel regeneration
        spaceshipData.applyFuelRegeneration();
        saveSpaceshipData();

        // Load travel logs
        String logsJson = prefs.getString(KEY_TRAVEL_LOGS, null);
        if (logsJson != null) {
            Type listType = new TypeToken<ArrayList<TravelLog>>(){}.getType();
            travelLogs = gson.fromJson(logsJson, listType);
        } else {
            travelLogs = new ArrayList<>();
        }

        // Load current planet
        currentPlanetId = prefs.getString(KEY_CURRENT_PLANET, "animal");
    }

    public void saveSpaceshipData() {
        String json = gson.toJson(spaceshipData);
        prefs.edit().putString(KEY_SPACESHIP_DATA, json).apply();
    }

    private void saveTravelLogs() {
        String json = gson.toJson(travelLogs);
        prefs.edit().putString(KEY_TRAVEL_LOGS, json).apply();
    }

    private void saveCurrentPlanet() {
        prefs.edit().putString(KEY_CURRENT_PLANET, currentPlanetId).apply();
    }

    // Getters
    public SpaceshipData getSpaceshipData() {
        return spaceshipData;
    }

    public List<TravelLog> getTravelLogs() {
        return new ArrayList<>(travelLogs);
    }

    public String getCurrentPlanetId() {
        return currentPlanetId;
    }

    public void setCurrentPlanetId(String planetId) {
        if (planetId == null || planetId.isEmpty()) {
            return;
        }
        currentPlanetId = planetId;
        saveCurrentPlanet();
    }

    public boolean isTraveling() {
        return isTraveling;
    }

    public int getFuelCells() {
        return spaceshipData.getFuelCells();
    }

    public int getMaxFuelCells() {
        return spaceshipData.getMaxFuelCells();
    }

    // Travel calculations
    public int calculateFuelCost(Planet from, Planet to) {
        // Base fuel cost is 10, can be modified based on distance or difficulty
        return 10;
    }

    public long calculateTravelDuration(Planet from, Planet to) {
        return DURATION_PRELAUNCH + DURATION_TAKEOFF + DURATION_TRAVEL +
               DURATION_APPROACH + DURATION_LANDING;
    }

    public boolean canTravelTo(Planet destination) {
        if (isTraveling) return false;
        if (!destination.isUnlocked()) return false;

        int fuelCost = calculateFuelCost(null, destination);
        return spaceshipData.getFuelCells() >= fuelCost;
    }

    // Travel execution
    public void travelTo(Planet destination, TravelAnimationCallback animationCallback) {
        if (!canTravelTo(destination)) {
            return;
        }

        isTraveling = true;
        String fromPlanetId = currentPlanetId;

        // Use fuel
        int fuelCost = calculateFuelCost(null, destination);
        spaceshipData.useFuel(fuelCost);
        saveSpaceshipData();
        notifyFuelChanged();

        // Create travel log
        TravelLog log = new TravelLog(fromPlanetId, "", destination.getId(), destination.getName());
        log.setPlanetEmoji(destination.getEmoji());

        // Execute travel phases
        executeTravelSequence(destination, log, animationCallback);
    }

    private void executeTravelSequence(Planet destination, TravelLog log,
                                        TravelAnimationCallback callback) {
        // Phase 1: Pre-launch
        notifyPhaseChanged(PHASE_PRELAUNCH, "Pre-launch");
        if (callback != null) callback.onPreLaunch();

        handler.postDelayed(() -> {
            // Phase 2: Takeoff
            notifyPhaseChanged(PHASE_TAKEOFF, "Takeoff");
            if (callback != null) callback.onTakeoff();

            handler.postDelayed(() -> {
                // Phase 3: Traveling
                notifyPhaseChanged(PHASE_TRAVELING, "Traveling");
                if (callback != null) callback.onTraveling();
                startTravelProgressUpdates(DURATION_TRAVEL);

                handler.postDelayed(() -> {
                    // Phase 4: Approach
                    notifyPhaseChanged(PHASE_APPROACH, "Approach");
                    if (callback != null) callback.onApproach(destination);

                    handler.postDelayed(() -> {
                        // Phase 5: Landing
                        notifyPhaseChanged(PHASE_LANDING, "Landing");
                        if (callback != null) callback.onLanding();

                        handler.postDelayed(() -> {
                            // Phase 6: Complete
                            notifyPhaseChanged(PHASE_COMPLETE, "Complete");
                            completeTravelTo(destination, log);
                            if (callback != null) callback.onComplete(destination);

                        }, DURATION_LANDING);
                    }, DURATION_APPROACH);
                }, DURATION_TRAVEL);
            }, DURATION_TAKEOFF);
        }, DURATION_PRELAUNCH);
    }

    private void startTravelProgressUpdates(long duration) {
        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + duration;

        Runnable progressUpdater = new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                float progress = (float)(now - startTime) / duration;
                progress = Math.min(1.0f, Math.max(0.0f, progress));

                notifyProgress(progress);

                if (now < endTime && isTraveling) {
                    handler.postDelayed(this, 100); // Update every 100ms
                }
            }
        };
        handler.post(progressUpdater);
    }

    private void completeTravelTo(Planet destination, TravelLog log) {
        isTraveling = false;
        currentPlanetId = destination.getId();
        saveCurrentPlanet();

        // Record trip statistics
        spaceshipData.recordTrip(100); // simplified distance
        saveSpaceshipData();

        // Add travel log
        travelLogs.add(0, log); // Add to beginning
        if (travelLogs.size() > 50) { // Keep only last 50 logs
            travelLogs = travelLogs.subList(0, 50);
        }
        saveTravelLogs();

        // Notify listeners
        for (TravelEventListener listener : listeners) {
            listener.onTravelComplete(destination);
        }
    }

    public void cancelTravel() {
        if (isTraveling) {
            isTraveling = false;
            handler.removeCallbacksAndMessages(null);

            for (TravelEventListener listener : listeners) {
                listener.onTravelCancelled();
            }
        }
    }

    // Fuel management
    public void addFuel(int amount) {
        spaceshipData.addFuel(amount);
        saveSpaceshipData();
        notifyFuelChanged();
    }

    public void refuelFromReward(int amount) {
        addFuel(amount);
    }

    // Spaceship customization
    public void setSpaceshipName(String name) {
        spaceshipData.setSpaceshipName(name);
        saveSpaceshipData();
    }

    public void setSpaceshipColors(String primary, String secondary, String trail) {
        spaceshipData.setPrimaryColor(primary);
        spaceshipData.setSecondaryColor(secondary);
        spaceshipData.setEngineTrailColor(trail);
        saveSpaceshipData();
    }

    public void unlockSpaceship(String type) {
        spaceshipData.unlockSpaceship(type);
        saveSpaceshipData();
    }

    public void selectSpaceship(String type) {
        if (spaceshipData.isSpaceshipUnlocked(type)) {
            spaceshipData.setSpaceshipType(type);
            saveSpaceshipData();
        }
    }

    // Animation helpers
    public static AnimatorSet createSpaceshipTakeoffAnimation(View spaceship) {
        ObjectAnimator moveUp = ObjectAnimator.ofFloat(spaceship, "translationY", 0, -500);
        moveUp.setDuration(DURATION_TAKEOFF);
        moveUp.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(spaceship, "scaleX", 1f, 0.5f);
        scaleX.setDuration(DURATION_TAKEOFF);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(spaceship, "scaleY", 1f, 0.5f);
        scaleY.setDuration(DURATION_TAKEOFF);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(moveUp, scaleX, scaleY);
        return set;
    }

    public static AnimatorSet createSpaceshipLandingAnimation(View spaceship) {
        spaceship.setTranslationY(-500);
        spaceship.setScaleX(0.5f);
        spaceship.setScaleY(0.5f);

        ObjectAnimator moveDown = ObjectAnimator.ofFloat(spaceship, "translationY", -500, 0);
        moveDown.setDuration(DURATION_LANDING);
        moveDown.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(spaceship, "scaleX", 0.5f, 1f);
        scaleX.setDuration(DURATION_LANDING);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(spaceship, "scaleY", 0.5f, 1f);
        scaleY.setDuration(DURATION_LANDING);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(moveDown, scaleX, scaleY);
        return set;
    }

    public static ObjectAnimator createEngineGlowAnimation(View engineGlow) {
        ObjectAnimator pulse = ObjectAnimator.ofFloat(engineGlow, "alpha", 0.3f, 1f, 0.3f);
        pulse.setDuration(500);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        return pulse;
    }

    public static AnimatorSet createStarStreakAnimation(View star, int direction) {
        float startX = direction > 0 ? 0 : 1000;
        float endX = direction > 0 ? 1000 : 0;

        ObjectAnimator moveX = ObjectAnimator.ofFloat(star, "translationX", startX, endX);
        moveX.setDuration(300);

        ObjectAnimator fade = ObjectAnimator.ofFloat(star, "alpha", 1f, 0f);
        fade.setDuration(300);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(moveX, fade);
        return set;
    }

    // Notification helpers
    private void notifyPhaseChanged(int phase, String phaseName) {
        for (TravelEventListener listener : listeners) {
            listener.onTravelPhaseChanged(phase, phaseName);
        }
    }

    private void notifyProgress(float progress) {
        for (TravelEventListener listener : listeners) {
            listener.onTravelProgress(progress);
        }
    }

    private void notifyFuelChanged() {
        for (TravelEventListener listener : listeners) {
            listener.onFuelChanged(spaceshipData.getFuelCells(), spaceshipData.getMaxFuelCells());
        }
    }

    // Listeners
    public void addListener(TravelEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(TravelEventListener listener) {
        listeners.remove(listener);
    }

    // Callback interface for animations
    public interface TravelAnimationCallback {
        void onPreLaunch();
        void onTakeoff();
        void onTraveling();
        void onApproach(Planet destination);
        void onLanding();
        void onComplete(Planet destination);
    }
}
