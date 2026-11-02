package com.example.postgresql.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Playlist {

    private int id;
    private String title;

    @JsonProperty("cover_url")
    private String coverUrl;

    @JsonProperty("owner_id")
    private int ownerId;

    @JsonProperty("is_public")
    private boolean isPublic;

    @JsonProperty("is_featured")
    private boolean isFeatured;

    private int trackCount;

    public Playlist() {}

    public Playlist(int id, String title, String coverUrl, int ownerId, boolean isPublic, boolean isFeatured) {
        this.id = id;
        this.title = title;
        this.coverUrl = coverUrl;
        this.ownerId = ownerId;
        this.isPublic = isPublic;
        this.isFeatured = isFeatured;
    }

    @Override
    public String toString() {
        return title;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean aPublic) { isPublic = aPublic; }

    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }

    public int getTrackCount() { return trackCount; }
    public void setTrackCount(int trackCount) { this.trackCount = trackCount; }
}