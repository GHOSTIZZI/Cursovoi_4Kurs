package com.example.postgresql.DAO;

import com.example.postgresql.Models.Playlist;
import com.example.postgresql.Models.PlaylistTrackWrapper;
import com.example.postgresql.Models.Track;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.CompletableFuture;

public class PlaylistDAO extends SupabaseDAO {

    private static final String PLAYLISTS = "/playlists";
    private static final String PLAYLIST_TRACKS = "/playlist_tracks";


    public CompletableFuture<List<Playlist>> getUserPlaylists(int userId) {

        String query = PLAYLISTS + "?owner_id=eq." + userId + "&is_featured=eq.false" + "&order=title.asc";

        HttpRequest request = createBaseRequestBuilder(query).GET().build();

        return sendAndDeserializeList(
                request,
                new TypeReference<List<Playlist>>() {
                },
                "получении плейлистов пользователя"
        );
    }


    public CompletableFuture<List<Playlist>> getFeaturedPlaylists() {
        String query = PLAYLISTS + "?is_featured=eq.true&order=title.asc";

        HttpRequest request = createBaseRequestBuilder(query).GET().build();

        return sendAndDeserializeList(
                request,
                new TypeReference<List<Playlist>>() {
                },
                "получении featured плейлистов"
        );
    }

    public CompletableFuture<Boolean> createPlaylist(String title, String coverUrl, int ownerId, boolean isFeatured) {

        if (title == null || title.trim().isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        ObjectNode json = objectMapper.createObjectNode()
                .put("title", title)
                .put("cover_url", coverUrl != null ? coverUrl : "")
                .put("owner_id", ownerId)
                .put("is_featured", isFeatured);

        return sendPostRequest(PLAYLISTS, json, "создании плейлиста");
    }

    public CompletableFuture<Boolean> deletePlaylist(int playlistId) {
        return sendDeleteRequest(
                PLAYLISTS + "?id=eq." + playlistId,
                "удалении плейлиста"
        );
    }


    public CompletableFuture<Boolean> addTrackToPlaylist(int playlistId, int trackId) {

        ObjectNode json = objectMapper.createObjectNode()
                .put("playlist_id", playlistId)
                .put("track_id", trackId);

        return sendPostRequest(
                PLAYLIST_TRACKS,
                json,
                "добавлении трека в плейлист"
        ).thenCompose(success -> {
            if (!success) return CompletableFuture.completedFuture(false);
            return updatePlaylistCoverAfterAdd(playlistId);
        });
    }

    private CompletableFuture<Boolean> updatePlaylistCoverAfterAdd(int playlistId) {

        String playlistQuery = PLAYLISTS + "?id=eq." + playlistId;
        HttpRequest playlistRequest = createBaseRequestBuilder(playlistQuery).GET().build();

        return sendAndDeserializeList(
                playlistRequest,
                new TypeReference<List<Playlist>>() {
                },
                "проверки типа плейлиста"
        ).thenCompose(playlists -> {
            if (playlists != null && !playlists.isEmpty()) {
                Playlist playlist = playlists.get(0);

                if (playlist.isFeatured()) {
                    return CompletableFuture.completedFuture(true);
                }
            }

            return getTracksByPlaylistId(playlistId).thenCompose(tracks -> {
                if (tracks != null && !tracks.isEmpty()) {
                    String firstTrackCover = tracks.get(0).getCoverUrl();

                    if (firstTrackCover != null && !firstTrackCover.trim().isEmpty()) {
                        String query = PLAYLISTS + "?id=eq." + playlistId;
                        ObjectNode json = objectMapper.createObjectNode().put("cover_url", firstTrackCover);

                        HttpRequest request = createBaseRequestBuilder(query)
                                .header("Content-Type", "application/json")
                                .header("Prefer", "return=minimal")
                                .method("PATCH", HttpRequest.BodyPublishers.ofString(json.toString()))
                                .build();

                        return java.net.http.HttpClient.newHttpClient()
                                .sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                                .thenApply(res -> res.statusCode() >= 200 && res.statusCode() < 300)
                                .exceptionally(ex -> false);
                    }
                }
                return CompletableFuture.completedFuture(true);
            });
        });
    }


    public CompletableFuture<Boolean> removeTrackFromPlaylist(int playlistId, int trackId) {
        return sendDeleteRequest(
                PLAYLIST_TRACKS + "?playlist_id=eq." + playlistId + "&track_id=eq." + trackId,
                "удалении трека из плейлиста"
        ).thenCompose(success -> {
            if (!success) return CompletableFuture.completedFuture(false);
            return checkAndResetCoverIfEmpty(playlistId);
        });
    }

    private CompletableFuture<Boolean> checkAndResetCoverIfEmpty(int playlistId) {
        return getTracksByPlaylistId(playlistId).thenCompose(tracks -> {
            if (tracks == null || tracks.isEmpty()) {

                String query = PLAYLISTS + "?id=eq." + playlistId;
                ObjectNode json = objectMapper.createObjectNode().putNull("cover_url");

                HttpRequest request = createBaseRequestBuilder(query)
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(json.toString()))
                        .build();

                return java.net.http.HttpClient.newHttpClient()
                        .sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                        .thenApply(res -> true)
                        .exceptionally(ex -> true);
            }
            return CompletableFuture.completedFuture(true);
        });
    }


    public CompletableFuture<Boolean> updatePlaylist(int playlistId, String title, String coverUrl) {
        if (title == null || title.trim().isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        ObjectNode json = objectMapper.createObjectNode()
                .put("title", title.trim())
                .put("cover_url", coverUrl != null ? coverUrl.trim() : "");

        String pathQuery = PLAYLISTS + "?id=eq." + playlistId;

        HttpRequest request = createBaseRequestBuilder(pathQuery)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        return java.net.http.HttpClient.newHttpClient()
                .sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() >= 200 && response.statusCode() < 300)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return false;
                });
    }

    public CompletableFuture<List<Track>> getTracksByPlaylistId(int playlistId) {
        String tracksSelect = "*,tracks_authors(author:authors(id,name,photo_url))";
        String query = PLAYLIST_TRACKS + "?select=tracks(" + tracksSelect + ")&playlist_id=eq." + playlistId + "&order=added_at.asc";

        HttpRequest request = createBaseRequestBuilder(query).GET().build();

        return sendAndDeserializeList(
                request,
                new TypeReference<List<PlaylistTrackWrapper>>() {
                },
                "получении треков плейлиста"
        ).thenApply(wrappers -> {
            List<Track> tracks = new ArrayList<>();
            if (wrappers != null) {
                for (PlaylistTrackWrapper wrapper : wrappers) {
                    if (wrapper != null && wrapper.getTracks() != null) {
                        tracks.add(wrapper.getTracks());
                    }
                }
            }
            return tracks;
        });
    }

}