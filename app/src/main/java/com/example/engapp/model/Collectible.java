package com.example.engapp.model;

import java.io.Serializable;

/**
 * Represents a collectible item in the game.
 * Includes stars, crystals, badges, fuel cells, and special items.
 */
public class Collectible implements Serializable {

    // Collectible categories
    public static final String CATEGORY_STAR = "star";
    public static final String CATEGORY_CRYSTAL = "crystal";
    public static final String CATEGORY_BADGE = "badge";
    public static final String CATEGORY_FUEL = "fuel";
    public static final String CATEGORY_ACCESSORY = "accessory";
    public static final String CATEGORY_DECORATION = "decoration";

    // Star types
    public static final String STAR_GOLDEN = "golden_star";
    public static final String STAR_RAINBOW = "rainbow_star";
    public static final String STAR_SHOOTING = "shooting_star";

    private String collectibleId;
    private String name;
    private String nameVi;
    private String emoji;
    private String category;
    private String source;
    private String sourcePlanetId;
    private long dateEarned;
    private boolean isSpecial;
    private int value;
    private String description;
    private String descriptionVi;

    public Collectible() {
        this.collectibleId = java.util.UUID.randomUUID().toString();
        this.dateEarned = System.currentTimeMillis();
        this.isSpecial = false;
        this.value = 1;
    }

    public Collectible(String name, String nameVi, String emoji, String category) {
        this();
        this.name = name;
        this.nameVi = nameVi;
        this.emoji = emoji;
        this.category = category;
    }

    // Factory methods for common collectibles
    public static Collectible createGoldenStar(String source) {
        Collectible star = new Collectible("Golden Star", "Sao Vàng", "⭐", CATEGORY_STAR);
        star.setSource(source);
        star.setValue(10);
        return star;
    }

    public static Collectible createRainbowStar(String source) {
        Collectible star = new Collectible("Rainbow Star", "Sao Cầu Vồng", "🌈", CATEGORY_STAR);
        star.setSource(source);
        star.setValue(50);
        star.setSpecial(true);
        return star;
    }

    public static Collectible createWordCrystal(String wordEnglish, String wordVietnamese, String planetId) {
        Collectible crystal = new Collectible(
            wordEnglish + " Crystal",
            "Pha lê " + wordVietnamese,
            "💎",
            CATEGORY_CRYSTAL
        );
        crystal.setSourcePlanetId(planetId);
        crystal.setSource("vocabulary");
        return crystal;
    }

    public static Collectible createBadge(String badgeName, String badgeNameVi, String emoji) {
        Collectible badge = new Collectible(badgeName, badgeNameVi, emoji, CATEGORY_BADGE);
        badge.setSpecial(true);
        return badge;
    }

    public static Collectible createFuelCell(int amount) {
        Collectible fuel = new Collectible("Fuel Cell", "Pin Năng Lượng", "🔋", CATEGORY_FUEL);
        fuel.setValue(amount);
        return fuel;
    }

    // Getters and Setters
    public String getCollectibleId() { return collectibleId; }
    public void setCollectibleId(String collectibleId) { this.collectibleId = collectibleId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameVi() { return nameVi; }
    public void setNameVi(String nameVi) { this.nameVi = nameVi; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getSourcePlanetId() { return sourcePlanetId; }
    public void setSourcePlanetId(String sourcePlanetId) { this.sourcePlanetId = sourcePlanetId; }

    public long getDateEarned() { return dateEarned; }
    public void setDateEarned(long dateEarned) { this.dateEarned = dateEarned; }

    public boolean isSpecial() { return isSpecial; }
    public void setSpecial(boolean special) { isSpecial = special; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDescriptionVi() { return descriptionVi; }
    public void setDescriptionVi(String descriptionVi) { this.descriptionVi = descriptionVi; }

    // Helper methods
    public String getFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy",
            java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(dateEarned));
    }

    public boolean isStar() {
        return CATEGORY_STAR.equals(category);
    }

    public boolean isCrystal() {
        return CATEGORY_CRYSTAL.equals(category);
    }

    public boolean isBadge() {
        return CATEGORY_BADGE.equals(category);
    }

    public boolean isFuel() {
        return CATEGORY_FUEL.equals(category);
    }
}

