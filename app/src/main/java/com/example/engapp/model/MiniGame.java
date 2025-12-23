package com.example.engapp.model;

import java.io.Serializable;

/**
 * Represents a mini-game within a zone.
 */
public class MiniGame implements Serializable {
    public static final String TYPE_GUESS_NAME = "guess_name";
    public static final String TYPE_LISTEN_CHOOSE = "listen_choose";
    public static final String TYPE_SPEAK = "speak";
    public static final String TYPE_MATCH = "match";
    public static final String TYPE_FILL_BLANK = "fill_blank";

    private String id;
    private String type; // guess_name, listen_choose, speak, match, fill_blank
    private String title;
    private String titleVi;
    private String description;
    private int maxScore;
    private int earnedScore;
    private boolean isCompleted;
    private int starsEarned; // 0-3

    public MiniGame() {}

    public MiniGame(String type, String title, String titleVi) {
        this.type = type;
        this.title = title;
        this.titleVi = titleVi;
        this.isCompleted = false;
        this.starsEarned = 0;
        this.maxScore = 100;
        this.earnedScore = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTitleVi() { return titleVi; }
    public void setTitleVi(String titleVi) { this.titleVi = titleVi; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getMaxScore() { return maxScore; }
    public void setMaxScore(int maxScore) { this.maxScore = maxScore; }

    public int getEarnedScore() { return earnedScore; }
    public void setEarnedScore(int earnedScore) { this.earnedScore = earnedScore; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getStarsEarned() { return starsEarned; }
    public void setStarsEarned(int starsEarned) { this.starsEarned = starsEarned; }

    public String getGameIcon() {
        switch (type) {
            case TYPE_GUESS_NAME: return "🔍";
            case TYPE_LISTEN_CHOOSE: return "👂";
            case TYPE_SPEAK: return "🎤";
            case TYPE_MATCH: return "🧩";
            case TYPE_FILL_BLANK: return "✏️";
            default: return "🎮";
        }
    }
}

