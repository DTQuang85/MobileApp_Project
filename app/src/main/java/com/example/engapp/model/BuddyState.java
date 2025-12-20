package com.example.engapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the state and data of the user's Buddy companion.
 * Buddy accompanies the user throughout their learning journey.
 */
public class BuddyState implements Serializable {

    // Buddy States
    public static final String STATE_IDLE = "idle";
    public static final String STATE_HAPPY = "happy";
    public static final String STATE_ENCOURAGING = "encouraging";
    public static final String STATE_TRAVELING = "traveling";
    public static final String STATE_THINKING = "thinking";
    public static final String STATE_SLEEPING = "sleeping";
    public static final String STATE_CELEBRATING = "celebrating";
    public static final String STATE_SAD = "sad";

    private String odId;
    private String currentBuddyId;
    private List<String> unlockedBuddies;
    private int buddyMood; // 0-100
    private int buddyLevel;
    private String currentState;
    private List<String> equippedAccessories;
    private long lastInteraction;
    private int totalInteractions;
    private int daysWithBuddy;

    // Buddy personalities
    public static final String BUDDY_ROBOT = "robot";
    public static final String BUDDY_ALIEN = "alien";
    public static final String BUDDY_CAT = "cat";
    public static final String BUDDY_FOX = "fox";
    public static final String BUDDY_DRAGON = "dragon";
    public static final String BUDDY_UNICORN = "unicorn";
    public static final String BUDDY_PANDA = "panda";
    public static final String BUDDY_LION = "lion";

    public BuddyState() {
        this.currentBuddyId = BUDDY_ROBOT;
        this.unlockedBuddies = new ArrayList<>();
        this.unlockedBuddies.add(BUDDY_ROBOT);
        this.unlockedBuddies.add(BUDDY_ALIEN);
        this.unlockedBuddies.add(BUDDY_CAT);
        this.unlockedBuddies.add(BUDDY_FOX);
        this.buddyMood = 100;
        this.buddyLevel = 1;
        this.currentState = STATE_IDLE;
        this.equippedAccessories = new ArrayList<>();
        this.lastInteraction = System.currentTimeMillis();
        this.totalInteractions = 0;
        this.daysWithBuddy = 0;
    }

    // Getters and Setters
    public String getodId() { return odId; }
    public void setodId(String odId) { this.odId = odId; }

    public String getCurrentBuddyId() { return currentBuddyId; }
    public void setCurrentBuddyId(String currentBuddyId) { this.currentBuddyId = currentBuddyId; }

    public List<String> getUnlockedBuddies() { return unlockedBuddies; }
    public void setUnlockedBuddies(List<String> unlockedBuddies) { this.unlockedBuddies = unlockedBuddies; }

    public int getBuddyMood() { return buddyMood; }
    public void setBuddyMood(int buddyMood) {
        this.buddyMood = Math.max(0, Math.min(100, buddyMood));
    }

    public int getBuddyLevel() { return buddyLevel; }
    public void setBuddyLevel(int buddyLevel) { this.buddyLevel = buddyLevel; }

    public String getCurrentState() { return currentState; }
    public void setCurrentState(String currentState) { this.currentState = currentState; }

    public List<String> getEquippedAccessories() { return equippedAccessories; }
    public void setEquippedAccessories(List<String> equippedAccessories) {
        this.equippedAccessories = equippedAccessories;
    }

    public long getLastInteraction() { return lastInteraction; }
    public void setLastInteraction(long lastInteraction) { this.lastInteraction = lastInteraction; }

    public int getTotalInteractions() { return totalInteractions; }
    public void setTotalInteractions(int totalInteractions) { this.totalInteractions = totalInteractions; }

    public int getDaysWithBuddy() { return daysWithBuddy; }
    public void setDaysWithBuddy(int daysWithBuddy) { this.daysWithBuddy = daysWithBuddy; }

    // Helper methods
    public void unlockBuddy(String buddyId) {
        if (!unlockedBuddies.contains(buddyId)) {
            unlockedBuddies.add(buddyId);
        }
    }

    public boolean isBuddyUnlocked(String buddyId) {
        return unlockedBuddies.contains(buddyId);
    }

    public void equipAccessory(String accessoryId) {
        if (!equippedAccessories.contains(accessoryId)) {
            equippedAccessories.add(accessoryId);
        }
    }

    public void unequipAccessory(String accessoryId) {
        equippedAccessories.remove(accessoryId);
    }

    public void increaseMood(int amount) {
        this.buddyMood = Math.min(100, this.buddyMood + amount);
    }

    public void decreaseMood(int amount) {
        this.buddyMood = Math.max(0, this.buddyMood - amount);
    }

    public void recordInteraction() {
        this.lastInteraction = System.currentTimeMillis();
        this.totalInteractions++;
    }

    public String getBuddyEmoji() {
        switch (currentBuddyId) {
            case BUDDY_ROBOT: return "🤖";
            case BUDDY_ALIEN: return "👽";
            case BUDDY_CAT: return "🐱";
            case BUDDY_FOX: return "🦊";
            case BUDDY_DRAGON: return "🐲";
            case BUDDY_UNICORN: return "🦄";
            case BUDDY_PANDA: return "🐼";
            case BUDDY_LION: return "🦁";
            default: return "🤖";
        }
    }

    public String getBuddyName() {
        switch (currentBuddyId) {
            case BUDDY_ROBOT: return "Robo-Buddy";
            case BUDDY_ALIEN: return "Alien-Friend";
            case BUDDY_CAT: return "Kitty-Pal";
            case BUDDY_FOX: return "Foxy-Guide";
            case BUDDY_DRAGON: return "Dragon";
            case BUDDY_UNICORN: return "Unicorn";
            case BUDDY_PANDA: return "Panda";
            case BUDDY_LION: return "Lion";
            default: return "Buddy";
        }
    }
}

