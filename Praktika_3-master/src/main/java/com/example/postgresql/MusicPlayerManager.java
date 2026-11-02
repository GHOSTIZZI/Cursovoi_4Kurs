package com.example.postgresql;

import com.example.postgresql.Models.Track;
import javafx.beans.property.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.List;

public class MusicPlayerManager {

    private static final MusicPlayerManager instance = new MusicPlayerManager();

    private MediaPlayer mediaPlayer;

    private final ObjectProperty<Track> currentTrack = new SimpleObjectProperty<>();
    private final BooleanProperty playing = new SimpleBooleanProperty(false);
    private final BooleanProperty looping = new SimpleBooleanProperty(false);
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final DoubleProperty volume = new SimpleDoubleProperty(0.5);

    private final StringProperty currentTimeString = new SimpleStringProperty("0:00");
    private final StringProperty durationString = new SimpleStringProperty("0:00");

    private List<Track> currentPlaylist;
    private int currentIndex = -1;

    private long lastPrevClickTime = 0;


    private MusicPlayerManager() {
        volume.addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue());
            }
        });
    }

    public static MusicPlayerManager getInstance() {
        return instance;
    }

    public void playTrack(Track track, List<Track> playlist) {
        this.currentPlaylist = playlist;
        if (playlist != null) {
            for (int i = 0; i < playlist.size(); i++) {
                if (playlist.get(i).getId() == track.getId()) {
                    currentIndex = i;
                    break;
                }
            }
        }
        playTrackInternal(track);
    }

    private void playTrackInternal(Track track) {
        if (track == null || track.getAudioUrl() == null || track.getAudioUrl().isBlank()) return;

        if (mediaPlayer != null && currentTrack.get() != null && currentTrack.get().getId() == track.getId()) {
            if (mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                mediaPlayer.play();
                playing.set(true);
            }
            return;
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }

        currentTrack.set(track);

        try {
            Media media = new Media(track.getAudioUrl());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(volume.get());

            mediaPlayer.setCycleCount(looping.get() ? MediaPlayer.INDEFINITE : 1);

            mediaPlayer.setOnReady(() -> {
                Duration total = media.getDuration();
                durationString.set(formatTime(total));
                mediaPlayer.play();
                playing.set(true);
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldT, newT) -> {
                if (currentTrack.get() != null && currentTrack.get().isRadio()) {
                    progress.set(0.0);
                    currentTimeString.set("LIVE");
                    return;
                }
                Duration totalDuration = media.getDuration();
                if (totalDuration != null && !totalDuration.isUnknown() && !totalDuration.isIndefinite() && totalDuration.toSeconds() > 0) {
                    progress.set(newT.toSeconds() / totalDuration.toSeconds());
                    currentTimeString.set(formatTime(newT));
                }
            });


            mediaPlayer.setOnEndOfMedia(() -> {
                if (!looping.get()) {
                    playNext();
                }
            });

            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void togglePlayPause() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playing.set(false);
        } else {
            mediaPlayer.play();
            playing.set(true);
        }
    }

    public void playNext() {
        if (currentPlaylist == null || currentPlaylist.isEmpty()) return;
        currentIndex = (currentIndex + 1) % currentPlaylist.size();
        playTrackInternal(currentPlaylist.get(currentIndex));
    }


    public void playPrev() {
        if (currentPlaylist == null || currentPlaylist.isEmpty()) return;

        long now = System.currentTimeMillis();
        if (now - lastPrevClickTime < 400) {
            // Двойной клик
            currentIndex = (currentIndex - 1 + currentPlaylist.size()) % currentPlaylist.size();
            playTrackInternal(currentPlaylist.get(currentIndex));
            lastPrevClickTime = 0;
        } else {
            // Одинарный клик
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.ZERO);
            }
            lastPrevClickTime = now;
        }
    }


    public void toggleLoop() {
        boolean newVal = !looping.get();
        looping.set(newVal);
        if (mediaPlayer != null) {
            mediaPlayer.setCycleCount(newVal ? MediaPlayer.INDEFINITE : 1);
        }

    }

    public boolean isLooping() {
        return looping.get();
    }


    public void seek(double progressPercent) {
        if (mediaPlayer != null && mediaPlayer.getMedia().getDuration() != null) {
            Duration target = mediaPlayer.getMedia().getDuration().multiply(progressPercent);
            mediaPlayer.seek(target);
        }
    }

    private String formatTime(Duration duration) {
        if (currentTrack.get() != null && currentTrack.get().isRadio()) return "LIVE";
        if (duration == null || duration.isUnknown() || duration.isIndefinite()) return "0:00";
        int total = (int) duration.toSeconds();
        int min = total / 60;
        int sec = total % 60;
        return String.format("%d:%02d", min, sec);
    }

    public ObjectProperty<Track> currentTrackProperty() {
        return currentTrack;
    }

    public BooleanProperty playingProperty() {
        return playing;
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public DoubleProperty volumeProperty() {
        return volume;
    }

    public StringProperty currentTimeStringProperty() {
        return currentTimeString;
    }

    public StringProperty durationStringProperty() {
        return durationString;
    }

    public Track getCurrentTrack() {
        return currentTrack.get();
    }

    public List<Track> getCurrentPlaylist() {
        return currentPlaylist;
    }

    public BooleanProperty loopingProperty() {
        return looping;
    }
}