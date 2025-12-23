package com.example.engapp.model;

import java.io.Serializable;

/**
 * Represents a node within a planet.
 * Each planet has multiple nodes: Learn, Quiz, Dialogue, Puzzle, Battle
 * Phase 3 of TODO: Planet Map
 */
public class PlanetNode implements Serializable {
    private int id;
    private int planetId;
    private String nodeKey;
    private String nodeType;  // learn, quiz, dialogue, puzzle, battle
    private String name;
    private String nameVi;
    private String description;
    private String emoji;
    private int orderIndex;
    private boolean isUnlocked;
    private boolean isCompleted;
    private int starsEarned;
    private int maxStars;

    // Position for grid/path display
    private int gridRow;
    private int gridCol;

    // Reward info
    private int rewardStars;
    private int rewardFuel;
    private int rewardCrystals;

    public PlanetNode() {
        this.maxStars = 3;
        this.isUnlocked = false;
        this.isCompleted = false;
    }

    public PlanetNode(int id, int planetId, String nodeKey, String nodeType,
                      String name, String nameVi, String emoji, int orderIndex) {
        this.id = id;
        this.planetId = planetId;
        this.nodeKey = nodeKey;
        this.nodeType = nodeType;
        this.name = name;
        this.nameVi = nameVi;
        this.emoji = emoji;
        this.orderIndex = orderIndex;
        this.maxStars = 3;
        this.isUnlocked = orderIndex == 1; // First node is always unlocked
        this.isCompleted = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPlanetId() { return planetId; }
    public void setPlanetId(int planetId) { this.planetId = planetId; }

    public String getNodeKey() { return nodeKey; }
    public void setNodeKey(String nodeKey) { this.nodeKey = nodeKey; }

    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameVi() { return nameVi; }
    public void setNameVi(String nameVi) { this.nameVi = nameVi; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getStarsEarned() { return starsEarned; }
    public void setStarsEarned(int starsEarned) { this.starsEarned = starsEarned; }

    public int getMaxStars() { return maxStars; }
    public void setMaxStars(int maxStars) { this.maxStars = maxStars; }

    public int getGridRow() { return gridRow; }
    public void setGridRow(int gridRow) { this.gridRow = gridRow; }

    public int getGridCol() { return gridCol; }
    public void setGridCol(int gridCol) { this.gridCol = gridCol; }

    public int getRewardStars() { return rewardStars; }
    public void setRewardStars(int rewardStars) { this.rewardStars = rewardStars; }

    public int getRewardFuel() { return rewardFuel; }
    public void setRewardFuel(int rewardFuel) { this.rewardFuel = rewardFuel; }

    public int getRewardCrystals() { return rewardCrystals; }
    public void setRewardCrystals(int rewardCrystals) { this.rewardCrystals = rewardCrystals; }

    /**
     * Get the Activity class to launch for this node type
     */
    public String getActivityClassName() {
        switch (nodeType) {
            case "learn":
                return "LearnWordsActivity";
            case "quiz":
                return "ExploreActivity";
            case "dialogue":
                return "DialogueActivity";
            case "puzzle":
                return "PuzzleGameActivity";
            case "battle":
                return "WordBattleActivity";
            default:
                return "LearnWordsActivity";
        }
    }

    /**
     * Get display color based on node type
     */
    public String getTypeColor() {
        switch (nodeType) {
            case "learn":
                return "#4ECDC4";  // Teal
            case "quiz":
                return "#FF6B6B";  // Red
            case "dialogue":
                return "#45B7D1";  // Blue
            case "puzzle":
                return "#96CEB4";  // Green
            case "battle":
                return "#A29BFE";  // Purple
            default:
                return "#FFFFFF";
        }
    }

    /**
     * Get progress percentage
     */
    public int getProgressPercent() {
        if (maxStars == 0) return 0;
        return (starsEarned * 100) / maxStars;
    }
}
