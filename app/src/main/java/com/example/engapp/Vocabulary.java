package com.example.engapp;

public class Vocabulary {
    private int id;
    private String category;
    private String term;
    private String type;
    private String pronunciation;
    private String definition;
    private String example;
    private String image;

    public Vocabulary() {
        // Constructor mặc định cho Firestore
    }

    public Vocabulary(int id, String category, String term, String type, String pronunciation, 
                     String definition, String example, String image) {
        this.id = id;
        this.category = category;
        this.term = term;
        this.type = type;
        this.pronunciation = pronunciation;
        this.definition = definition;
        this.example = example;
        this.image = image;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getTerm() {
        return term;
    }

    public String getType() {
        return type;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public String getDefinition() {
        return definition;
    }

    public String getExample() {
        return example;
    }

    public String getImage() {
        return image;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
