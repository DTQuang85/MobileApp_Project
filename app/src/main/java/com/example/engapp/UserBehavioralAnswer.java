package com.example.engapp;

public class UserBehavioralAnswer {
    private String userId;
    private String questionId;
    private String answer_text;
    private int score;
    private int keywordScore;
    private boolean grammarGood;
    private boolean structureGood;
    private long timestamp;

    public UserBehavioralAnswer() {}

    public UserBehavioralAnswer(String userId, String questionId, String answer_text,
                               int score, int keywordScore, boolean grammarGood,
                               boolean structureGood, long timestamp) {
        this.userId = userId;
        this.questionId = questionId;
        this.answer_text = answer_text;
        this.score = score;
        this.keywordScore = keywordScore;
        this.grammarGood = grammarGood;
        this.structureGood = structureGood;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getAnswer_text() { return answer_text; }
    public void setAnswer_text(String answer_text) { this.answer_text = answer_text; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getKeywordScore() { return keywordScore; }
    public void setKeywordScore(int keywordScore) { this.keywordScore = keywordScore; }

    public boolean isGrammarGood() { return grammarGood; }
    public void setGrammarGood(boolean grammarGood) { this.grammarGood = grammarGood; }

    public boolean isStructureGood() { return structureGood; }
    public void setStructureGood(boolean structureGood) { this.structureGood = structureGood; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
