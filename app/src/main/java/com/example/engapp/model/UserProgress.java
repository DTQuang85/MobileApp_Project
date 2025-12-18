package com.example.engapp.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks user's learning progress, stars, badges, and energy.
 */
public class UserProgress implements Serializable {
    private String odId;
    private int totalStars;
    private int totalEnergy;
    private int totalBadges;
    private int currentLevel;
    private int wordsLearned;
    private int gamesCompleted;
    private int planetsUnlocked;
    private Map<String, Integer> planetStars; // planetId -> stars earned
    private Map<String, Boolean> badges; // badgeId -> earned
    private Map<String, ZoneProgress> zoneProgress;

    public UserProgress() {
        this.totalStars = 0;
        this.totalEnergy = 100;
        this.totalBadges = 0;
        this.currentLevel = 1;
        this.wordsLearned = 0;
        this.gamesCompleted = 0;
        this.planetsUnlocked = 1;
        this.planetStars = new HashMap<>();
        this.badges = new HashMap<>();
        this.zoneProgress = new HashMap<>();
    }

    // Getters and Setters
    public String getodId() { return odId; }
    public void setodId(String odId) { this.odId = odId; }

    public int getTotalStars() { return totalStars; }
    public void setTotalStars(int totalStars) { this.totalStars = totalStars; }

    public int getTotalEnergy() { return totalEnergy; }
    public void setTotalEnergy(int totalEnergy) { this.totalEnergy = totalEnergy; }

    public int getTotalBadges() { return totalBadges; }
    public void setTotalBadges(int totalBadges) { this.totalBadges = totalBadges; }

    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }

    public int getWordsLearned() { return wordsLearned; }
    public void setWordsLearned(int wordsLearned) { this.wordsLearned = wordsLearned; }

    public int getGamesCompleted() { return gamesCompleted; }
    public void setGamesCompleted(int gamesCompleted) { this.gamesCompleted = gamesCompleted; }

    public int getPlanetsUnlocked() { return planetsUnlocked; }
    public void setPlanetsUnlocked(int planetsUnlocked) { this.planetsUnlocked = planetsUnlocked; }

    public Map<String, Integer> getPlanetStars() { return planetStars; }
    public void setPlanetStars(Map<String, Integer> planetStars) { this.planetStars = planetStars; }

    public Map<String, Boolean> getBadges() { return badges; }
    public void setBadges(Map<String, Boolean> badges) { this.badges = badges; }

    public Map<String, ZoneProgress> getZoneProgress() { return zoneProgress; }
    public void setZoneProgress(Map<String, ZoneProgress> zoneProgress) { this.zoneProgress = zoneProgress; }

    public void addStars(int stars) {
        this.totalStars += stars;
        checkLevelUp();
    }

    public void addEnergy(int energy) {
        this.totalEnergy = Math.min(100, this.totalEnergy + energy);
    }

    public boolean useEnergy(int energy) {
        if (this.totalEnergy >= energy) {
            this.totalEnergy -= energy;
            return true;
        }
        return false;
    }

    public void earnBadge(String badgeId) {
        if (!badges.containsKey(badgeId) || !badges.get(badgeId)) {
            badges.put(badgeId, true);
            totalBadges++;
        }
    }

    private void checkLevelUp() {
        int newLevel = (totalStars / 50) + 1;
        if (newLevel > currentLevel) {
            currentLevel = newLevel;
        }
    }

    public static class ZoneProgress implements Serializable {
        private String zoneId;
        private int starsEarned;
        private boolean completed;
        private int highScore;

        public ZoneProgress() {}

        public String getZoneId() { return zoneId; }
        public void setZoneId(String zoneId) { this.zoneId = zoneId; }

        public int getStarsEarned() { return starsEarned; }
        public void setStarsEarned(int starsEarned) { this.starsEarned = starsEarned; }

        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }

        public int getHighScore() { return highScore; }
        public void setHighScore(int highScore) { this.highScore = highScore; }
    }
}
