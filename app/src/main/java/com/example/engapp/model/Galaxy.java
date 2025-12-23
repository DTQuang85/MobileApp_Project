package com.example.engapp.model;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a Galaxy in the space learning game.
 * Each Galaxy contains multiple Planets with different themes.
 * Phase 2 of TODO: Bản đồ ngân hà
 */
public class Galaxy implements Serializable {
    private int id;
    private String galaxyKey;
    private String name;
    private String nameVi;
    private String description;
    private String emoji;
    private String themeColor;
    private String backgroundImage;
    private int orderIndex;
    private boolean isUnlocked;
    private int requiredStars;      // Stars needed to unlock this galaxy
    private int totalPlanets;       // Number of planets in this galaxy
    private int completedPlanets;   // Planets completed by user

    // Visual properties for map
    private float mapPositionX;
    private float mapPositionY;
    private float scale;
    private String warpAnimationType;

    // List of planets in this galaxy
    private List<Planet> planets;

    public Galaxy() {}

    public Galaxy(int id, String galaxyKey, String name, String nameVi, String description,
                  String emoji, String themeColor, int orderIndex) {
        this.id = id;
        this.galaxyKey = galaxyKey;
        this.name = name;
        this.nameVi = nameVi;
        this.description = description;
        this.emoji = emoji;
        this.themeColor = themeColor;
        this.orderIndex = orderIndex;
        this.isUnlocked = false;
        this.requiredStars = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getGalaxyKey() { return galaxyKey; }
    public void setGalaxyKey(String galaxyKey) { this.galaxyKey = galaxyKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameVi() { return nameVi; }
    public void setNameVi(String nameVi) { this.nameVi = nameVi; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getThemeColor() { return themeColor; }
    public void setThemeColor(String themeColor) { this.themeColor = themeColor; }

    public String getBackgroundImage() { return backgroundImage; }
    public void setBackgroundImage(String backgroundImage) { this.backgroundImage = backgroundImage; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }

    public int getRequiredStars() { return requiredStars; }
    public void setRequiredStars(int requiredStars) { this.requiredStars = requiredStars; }

    public int getTotalPlanets() { return totalPlanets; }
    public void setTotalPlanets(int totalPlanets) { this.totalPlanets = totalPlanets; }

    public int getCompletedPlanets() { return completedPlanets; }
    public void setCompletedPlanets(int completedPlanets) { this.completedPlanets = completedPlanets; }

    public float getMapPositionX() { return mapPositionX; }
    public void setMapPositionX(float mapPositionX) { this.mapPositionX = mapPositionX; }

    public float getMapPositionY() { return mapPositionY; }
    public void setMapPositionY(float mapPositionY) { this.mapPositionY = mapPositionY; }

    public float getScale() { return scale; }
    public void setScale(float scale) { this.scale = scale; }

    public String getWarpAnimationType() { return warpAnimationType; }
    public void setWarpAnimationType(String warpAnimationType) { this.warpAnimationType = warpAnimationType; }

    public List<Planet> getPlanets() { return planets; }
    public void setPlanets(List<Planet> planets) { this.planets = planets; }

    /**
     * Calculate progress percentage for this galaxy
     */
    public int getProgressPercent() {
        if (totalPlanets == 0) return 0;
        return (completedPlanets * 100) / totalPlanets;
    }

    /**
     * Check if galaxy is completed
     */
    public boolean isCompleted() {
        return totalPlanets > 0 && completedPlanets >= totalPlanets;
    }
}

