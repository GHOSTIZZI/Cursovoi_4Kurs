package com.example.postgresql;

import com.example.postgresql.DAO.FavoriteDAO;
import com.example.postgresql.DAO.TrackDAO;
import com.example.postgresql.Models.Playlist;
import com.example.postgresql.Models.Track;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CancellationException;

public class TrackDetailController {

    private final PlaylistService playlistService = new PlaylistService();
    private final FavoriteDAO favoriteDAO = new FavoriteDAO();
    private final TrackDAO trackDAO = new TrackDAO();

    private final MusicPlayerManager playerManager = MusicPlayerManager.getInstance();

    @FXML
    private ImageView coverImage;

    @FXML
    private Label titleLabel;
    @FXML
    private Label artistLabel;

    @FXML
    private Slider volumeSlider;
    @FXML
    private Slider progressSlider;

    @FXML
    private Label currentTimeLabel;
    @FXML
    private Label durationLabel;


    @FXML
    private Button downloadButton;
    @FXML
    private Button favoriteButton;
    @FXML
    private Button loopButton;
    @FXML
    private Button prevButton;
    @FXML
    private Button playPauseButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button libraryAdd;
    @FXML
    private Button lyricsButton;
    private boolean isShowingLyrics = false;

    @FXML
    private ScrollPane lyricsPane;
    @FXML
    private VBox lyricsContainer;


    private Track track;
    private Form1 mainController;
    private List<Track> allTracks;


    private ImageView favoriteFillIcon;
    private ImageView favoriteUnfillIcon;
    private ImageView playIcon;
    private ImageView pauseIcon;
    private ImageView prevIcon;
    private ImageView nextIcon;
    private ImageView libraryAddIcon;
    private ImageView loopIcon;
    private ImageView loopActiveIcon;
    private ImageView downloadIcon;
    private ImageView textIcon;


    @FXML
    private void initialize() {
        playIcon = new ImageView(new Image(getClass().getResource("/images/play.png").toExternalForm()));
        pauseIcon = new ImageView(new Image(getClass().getResource("/images/pause.png").toExternalForm()));
        prevIcon = new ImageView(new Image(getClass().getResource("/images/rewind.png").toExternalForm()));
        nextIcon = new ImageView(new Image(getClass().getResource("/images/forward.png").toExternalForm()));
        libraryAddIcon = new ImageView(new Image(getClass().getResource("/images/library_add.png").toExternalForm()));

        playIcon.setFitWidth(80);
        playIcon.setFitHeight(80);
        pauseIcon.setFitWidth(80);
        pauseIcon.setFitHeight(80);
        prevIcon.setFitWidth(40);
        prevIcon.setFitHeight(50);
        nextIcon.setFitWidth(40);
        nextIcon.setFitHeight(50);
        libraryAddIcon.setFitWidth(27);
        libraryAddIcon.setFitHeight(27);

        playPauseButton.setGraphic(playIcon);
        prevButton.setGraphic(prevIcon);
        nextButton.setGraphic(nextIcon);
        libraryAdd.setGraphic(libraryAddIcon);

        favoriteFillIcon = new ImageView(new Image(getClass().getResource("/images/favoritefill.png").toExternalForm()));
        favoriteUnfillIcon = new ImageView(new Image(getClass().getResource("/images/favoriteunfill.png").toExternalForm()));
        favoriteFillIcon.setFitWidth(30);
        favoriteFillIcon.setFitHeight(30);
        favoriteUnfillIcon.setFitWidth(30);
        favoriteUnfillIcon.setFitHeight(30);

        loopIcon = new ImageView(new Image(getClass().getResource("/images/loop.png").toExternalForm()));
        loopActiveIcon = new ImageView(new Image(getClass().getResource("/images/loopactive.png").toExternalForm()));

        textIcon = new ImageView(new Image(getClass().getResource("/images/text.png").toExternalForm()));
        textIcon.setFitWidth(22);
        textIcon.setFitHeight(22);
        lyricsButton.setGraphic(textIcon);
        lyricsButton.setText("");

        loopIcon.setFitWidth(25);
        loopIcon.setFitHeight(20);
        loopActiveIcon.setFitWidth(25);
        loopActiveIcon.setFitHeight(25);

        playerManager.loopingProperty().addListener((obs, oldVal, isLooping) -> {
            loopButton.setGraphic(isLooping ? loopActiveIcon : loopIcon);
        });

        loopButton.setGraphic(playerManager.isLooping() ? loopActiveIcon : loopIcon);

        artistLabel.setOnMouseClicked(event -> {
            if (track != null && mainController != null) {
                mainController.openAuthorCatalog(track.getArtist());
            }
        });

        playerManager.playingProperty().addListener((obs, oldVal, isPlaying) -> {
            playPauseButton.setGraphic(isPlaying ? pauseIcon : playIcon);
        });

        progressSlider.setMax(1.0);

        playerManager.progressProperty().addListener((obs, oldVal, progress) -> {
            if (!progressSlider.isValueChanging()) {
                progressSlider.setValue(progress.doubleValue());
            }
        });

        progressSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                playerManager.seek(progressSlider.getValue());
            }
        });

        currentTimeLabel.textProperty().bind(playerManager.currentTimeStringProperty());
        durationLabel.textProperty().bind(playerManager.durationStringProperty());

        volumeSlider.setValue(playerManager.volumeProperty().get());
        volumeSlider.valueProperty().addListener((obs, oldV, newV) -> playerManager.volumeProperty().set(newV.doubleValue()));

        playerManager.currentTrackProperty().addListener((obs, oldT, newT) -> {
            if (newT != null) {
                this.track = newT;
                loadTrackVisuals();
                setupFavoriteButton();
            }
        });

        try {
            downloadIcon = new ImageView(new Image(getClass().getResource("/images/download.png").toExternalForm()));
            downloadIcon.setFitWidth(25);
            downloadIcon.setFitHeight(25);
            downloadButton.setGraphic(downloadIcon);
        } catch (Exception e) {
            downloadButton.setText("DWD");
        }


    }

    public void setTrackList(List<Track> tracks) {
        this.allTracks = tracks;
    }

    public void setMainController(Form1 controller) {
        this.mainController = controller;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;

        if (playerManager.getCurrentTrack() == null || playerManager.getCurrentTrack().getId() != track.getId()) {
            playerManager.playTrack(track, allTracks);
            if (mainController != null && mainController.getCurrentUserId() > 0) {
                int userId = mainController.getCurrentUserId();
                trackDAO.addToHistory(userId, track.getId())
                        .thenAccept(success -> {
                            if (success && mainController != null) {
                                Platform.runLater(mainController::loadHistoryCards);
                            }
                        });
            }
        }

        loadTrackVisuals();
        setupFavoriteButton();
        playPauseButton.setGraphic(playerManager.playingProperty().get() ? pauseIcon : playIcon);
    }

    private void loadTrackVisuals() {
        if (track == null) return;
        titleLabel.setText(track.getTitle());
        artistLabel.setText(track.getArtist());

        if (lyricsContainer != null) {
            lyricsContainer.getChildren().clear();
            String rawLyrics = track.getLyrics();
            if (rawLyrics != null && !rawLyrics.trim().isEmpty()) {
                String[] verses = rawLyrics.split("\n");
                for (String verse : verses) {
                    Label lineLabel = new Label(verse);
                    lineLabel.setWrapText(true);
                    lineLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                    if (verse.trim().isEmpty()) {
                        lineLabel.setMinHeight(15);
                    } else {
                        lineLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #e0e0e0; -fx-padding: 2 0;");
                    }
                    lyricsContainer.getChildren().add(lineLabel);
                }
                lyricsButton.setDisable(false);
            } else {
                Label emptyLabel = new Label("Для этого трека пока нет текста.");
                emptyLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #888888;");
                lyricsContainer.getChildren().add(emptyLabel);
            }
        }

        try {
            if (track.getCoverUrl() != null && !track.getCoverUrl().trim().isEmpty()) {
                coverImage.setImage(new Image(track.getCoverUrl(), true));
            } else {
                coverImage.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки обложки: " + track.getCoverUrl());
        }
    }

    private void setupFavoriteButton() {
        if (mainController == null || mainController.getCurrentUserId() <= 0 || track == null) {
            favoriteButton.setVisible(false);
            return;
        }
        favoriteButton.setVisible(true);
        favoriteButton.setText("");
        favoriteButton.setDisable(true);

        int userId = mainController.getCurrentUserId();

        favoriteDAO.isFavorite(userId, track.getId())
                .thenAccept(isFav -> Platform.runLater(() -> {
                    favoriteButton.setDisable(false);
                    updateFavoriteButtonUI(isFav);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        favoriteButton.setDisable(false);
                        if (!(ex.getCause() instanceof CancellationException) && mainController != null) {
                            mainController.showError("Не удалось проверить статус избранного: " + ex.getMessage());
                        }
                    });
                    return null;
                });
    }

    private void updateFavoriteButtonUI(boolean isFav) {
        favoriteButton.getStyleClass().removeAll("favorite-added", "favorite-not-added");
        if (isFav) {
            favoriteButton.setGraphic(favoriteFillIcon);
            favoriteButton.setText("");
            favoriteButton.getStyleClass().add("favorite-added");
        } else {
            favoriteButton.setGraphic(favoriteUnfillIcon);
            favoriteButton.setText("");
            favoriteButton.getStyleClass().add("favorite-not-added");
        }
    }

    @FXML
    private void togglePlayPause() {
        playerManager.togglePlayPause();
    }

    @FXML
    private void handlePrevButton() {
        playerManager.playPrev();
    }

    @FXML
    private void handleNextButton() {
        playerManager.playNext();
    }

    @FXML
    private void addToFavorites() {
        if (mainController == null || track == null) return;
        int userId = mainController.getCurrentUserId();
        if (userId <= 0) {
            mainController.showError("Ошибка: пользователь не авторизован");
            return;
        }
        favoriteButton.setDisable(true);
        boolean isCurrentlyFavorite = favoriteButton.getStyleClass().contains("favorite-added");

        CompletableFuture<Boolean> future = isCurrentlyFavorite
                ? favoriteDAO.removeFromFavorites(userId, track.getId())
                : favoriteDAO.addToFavorites(userId, track.getId());

        future.thenAccept(success -> Platform.runLater(() -> {
            favoriteButton.setDisable(false);
            if (success) {
                updateFavoriteButtonUI(!isCurrentlyFavorite);
            } else {
                mainController.showError("Ошибка при обновлении избранного.");
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                favoriteButton.setDisable(false);
                mainController.showError("Сетевая ошибка: " + ex.getMessage());
            });
            return null;
        });
    }

    @FXML
    private void goBack() {
        if (mainController != null) {
            mainController.showMainView();
            mainController.loadHistoryCards();
        }
    }

    @FXML
    private void toggleLoop() {
        playerManager.toggleLoop();
    }

    @FXML
    private void addToPlaylist() {
        if (track == null || mainController == null) return;
        int userId = mainController.getCurrentUserId();

        if (userId <= 0) {
            mainController.showError("Необходимо войти в систему.");
            return;
        }

        playlistService.loadUserPlaylists(userId).thenAccept(playlists -> Platform.runLater(() -> showPlaylistContextMenu(playlists)));
    }

    private void showPlaylistContextMenu(List<Playlist> playlists) {
        ContextMenu contextMenu = new ContextMenu();

        contextMenu.setStyle(
                "-fx-background-color: #1a1a1a; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 5;"
        );

        if (playlists == null || playlists.isEmpty()) {
            MenuItem emptyItem = new MenuItem("У вас нет плейлистов");
            emptyItem.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");
            emptyItem.setDisable(true);
            contextMenu.getItems().add(emptyItem);
        } else {
            for (Playlist playlist : playlists) {
                MenuItem item = new MenuItem(playlist.getTitle());
                item.setStyle(
                        "-fx-text-fill: #ffffff; " +
                                "-fx-font-size: 14px; " +
                                "-fx-padding: 8 15 8 15; " +
                                "-fx-background-color: transparent;"
                );


                item.setOnAction(e -> {
                    playlistService.addTrack(playlist.getId(), track.getId())
                            .thenAccept(success -> Platform.runLater(() -> {
                                if (success) {
                                    System.out.println("Трек добавлен в плейлист: " + playlist.getTitle());
                                } else {
                                    mainController.showAlert("Напоминание","Трек уже есть в этом плейлисте.");
                                }
                            }));
                });
                contextMenu.getItems().add(item);
            }
        }

        contextMenu.show(libraryAdd, javafx.geometry.Side.BOTTOM, 0, 5);
    }

    @FXML
    private void toggleLyrics() {
        isShowingLyrics = !isShowingLyrics;
        if (isShowingLyrics) {
            coverImage.setVisible(false);
            coverImage.setManaged(false);
            lyricsPane.setVisible(true);
            lyricsPane.setManaged(true);
            lyricsButton.setStyle("-fx-background-color: #181818; -fx-background-radius: 12; -fx-padding: 8; -fx-cursor: hand;");
        } else {
            coverImage.setVisible(true);
            coverImage.setManaged(true);
            lyricsPane.setVisible(false);
            lyricsPane.setManaged(false);
            lyricsButton.setStyle("-fx-background-color: #0f0f0f; -fx-background-radius: 12; -fx-padding: 8; -fx-cursor: hand;");
        }
    }


    @FXML
    private void downloadTrack() {
        if (track == null || track.getAudioUrl() == null || track.getAudioUrl().isBlank()) {
            if (mainController != null) mainController.showError("Невозможно скачать: ссылка на аудио отсутствует.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить трек");

        String safeFileName = (track.getArtist() + " - " + track.getTitle()).replaceAll("[\\\\/:*?\"<>|]", "_") + ".mp3";
        fileChooser.setInitialFileName(safeFileName);


        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files (*.mp3)", "*.mp3"));

        Stage stage = (Stage) downloadButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            downloadButton.setDisable(true);

            CompletableFuture.runAsync(() -> {
                try {
                    java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                            .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                            .build();

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(java.net.URI.create(track.getAudioUrl()))
                            .GET()
                            .build();

                    HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

                    if (response.statusCode() == 200) {
                        java.nio.file.Files.write(file.toPath(), response.body());
                        Platform.runLater(() -> {
                            downloadButton.setDisable(false);
                            if (mainController != null) {
                                mainController.showAlert("Успешно", "Трек успешно скачан!");
                            }
                        });
                    } else {
                        throw new RuntimeException("Сервер вернул код ошибки: " + response.statusCode());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        downloadButton.setDisable(false);
                        if (mainController != null) {
                            mainController.showError("Не удалось скачать трек: " + e.getMessage());
                        }
                    });
                }
            });
        }
    }
}