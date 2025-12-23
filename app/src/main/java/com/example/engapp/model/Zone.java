package com.example.engapp.model;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a zone within a planet.
 * Each zone contains vocabulary words, sentences, and mini-games.
 */
public class Zone implements Serializable {
    private String id;
    private String name;
    private String nameVi;
    private String description;
    private String emoji;
    private List<Word> words;
    private List<Sentence> sentences;
    private List<MiniGame> miniGames;
    private boolean isCompleted;
    private int starsEarned; // 0-3 stars
    private boolean isUnlocked;

    public Zone() {}

    public Zone(String id, String name, String nameVi, String emoji) {
        this.id = id;
        this.name = name;
        this.nameVi = nameVi;
        this.emoji = emoji;
        this.isCompleted = false;
        this.starsEarned = 0;
        this.isUnlocked = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameVi() { return nameVi; }
    public void setNameVi(String nameVi) { this.nameVi = nameVi; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public List<Word> getWords() { return words; }
    public void setWords(List<Word> words) { this.words = words; }

    public List<Sentence> getSentences() { return sentences; }
    public void setSentences(List<Sentence> sentences) { this.sentences = sentences; }

    public List<MiniGame> getMiniGames() { return miniGames; }
    public void setMiniGames(List<MiniGame> miniGames) { this.miniGames = miniGames; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getStarsEarned() { return starsEarned; }
    public void setStarsEarned(int starsEarned) { this.starsEarned = starsEarned; }

    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }

    public int getTotalWords() {
        return words != null ? words.size() : 0;
    }
}

