package com.example.engapp.model;

import java.io.Serializable;

/**
 * Represents a travel log entry for the Captain's Log feature.
 * Records each journey between planets with learning statistics.
 */
public class TravelLog implements Serializable {

    private String logId;
    private String odId;
    private String fromPlanetId;
    private String fromPlanetName;
    private String toPlanetId;
    private String toPlanetName;
    private long travelDate;
    private int starsEarnedDuringVisit;
    private int wordsLearnedDuringVisit;
    private int gamesCompletedDuringVisit;
    private long timeSpentOnPlanet; // in milliseconds
    private String buddyComment;
    private String planetEmoji;

    public TravelLog() {
        this.logId = java.util.UUID.randomUUID().toString();
        this.travelDate = System.currentTimeMillis();
        this.starsEarnedDuringVisit = 0;
        this.wordsLearnedDuringVisit = 0;
        this.gamesCompletedDuringVisit = 0;
        this.timeSpentOnPlanet = 0;
    }

    public TravelLog(String fromPlanetId, String fromPlanetName,
                     String toPlanetId, String toPlanetName) {
        this();
        this.fromPlanetId = fromPlanetId;
        this.fromPlanetName = fromPlanetName;
        this.toPlanetId = toPlanetId;
        this.toPlanetName = toPlanetName;
    }

    // Getters and Setters
    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getodId() { return odId; }
    public void setodId(String odId) { this.odId = odId; }

    public String getFromPlanetId() { return fromPlanetId; }
    public void setFromPlanetId(String fromPlanetId) { this.fromPlanetId = fromPlanetId; }

    public String getFromPlanetName() { return fromPlanetName; }
    public void setFromPlanetName(String fromPlanetName) { this.fromPlanetName = fromPlanetName; }

    public String getToPlanetId() { return toPlanetId; }
    public void setToPlanetId(String toPlanetId) { this.toPlanetId = toPlanetId; }

    public String getToPlanetName() { return toPlanetName; }
    public void setToPlanetName(String toPlanetName) { this.toPlanetName = toPlanetName; }

    public long getTravelDate() { return travelDate; }
    public void setTravelDate(long travelDate) { this.travelDate = travelDate; }

    public int getStarsEarnedDuringVisit() { return starsEarnedDuringVisit; }
    public void setStarsEarnedDuringVisit(int starsEarnedDuringVisit) {
        this.starsEarnedDuringVisit = starsEarnedDuringVisit;
    }

    public int getWordsLearnedDuringVisit() { return wordsLearnedDuringVisit; }
    public void setWordsLearnedDuringVisit(int wordsLearnedDuringVisit) {
        this.wordsLearnedDuringVisit = wordsLearnedDuringVisit;
    }

    public int getGamesCompletedDuringVisit() { return gamesCompletedDuringVisit; }
    public void setGamesCompletedDuringVisit(int gamesCompletedDuringVisit) {
        this.gamesCompletedDuringVisit = gamesCompletedDuringVisit;
    }

    public long getTimeSpentOnPlanet() { return timeSpentOnPlanet; }
    public void setTimeSpentOnPlanet(long timeSpentOnPlanet) { this.timeSpentOnPlanet = timeSpentOnPlanet; }

    public String getBuddyComment() { return buddyComment; }
    public void setBuddyComment(String buddyComment) { this.buddyComment = buddyComment; }

    public String getPlanetEmoji() { return planetEmoji; }
    public void setPlanetEmoji(String planetEmoji) { this.planetEmoji = planetEmoji; }

    // Helper methods
    public String getFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm",
            java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(travelDate));
    }

    public String getFormattedTimeSpent() {
        long minutes = timeSpentOnPlanet / (60 * 1000);
        if (minutes < 60) {
            return minutes + " phút";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return hours + " giờ " + remainingMinutes + " phút";
        }
    }

    public String generateAutoComment() {
        StringBuilder comment = new StringBuilder();

        if (wordsLearnedDuringVisit > 0) {
            comment.append("Học được ").append(wordsLearnedDuringVisit).append(" từ mới! ");
        }

        if (starsEarnedDuringVisit > 0) {
            comment.append("Thu thập ").append(starsEarnedDuringVisit).append(" ⭐! ");
        }

        if (gamesCompletedDuringVisit > 0) {
            comment.append("Hoàn thành ").append(gamesCompletedDuringVisit).append(" trò chơi!");
        }

        if (comment.length() == 0) {
            comment.append("Một chuyến thám hiểm thú vị!");
        }

        return comment.toString().trim();
    }
}

