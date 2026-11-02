package com.example.postgresql;

import com.example.postgresql.Models.RadioStation;
import com.example.postgresql.Models.Track;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

public class RadioPlayerController {

    @FXML private ImageView coverImage;
    @FXML private Label titleLabel;
    @FXML private Button playPauseButton;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Slider volumeSlider;

    private final MusicPlayerManager playerManager = MusicPlayerManager.getInstance();
    private Form1 mainController;

    private ImageView playIcon;
    private ImageView pauseIcon;
    private ImageView prevIcon;
    private ImageView nextIcon;

    @FXML
    private void initialize() {
        try {
            playIcon = new ImageView(new Image(getClass().getResource("/images/play.png").toExternalForm()));
            pauseIcon = new ImageView(new Image(getClass().getResource("/images/pause.png").toExternalForm()));
            prevIcon = new ImageView(new Image(getClass().getResource("/images/rewind.png").toExternalForm()));
            nextIcon = new ImageView(new Image(getClass().getResource("/images/forward.png").toExternalForm()));

            playIcon.setFitWidth(80); playIcon.setFitHeight(80);
            pauseIcon.setFitWidth(80); pauseIcon.setFitHeight(80);
            prevIcon.setFitWidth(35); prevIcon.setFitHeight(45);
            nextIcon.setFitWidth(35); nextIcon.setFitHeight(45);

            playPauseButton.setGraphic(playerManager.playingProperty().get() ? pauseIcon : playIcon);
            prevButton.setGraphic(prevIcon);
            nextButton.setGraphic(nextIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }

        playerManager.playingProperty().addListener((obs, oldVal, isPlaying) -> {
            playPauseButton.setGraphic(isPlaying ? pauseIcon : playIcon);
        });

        volumeSlider.setValue(playerManager.volumeProperty().get());
        volumeSlider.valueProperty().addListener((obs, oldV, newV) -> playerManager.volumeProperty().set(newV.doubleValue()));

        playerManager.currentTrackProperty().addListener((obs, oldT, newT) -> {
            if (newT != null && newT.isRadio()) {
                loadStationVisuals(newT);
            }
        });

        Track current = playerManager.getCurrentTrack();
        if (current != null && current.isRadio()) {
            loadStationVisuals(current);
        }
    }

    public void setMainController(Form1 controller) {
        this.mainController = controller;
    }

    public void setStationTracks(List<Track> tracks) {}

    public void setStation(RadioStation station) {}

    private void loadStationVisuals(Track track) {
        titleLabel.setText(track.getTitle());
        try {
            if (track.getCoverUrl() != null && !track.getCoverUrl().trim().isEmpty()) {
                coverImage.setImage(new Image(track.getCoverUrl(), 500, 500, true, true));
            } else {
                coverImage.setImage(new Image("https://via.placeholder.com/500x500/7733ff/ffffff?text=Radio", true));
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки обложки радио: " + track.getTitle());
        }
    }

    @FXML
    private void togglePlayPause() {
        playerManager.togglePlayPause();
    }

    @FXML
    private void handlePrevStation() {
        playerManager.playPrev();
    }

    @FXML
    private void handleNextStation() {
        playerManager.playNext();
    }

    @FXML
    private void closePlayer() {
        if (mainController != null) {
            mainController.showMainView();
        }
    }
}