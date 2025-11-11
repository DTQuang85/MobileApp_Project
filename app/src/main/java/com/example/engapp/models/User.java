package com.example.engapp.models;

public class User {
    private String uid;
    private String email;
    private String displayName;
    private int totalScore;
    private int lessonsCompleted;

    public User() {
    }

    public User(String uid, String email, String displayName) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.totalScore = 0;
        this.lessonsCompleted = 0;
    }

    public User(String uid, String email, String displayName, int totalScore, int lessonsCompleted) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.totalScore = totalScore;
        this.lessonsCompleted = lessonsCompleted;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getLessonsCompleted() {
        return lessonsCompleted;
    }

    public void setLessonsCompleted(int lessonsCompleted) {
        this.lessonsCompleted = lessonsCompleted;
    }
}

