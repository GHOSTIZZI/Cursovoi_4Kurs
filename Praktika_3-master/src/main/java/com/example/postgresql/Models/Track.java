package com.example.postgresql.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Track {

    private int id;
    private String title;
    private String artist;
    private String coverUrl;
    private String album;
    private String genre;
    private String trackUrl;
    private String audioUrl;
    private String authorName;
    private String lyrics;
    private List<Author> authors = new ArrayList<>(); // Список всех авторов трека
    private boolean isRadio = false;

    public boolean isRadio() { return isRadio; }
    public void setRadio(boolean radio) { isRadio = radio; }

    public Track() {}


    @JsonProperty("tracks_authors")
    public void setTracksAuthors(JsonNode tracksAuthorsNode) {
        authors.clear();
        if (tracksAuthorsNode != null && tracksAuthorsNode.isArray()) {
            for (JsonNode linkNode : tracksAuthorsNode) {
                if (linkNode.has("author")) {
                    JsonNode authorNode = linkNode.get("author");
                    if (authorNode != null) {
                        Author author = new Author();
                        if (authorNode.has("id")) author.setId(authorNode.get("id").asInt());
                        if (authorNode.has("name")) author.setName(authorNode.get("name").asText());
                        if (authorNode.has("photo_url")) author.setPhotoUrl(authorNode.get("photo_url").asText());
                        authors.add(author);
                    }
                }
            }
        }


        if (!authors.isEmpty()) {
            this.artist = authors.stream().map(Author::getName).collect(Collectors.joining(", "));
        } else {
            this.artist = "Неизвестный автор";
        }
    }

    public List<Author> getAuthors() { return authors; }
    public void setAuthors(List<Author> authors) { this.authors = authors; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbum() { return album; }
    @JsonProperty("album")
    public void setAlbum(String album) { this.album = album; }

    public String getGenre() { return genre; }
    @JsonProperty("genre")
    public void setGenre(String genre) { this.genre = genre; }

    public String getLyrics() { return lyrics; }
    @JsonProperty("lyrics")
    public void setLyrics(String lyrics) { this.lyrics = lyrics; }

    @JsonProperty("cover_url")
    public String getCoverUrl() { return coverUrl; }
    @JsonProperty("cover_url")
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    @JsonProperty("audio_url")
    public String getTrackUrl() { return trackUrl; }
    @JsonProperty("audio_url")
    public void setTrackUrl(String trackUrl) {
        this.trackUrl = trackUrl;
        this.audioUrl = trackUrl;
    }

    @JsonProperty("release_year")
    private int releaseYear;


    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

    @Override
    public String toString() {
        return id + " | " + title + " - " + artist;
    }
}