package com.example.engapp.model;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a planet in the space learning game.
 * Each planet contains multiple zones with learning content.
 */
public class Planet implements Serializable {
    private String id;
    private String name;
    private String nameVi;
    private String emoji;
    private int color;
    private String timeEra; // prehistoric, medieval, modern, future
    private List<Zone> zones;
    private boolean isUnlocked;
    private int requiredStars;
    private int starsEarned;
    private String backgroundImage;

    // New fields for interactive star map
    private float mapPositionX;
    private float mapPositionY;
    private float orbitRadius;
    private float orbitSpeed;
    private boolean isRealPlanet; // Solar System vs fictional
    private String buddyComment;
    private String unlockAnimationType;

    public Planet() {}

    public Planet(String id, String name, String nameVi, String emoji, int color, String timeEra) {
        this.id = id;
        this.name = name;
        this.nameVi = nameVi;
        this.emoji = emoji;
        this.color = color;
        this.timeEra = timeEra;
        this.isUnlocked = false;
        this.requiredStars = 0;
        this.starsEarned = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameVi() { return nameVi; }
    public void setNameVi(String nameVi) { this.nameVi = nameVi; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public String getTimeEra() { return timeEra; }
    public void setTimeEra(String timeEra) { this.timeEra = timeEra; }

    public List<Zone> getZones() { return zones; }
    public void setZones(List<Zone> zones) { this.zones = zones; }

    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }

    public int getRequiredStars() { return requiredStars; }
    public void setRequiredStars(int requiredStars) { this.requiredStars = requiredStars; }

    public int getStarsEarned() { return starsEarned; }
    public void setStarsEarned(int starsEarned) { this.starsEarned = starsEarned; }

    public String getBackgroundImage() { return backgroundImage; }
    public void setBackgroundImage(String backgroundImage) { this.backgroundImage = backgroundImage; }

    // New getters and setters for star map
    public float getMapPositionX() { return mapPositionX; }
    public void setMapPositionX(float mapPositionX) { this.mapPositionX = mapPositionX; }

    public float getMapPositionY() { return mapPositionY; }
    public void setMapPositionY(float mapPositionY) { this.mapPositionY = mapPositionY; }

    public float getOrbitRadius() { return orbitRadius; }
    public void setOrbitRadius(float orbitRadius) { this.orbitRadius = orbitRadius; }

    public float getOrbitSpeed() { return orbitSpeed; }
    public void setOrbitSpeed(float orbitSpeed) { this.orbitSpeed = orbitSpeed; }

    public boolean isRealPlanet() { return isRealPlanet; }
    public void setRealPlanet(boolean realPlanet) { isRealPlanet = realPlanet; }

    public String getBuddyComment() { return buddyComment; }
    public void setBuddyComment(String buddyComment) { this.buddyComment = buddyComment; }

    public String getUnlockAnimationType() { return unlockAnimationType; }
    public void setUnlockAnimationType(String unlockAnimationType) { this.unlockAnimationType = unlockAnimationType; }

    public int getProgress() {
        if (zones == null || zones.isEmpty()) return 0;
        int completed = 0;
        for (Zone zone : zones) {
            if (zone.isCompleted()) completed++;
        }
        return (completed * 100) / zones.size();
    }
}
