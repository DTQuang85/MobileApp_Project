package com.example.engapp;

public class VideoInterview {
    private int id;
    private String category;
    private String title;
    private String thumbnail;
    private String videoId;
    private String platform;
    private String streamUrl;

    public VideoInterview() {
        // Constructor mặc định cho Firestore
    }

    public VideoInterview(int id, String category, String title, String thumbnail, String videoId, String platform, String streamUrl) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.thumbnail = thumbnail;
        this.videoId = videoId;
        this.platform = platform;
        this.streamUrl = streamUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }
}
