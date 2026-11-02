package com.example.postgresql.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class History {

    private long id;

    @JsonProperty("user_id")
    private long userId;

    @JsonProperty("track_id")
    private long trackId;

    @JsonProperty("listened_at")
    private String listenedAt;

    public History() {
    }

    public History(long id, long userId, long trackId, String listenedAt) {
        this.id = id;
        this.userId = userId;
        this.trackId = trackId;
        this.listenedAt = listenedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getTrackId() {
        return trackId;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }

    public String getListenedAt() {
        return listenedAt;
    }

    public void setListenedAt(String listenedAt) {
        this.listenedAt = listenedAt;
    }

    @Override
    public String toString() {
        return "History{" +
                "id=" + id +
                ", userId=" + userId +
                ", trackId=" + trackId +
                ", listenedAt='" + listenedAt + '\'' +
                '}';
    }
}