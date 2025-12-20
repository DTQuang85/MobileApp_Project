package com.example.engapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the user's spaceship data for customization and travel.
 */
public class SpaceshipData implements Serializable {

    // Spaceship types
    public static final String TYPE_STARTER = "starter";
    public static final String TYPE_EXPLORER = "explorer";
    public static final String TYPE_CRUISER = "cruiser";
    public static final String TYPE_WARP = "warp";
    public static final String TYPE_GALAXY = "galaxy";

    private String odId;
    private String spaceshipType;
    private String spaceshipName;
    private String primaryColor;
    private String secondaryColor;
    private String engineTrailColor;
    private List<String> equippedDecorations;
    private List<String> unlockedSpaceships;
    private int fuelCells;
    private int maxFuelCells;
    private int totalTrips;
    private long totalDistanceTraveled;
    private long lastRefuelTime;

    public SpaceshipData() {
        this.spaceshipType = TYPE_STARTER;
        this.spaceshipName = "My Rocket";
        this.primaryColor = "#4ECDC4";
        this.secondaryColor = "#2C3E50";
        this.engineTrailColor = "#FF6B6B";
        this.equippedDecorations = new ArrayList<>();
        this.unlockedSpaceships = new ArrayList<>();
        this.unlockedSpaceships.add(TYPE_STARTER);
        this.fuelCells = 100;
        this.maxFuelCells = 100;
        this.totalTrips = 0;
        this.totalDistanceTraveled = 0;
        this.lastRefuelTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getodId() { return odId; }
    public void setodId(String odId) { this.odId = odId; }

    public String getSpaceshipType() { return spaceshipType; }
    public void setSpaceshipType(String spaceshipType) { this.spaceshipType = spaceshipType; }

    public String getSpaceshipName() { return spaceshipName; }
    public void setSpaceshipName(String spaceshipName) { this.spaceshipName = spaceshipName; }

    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }

    public String getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }

    public String getEngineTrailColor() { return engineTrailColor; }
    public void setEngineTrailColor(String engineTrailColor) { this.engineTrailColor = engineTrailColor; }

    public List<String> getEquippedDecorations() { return equippedDecorations; }
    public void setEquippedDecorations(List<String> equippedDecorations) {
        this.equippedDecorations = equippedDecorations;
    }

    public List<String> getUnlockedSpaceships() { return unlockedSpaceships; }
    public void setUnlockedSpaceships(List<String> unlockedSpaceships) {
        this.unlockedSpaceships = unlockedSpaceships;
    }

    public int getFuelCells() { return fuelCells; }
    public void setFuelCells(int fuelCells) { this.fuelCells = Math.max(0, Math.min(maxFuelCells, fuelCells)); }

    public int getMaxFuelCells() { return maxFuelCells; }
    public void setMaxFuelCells(int maxFuelCells) { this.maxFuelCells = maxFuelCells; }

    public int getTotalTrips() { return totalTrips; }
    public void setTotalTrips(int totalTrips) { this.totalTrips = totalTrips; }

    public long getTotalDistanceTraveled() { return totalDistanceTraveled; }
    public void setTotalDistanceTraveled(long totalDistanceTraveled) {
        this.totalDistanceTraveled = totalDistanceTraveled;
    }

    public long getLastRefuelTime() { return lastRefuelTime; }
    public void setLastRefuelTime(long lastRefuelTime) { this.lastRefuelTime = lastRefuelTime; }

    // Helper methods
    public boolean useFuel(int amount) {
        if (fuelCells >= amount) {
            fuelCells -= amount;
            return true;
        }
        return false;
    }

    public void addFuel(int amount) {
        fuelCells = Math.min(maxFuelCells, fuelCells + amount);
        lastRefuelTime = System.currentTimeMillis();
    }

    public void recordTrip(long distance) {
        totalTrips++;
        totalDistanceTraveled += distance;
    }

    public void unlockSpaceship(String type) {
        if (!unlockedSpaceships.contains(type)) {
            unlockedSpaceships.add(type);
        }
    }

    public boolean isSpaceshipUnlocked(String type) {
        return unlockedSpaceships.contains(type);
    }

    public void equipDecoration(String decorationId) {
        if (!equippedDecorations.contains(decorationId)) {
            equippedDecorations.add(decorationId);
        }
    }

    public void unequipDecoration(String decorationId) {
        equippedDecorations.remove(decorationId);
    }

    public int getFuelPercentage() {
        return (int) ((fuelCells * 100.0f) / maxFuelCells);
    }

    public String getSpaceshipEmoji() {
        switch (spaceshipType) {
            case TYPE_STARTER: return "🚀";
            case TYPE_EXPLORER: return "🛸";
            case TYPE_CRUISER: return "🚀";
            case TYPE_WARP: return "⚡";
            case TYPE_GALAXY: return "🌟";
            default: return "🚀";
        }
    }

    // Calculate fuel regeneration (1 fuel per 5 minutes)
    public int calculateFuelRegeneration() {
        long now = System.currentTimeMillis();
        long timePassed = now - lastRefuelTime;
        int fuelToAdd = (int) (timePassed / (5 * 60 * 1000)); // 5 minutes per fuel
        return fuelToAdd;
    }

    public void applyFuelRegeneration() {
        int fuelToAdd = calculateFuelRegeneration();
        if (fuelToAdd > 0) {
            addFuel(fuelToAdd);
        }
    }
}

