package com.example.postgresql.DAO;

import com.example.postgresql.Models.Track;
import com.example.postgresql.Models.UserStats;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.http.HttpRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TrackDAO extends SupabaseDAO {

    private static final String TRACKS_TABLE = "/tracks";
    private static final String HISTORY_TABLE = "/history";


    private static final String TRACKS_SELECT = "*,tracks_authors(author:authors(id,name,photo_url))";

    public CompletableFuture<List<Track>> getAllTracks(String orderBy) {
        String cleanedOrderBy = (orderBy != null && !orderBy.trim().isEmpty()) ? orderBy : "title.asc";

        String query = TRACKS_TABLE + "?select=" + TRACKS_SELECT + "&order=" + cleanedOrderBy;
        HttpRequest request = createBaseRequestBuilder(query).GET().build();
        return sendAndDeserializeList(request, new TypeReference<List<Track>>() {}, "получении списка всех треков");
    }

    public CompletableFuture<List<Track>> getAllTracks() {
        return getAllTracks("title.asc");
    }

    public CompletableFuture<List<Track>> getTracksByAuthorId(int authorId) {

        String query = TRACKS_TABLE + "?select=*,tracks_authors!inner(author_id,author:authors(id,name,photo_url))&tracks_authors.author_id=eq." + authorId + "&order=title.asc";

        HttpRequest request = createBaseRequestBuilder(query).GET().build();
        return sendAndDeserializeList(request, new TypeReference<List<Track>>() {}, "получении треков по ID автора");
    }

    public CompletableFuture<List<Track>> searchTracks(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        String trimmed = searchQuery.trim();
        String encoded = encodeUrlParameter(trimmed);
        String wildcard = "%25" + encoded + "%25";

        String queryByTitle = TRACKS_TABLE + "?select=" + TRACKS_SELECT + "&title=ilike.*" + wildcard + "&order=title.asc";
        HttpRequest requestByTitle = createBaseRequestBuilder(queryByTitle).GET().build();

        return sendAndDeserializeList(
                requestByTitle,
                new TypeReference<List<Track>>() {},
                "поиск треков по названию"
        );
    }

    public CompletableFuture<List<Track>> getFavoriteTracksByUser(int userId, String orderBy) {
        String query = TRACKS_TABLE + "?select=" + TRACKS_SELECT + ",favorites!inner()&favorites.user_id=eq." + userId;

        HttpRequest request = createBaseRequestBuilder(query).GET().build();
        return sendAndDeserializeList(request, new TypeReference<List<Track>>() {}, "получении избранных треков");
    }

    public CompletableFuture<Boolean> addTrack(Track track) {
        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put("title", track.getTitle())
                .put("album", track.getAlbum())
                .put("genre", track.getGenre())
                .put("cover_url", track.getCoverUrl())
                .put("audio_url", track.getTrackUrl())
                .put("lyrics", track.getLyrics())
          .put("release_year", track.getReleaseYear());

        return sendPostRequest(TRACKS_TABLE, jsonBody, "добавлении нового трека: " + track.getTitle());
    }

    public CompletableFuture<Boolean> deleteTrack(int trackId) {
        String pathQuery = TRACKS_TABLE + "?id=eq." + trackId;
        return sendDeleteRequest(pathQuery, "удалении трека с ID " + trackId);
    }


    public CompletableFuture<Boolean> addToHistory(long userId, long trackId) {
        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put("user_id", userId)
                .put("track_id", trackId);

        return sendPostRequest(HISTORY_TABLE, jsonBody, "добавлении трека " + trackId + " в историю пользователя " + userId);
    }


    public CompletableFuture<List<Track>> getHistoryTracksByUser(long userId) {

        String query = HISTORY_TABLE + "?select=tracks(" + TRACKS_SELECT + ")&user_id=eq." + userId + "&order=listened_at.desc";

        HttpRequest request = createBaseRequestBuilder(query).GET().build();

        return sendAndDeserializeList(request, new TypeReference<List<Map<String, Object>>>() {}, "получении истории прослушиваний")
                .thenApply(rows -> {
                    List<Track> uniqueTracks = new ArrayList<>();
                    Set<Long> seenIds = new HashSet<>();

                    for (Map<String, Object> row : rows) {
                        if (row.containsKey("tracks") && row.get("tracks") != null) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> trackMap = (Map<String, Object>) row.get("tracks");

                            Track track = objectMapper.convertValue(trackMap, Track.class);

                            if (track != null && seenIds.add((long) track.getId())) {
                                uniqueTracks.add(track);
                            }
                        }
                    }
                    return uniqueTracks;
                });
    }


    public CompletableFuture<Boolean> addAuthorToTrack(long trackId, long authorId) {
        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put("track_id", trackId)
                .put("author_id", authorId);

        return sendPostRequest("/tracks_authors", jsonBody, "привязках автора " + authorId + " к треку " + trackId);
    }


    public CompletableFuture<Boolean> removeAuthorFromTrack(long trackId, long authorId) {
        String pathQuery = "/tracks_authors?track_id=eq." + trackId + "&author_id=eq." + authorId;
        return sendDeleteRequest(pathQuery, "удалении связи автора " + authorId + " с треком " + trackId);
    }


    public CompletableFuture<Boolean> updateTrack(Track track) {
        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put("title", track.getTitle())
                .put("album", track.getAlbum())
                .put("genre", track.getGenre())
                .put("cover_url", track.getCoverUrl())
                .put("audio_url", track.getTrackUrl())
                .put("lyrics", track.getLyrics())
                .put("release_year", track.getReleaseYear());

        String pathQuery = TRACKS_TABLE + "?id=eq." + track.getId();

        HttpRequest request = createBaseRequestBuilder(pathQuery)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                .build();

        return java.net.http.HttpClient.newHttpClient()
                .sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int status = response.statusCode();
                    boolean success = status >= 200 && status < 300;
                    if (!success) {
                        System.err.println("Ошибка при обновлении трека с ID " + track.getId() + ", код ответа: " + status);
                    }
                    return success;
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return false;
                });
    }


    public CompletableFuture<Boolean> removeAllAuthorsForTrack(long trackId) {
        String pathQuery = "/tracks_authors?track_id=eq." + trackId;
        return sendDeleteRequest(pathQuery, "удалении всех связей авторов для трека " + trackId);
    }


    private CompletableFuture<List<Track>> getHistoryTracksByUserFull(long userId) {
        String query = HISTORY_TABLE + "?select=tracks(" + TRACKS_SELECT + ")&user_id=eq." + userId + "&order=listened_at.desc";
        HttpRequest request = createBaseRequestBuilder(query).GET().build();
        return sendAndDeserializeList(request, new TypeReference<List<Map<String, Object>>>() {}, "получении полной истории")
                .thenApply(rows -> {
                    List<Track> list = new ArrayList<>();
                    for (Map<String, Object> row : rows) {
                        if (row.containsKey("tracks") && row.get("tracks") != null) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> trackMap = (Map<String, Object>) row.get("tracks");
                            Track track = objectMapper.convertValue(trackMap, Track.class);
                            if (track != null) list.add(track);
                        }
                    }
                    return list;
                });
    }

    public CompletableFuture<UserStats> getUserStats(long userId) {
        return getHistoryTracksByUserFull(userId).thenApply(tracks -> {
            UserStats stats = new UserStats();
            if (tracks == null || tracks.isEmpty()) {
                stats.setTotalPlays(0);
                stats.setTopTracks(Collections.singletonList("Нет данных"));
                stats.setTopArtist("Нет данных");
                stats.setFavoriteGenre("Нет данных");
                return stats;
            }

            stats.setTotalPlays(tracks.size());

            Map<String, Long> trackCounts = tracks.stream()
                    .filter(t -> t != null && t.getTitle() != null)
                    .collect(Collectors.groupingBy(
                            t -> t.getTitle() + " (" + (t.getArtist() != null ? t.getArtist() : "Неизвестен") + ")",
                            Collectors.counting()
                    ));

            List<String> top5 = trackCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .map(entry -> entry.getKey() + " — " + entry.getValue() + " просл.")
                    .collect(Collectors.toList());

            stats.setTopTracks(top5.isEmpty() ? Collections.singletonList("Нет данных") : top5);


            Map<String, Long> artistCounts = tracks.stream()
                    .filter(t -> t.getArtist() != null)
                    .collect(Collectors.groupingBy(Track::getArtist, Collectors.counting()));
            stats.setTopArtist(artistCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey).orElse("Нет данных"));

            Map<String, Long> genreCounts = tracks.stream()
                    .filter(t -> t.getGenre() != null && !t.getGenre().trim().isEmpty())
                    .collect(Collectors.groupingBy(Track::getGenre, Collectors.counting()));
            stats.setFavoriteGenre(genreCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey).orElse("Не указан"));

            return stats;
        });
    }




}