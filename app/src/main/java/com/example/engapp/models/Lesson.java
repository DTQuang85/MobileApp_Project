package com.example.engapp.models;

import java.util.List;

public class Lesson {
    private String id;
    private String title;
    private String description;
    private String difficulty; // beginner, intermediate, advanced
    private List<Question> questions;
    private int totalPoints;

    public Lesson() {
    }

    public Lesson(String id, String title, String description, List<Question> questions) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.questions = questions;
        this.difficulty = "beginner";
        this.totalPoints = questions != null ? questions.size() * 10 : 0;
    }

    public Lesson(String id, String title, String description, String difficulty, List<Question> questions, int totalPoints) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.questions = questions;
        this.totalPoints = totalPoints;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
}

