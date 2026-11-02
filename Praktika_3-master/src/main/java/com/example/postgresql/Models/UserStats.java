package com.example.postgresql.Models;

import java.util.ArrayList;
import java.util.List;

public class UserStats {
    private int totalPlays;
    private List<String> topTracks = new ArrayList<>();
    private String topArtist;
    private String favoriteGenre;

    public int getTotalPlays() {
        return totalPlays;
    }

    public void setTotalPlays(int totalPlays) {
        this.totalPlays = totalPlays;
    }

    public List<String> getTopTracks() {
        return topTracks;
    }

    public void setTopTracks(List<String> topTracks) {
        this.topTracks = topTracks;
    }

    public String getTopArtist() {
        return topArtist;
    }

    public void setTopArtist(String topArtist) {
        this.topArtist = topArtist;
    }

    public String getFavoriteGenre() {
        return favoriteGenre;
    }

    public void setFavoriteGenre(String favoriteGenre) {
        this.favoriteGenre = favoriteGenre;
    }
}

