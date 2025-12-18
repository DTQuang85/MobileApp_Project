package com.example.engapp.model;

import java.io.Serializable;

/**
 * Represents an example sentence for learning.
 */
public class Sentence implements Serializable {
    private String id;
    private String english;
    private String vietnamese;
    private String audioUrl;
    private String[] keywords; // Important words highlighted
    private boolean isLearned;

    public Sentence() {}

    public Sentence(String english, String vietnamese) {
        this.english = english;
        this.vietnamese = vietnamese;
        this.isLearned = false;
    }

    public Sentence(String english, String vietnamese, String[] keywords) {
        this(english, vietnamese);
        this.keywords = keywords;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEnglish() { return english; }
    public void setEnglish(String english) { this.english = english; }

    public String getVietnamese() { return vietnamese; }
    public void setVietnamese(String vietnamese) { this.vietnamese = vietnamese; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public String[] getKeywords() { return keywords; }
    public void setKeywords(String[] keywords) { this.keywords = keywords; }

    public boolean isLearned() { return isLearned; }
    public void setLearned(boolean learned) { isLearned = learned; }
}

