package com.example.engapp;

import java.util.List;

public class BehavioralQuestion {
    private String id;
    private String question;
    private String category;
    private String difficulty; // easy, medium, hard
    private String sample_basic;
    private String sample_intermediate;
    private String sample_advanced;
    private List<String> keywords; // Array từ Firebase
    private String explanation;
    private String practice_template;

    public BehavioralQuestion() {}

    public BehavioralQuestion(String id, String question, String category, String difficulty,
                             String sample_basic, String sample_intermediate, String sample_advanced,
                             List<String> keywords, String explanation, String practice_template) {
        this.id = id;
        this.question = question;
        this.category = category;
        this.difficulty = difficulty;
        this.sample_basic = sample_basic;
        this.sample_intermediate = sample_intermediate;
        this.sample_advanced = sample_advanced;
        this.keywords = keywords;
        this.explanation = explanation;
        this.practice_template = practice_template;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getSample_basic() { return sample_basic; }
    public void setSample_basic(String sample_basic) { this.sample_basic = sample_basic; }

    public String getSample_intermediate() { return sample_intermediate; }
    public void setSample_intermediate(String sample_intermediate) { this.sample_intermediate = sample_intermediate; }

    public String getSample_advanced() { return sample_advanced; }
    public void setSample_advanced(String sample_advanced) { this.sample_advanced = sample_advanced; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getPractice_template() { return practice_template; }
    public void setPractice_template(String practice_template) { this.practice_template = practice_template; }
    
    // Helper method để convert keywords array sang string
    public String getKeywordsAsString() {
        if (keywords == null || keywords.isEmpty()) return "";
        return String.join(", ", keywords);
    }
}
