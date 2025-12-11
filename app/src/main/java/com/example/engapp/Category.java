package com.example.engapp;

public class Category {
    private String name;
    private String icon;
    private int color;

    public Category(String name, String icon, int color) {
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public int getColor() {
        return color;
    }
}
