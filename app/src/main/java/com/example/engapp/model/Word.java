package com.example.engapp.model;

import java.io.Serializable;

/**
 * Represents a vocabulary word in the learning game.
 */
public class Word implements Serializable {
    private String id;
    private String english;
    private String vietnamese;
    private String pronunciation; // IPA pronunciation
    private String imageUrl;
    private String audioUrl;
    private String exampleSentence;
    private String exampleTranslation;
    private boolean isLearned;
    private int timesCorrect;
    private int timesWrong;

    public Word() {}

    public Word(String english, String vietnamese) {
        this.english = english;
        this.vietnamese = vietnamese;
        this.isLearned = false;
        this.timesCorrect = 0;
        this.timesWrong = 0;
    }

    public Word(String english, String vietnamese, String imageUrl) {
        this(english, vietnamese);
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEnglish() { return english; }
    public void setEnglish(String english) { this.english = english; }

    public String getVietnamese() { return vietnamese; }
    public void setVietnamese(String vietnamese) { this.vietnamese = vietnamese; }

    public String getPronunciation() { return pronunciation; }
    public void setPronunciation(String pronunciation) { this.pronunciation = pronunciation; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public String getExampleSentence() { return exampleSentence; }
    public void setExampleSentence(String exampleSentence) { this.exampleSentence = exampleSentence; }

    public String getExampleTranslation() { return exampleTranslation; }
    public void setExampleTranslation(String exampleTranslation) { this.exampleTranslation = exampleTranslation; }

    public boolean isLearned() { return isLearned; }
    public void setLearned(boolean learned) { isLearned = learned; }

    public int getTimesCorrect() { return timesCorrect; }
    public void setTimesCorrect(int timesCorrect) { this.timesCorrect = timesCorrect; }

    public int getTimesWrong() { return timesWrong; }
    public void setTimesWrong(int timesWrong) { this.timesWrong = timesWrong; }

    public void incrementCorrect() { this.timesCorrect++; }
    public void incrementWrong() { this.timesWrong++; }
}

