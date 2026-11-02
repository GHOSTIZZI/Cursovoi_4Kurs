package com.example.postgresql;

import com.example.postgresql.DAO.AuthorDAO;
import com.example.postgresql.DAO.FavoriteDAO;
import com.example.postgresql.DAO.RadioDAO;
import com.example.postgresql.DAO.TrackDAO;
import com.example.postgresql.Models.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CancellationException;

import java.util.stream.Collectors;


public class Form1 {

    @FXML
    private Button miniFavoriteButton;
    @FXML
    private Button miniLoopButton;

    private ImageView miniFavFillIcon;
    private ImageView miniFavUnfillIcon;
    private ImageView miniLoopIcon;
    private ImageView miniLoopActiveIcon;

    @FXML
    private Button miniPrevButton;
    @FXML
    private Button miniNextButton;

    private ImageView miniPlayIcon;
    private ImageView miniPauseIcon;
    private ImageView miniPrevIcon;
    private ImageView miniNextIcon;

    @FXML
    private Label currentTimeLabel;
    @FXML
    private Label durationLabel;

    private boolean isFullPlayerOpen = false;

    @FXML
    private HBox miniPlayerBar;

    @FXML
    private ImageView LogoutImage;
    @FXML
    private ImageView LibraryImage;
    @FXML
    private ImageView miniCoverImage;

    @FXML
    private Label miniTitleLabel;
    @FXML
    private Label miniArtistLabel;

    @FXML
    private Button miniPlayButton;
    @FXML
    private Slider miniProgressBar;

    private final MusicPlayerManager playerManager = MusicPlayerManager.getInstance();

    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private FlowPane musicCardsPane;
    @FXML
    private BorderPane rootContainer;
    @FXML
    private TextField searchField;
    @FXML
    private FlowPane authorsPane;
    @FXML
    private VBox mainContent;
    @FXML
    private Label adminPanelBtn;
    @FXML
    private FlowPane radioCardsPane;
    @FXML
    private FlowPane featuredPlaylistsPane;

    @FXML
    private FlowPane historyPane;

    private final TrackDAO trackDAO = new TrackDAO();
    private final AuthorDAO authorDAO = new AuthorDAO();
    private final FavoriteDAO favoriteDAO = new FavoriteDAO();
    private final RadioDAO radioDAO = new RadioDAO();
    private final PlaylistService playlistService = new PlaylistService();

    private String currentUserRole;
    private String currentUserLogin;

    private int currentUserId = 0;
    private VBox searchContent = new VBox();


    private Timeline debounceTimer = null;

    private CompletableFuture<SearchResult> currentSearchFutureResult;


    //---------------------------------------------------------
    // Сеттеры и Инициализация
    //---------------------------------------------------------

    public BorderPane getRootContainer() {
        return rootContainer;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentUserLogin() {
        return currentUserLogin;
    }

    public void setCurrentUser(int id, String login, String role) {
        this.currentUserId = id;
        this.currentUserLogin = login;
        this.currentUserRole = role;
        if (adminPanelBtn != null) {
            adminPanelBtn.setVisible("admin".equals(role));
        }
        loadHistoryCards();
    }



    @FXML
    public void initialize() {
        if (adminPanelBtn != null) {
            adminPanelBtn.setVisible("admin".equals(currentUserRole));
        }

        loadInitialData();
        loadfavoriteImage();
        loadAccImage();
        loadPlaylistImage();
        loadpowerImage();
        loadFeaturedAlbums();
        initMiniPlayer();


        searchField.textProperty().addListener((observable, oldValue, newValue) -> {

            if (debounceTimer != null) {
                debounceTimer.stop();
            }

            if (newValue == null || newValue.trim().isEmpty()) {
                handleSearchInput("");
                return;
            }

            debounceTimer = new Timeline(new KeyFrame(Duration.millis(300), e -> {
                handleSearchInput(newValue);
            }));
            debounceTimer.play();
        });
    }

    private void loadInitialData() {
        createMusicCards();
        loadAuthors();
        createRadioCards();
    }

    public void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void showMainView() {
        isFullPlayerOpen = false;
        rootContainer.setCenter(mainScrollPane);

        if (playerManager.getCurrentTrack() != null) {
            miniPlayerBar.setVisible(true);
            miniPlayerBar.setManaged(true);
        }
    }

    @FXML
    public void openAdminPanel() {


        if (!"admin".equals(currentUserRole)) {
            showError("У вас нет прав для доступа к админ-панели.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminPanel.fxml"));
            Parent root = loader.load();
            AdminPanelController adminController = loader.getController();
            adminController.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Админ-панель");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);

            stage.setOnHiding(event -> {
                refreshAllData();
            });

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть админ-панель.");
        }
    }

    @FXML
    public void openIzbran() {
        if (currentUserId <= 0) {
            showAlert("Авторизация", "Для просмотра избранного необходимо войти в систему.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("izbran.fxml"));
            Parent content = loader.load();

            IzbranController controller = loader.getController();

            if (controller != null) {
                controller.setMainController(this);
            }
            rootContainer.setCenter(content);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть «Избранное».");
        }
    }

    @FXML
    public void openAccount() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Acc.fxml"));
            Parent content = loader.load();
            AccController controller = loader.getController();
            if (controller != null) {
                controller.setMainController(this);
            }
            rootContainer.setCenter(content);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть «Аккаунт».");
        }
    }

    @FXML
    private void logout() {

        Stage stage = (Stage) rootContainer.getScene().getWindow();
        stage.close();
    }


    // Логика Радио


    private void createRadioCards() {
        radioDAO.getAllStations()
                .thenAccept(allStations -> {
                    Platform.runLater(() -> {
                        if (radioCardsPane != null) {
                            radioCardsPane.getChildren().clear();
                            for (RadioStation station : allStations) {
                                StackPane card = createRadioCard(station);
                                radioCardsPane.getChildren().add(card);
                            }
                        }
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    showError("Не удалось загрузить радиостанции.");
                    return null;
                });
    }


    public StackPane createRadioCard(RadioStation station) {
        StackPane card = new StackPane();
        card.setPrefSize(180, 220);
        card.setStyle(
                "-fx-background-color: #1e1e1e; " +
                        "-fx-border-color: #333333; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0, 0, 2);"
        );
        card.setCursor(javafx.scene.Cursor.HAND);

        card.setOnMouseEntered(e ->
                card.setStyle(card.getStyle() + " -fx-effect: dropshadow(gaussian, rgba(179,136,255,0.2), 10, 0, 0, 4);")
        );
        card.setOnMouseExited(e ->
                card.setStyle(card.getStyle().replaceAll(" -fx-effect:.*?;",
                        " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0, 0, 2);"))
        );

        VBox content = new VBox();
        content.setSpacing(8);
        content.setPadding(new Insets(12));
        content.setAlignment(Pos.TOP_CENTER);

        ImageView cover = new ImageView();
        cover.setFitWidth(160);
        cover.setFitHeight(160);
        cover.setPreserveRatio(true);

        try {
            if (station.getCoverUrl() != null && !station.getCoverUrl().trim().isEmpty()) {

                cover.setImage(new Image(station.getCoverUrl(), 160, 160, true, true, true));
            } else {
                cover.setImage(new Image("https://via.placeholder.com/160x160/7733ff/ffffff?text=Radio", true));
            }
        } catch (Exception e) {
            System.err.println("Не удалось загрузить обложку радиостанции: " + station.getName());
            cover.setImage(new Image("https://via.placeholder.com/160x160/444444/b388ff?text=Error", true));
        }

        Text titleText = new Text(station.getName());
        titleText.setWrappingWidth(160);
        titleText.setStyle("-fx-font-size: 14px; -fx-fill: #ffffff; -fx-font-weight: bold;");

        content.getChildren().addAll(cover, titleText);
        card.getChildren().add(content);

        card.setOnMouseClicked(event -> openRadioPlayer(station));

        return card;
    }


    public void openRadioPlayer(RadioStation station) {
        isFullPlayerOpen = true;
        miniPlayerBar.setVisible(false);
        miniPlayerBar.setManaged(false);

        radioDAO.getAllStations().thenAccept(allStations -> {
            Platform.runLater(() -> {
                List<Track> radioTracks = new ArrayList<>();
                Track targetTrack = null;

                for (RadioStation s : allStations) {
                    Track t = new Track();
                    t.setId(-s.getId());
                    t.setTitle(s.getName());
                    t.setArtist("Прямой эфир / Радио");
                    t.setAudioUrl(s.getStreamUrl());
                    t.setCoverUrl(s.getCoverUrl());
                    t.setRadio(true);
                    radioTracks.add(t);

                    if (s.getId() == station.getId()) {
                        targetTrack = t;
                    }
                }

                if (targetTrack == null) {
                    targetTrack = radioTracks.get(0);
                }

                playerManager.playTrack(targetTrack, radioTracks);

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("radio_player.fxml"));
                    Parent playerView = loader.load();
                    RadioPlayerController controller = loader.getController();
                    controller.setMainController(this);
                    controller.setStationTracks(radioTracks); // Передаем плейлист радио в контроллер
                    rootContainer.setCenter(playerView);
                } catch (IOException e) {
                    e.printStackTrace();
                    showError("Не удалось открыть плеер радиостанции.");
                }
            });
        });
    }



    // Логика поиска




    private void handleSearchInput(String query) {
        if (query == null || query.trim().isEmpty()) {
            showMainView();
            return;
        }

        if (currentSearchFutureResult != null && !currentSearchFutureResult.isDone()) {
            currentSearchFutureResult.cancel(true);
        }

        CompletableFuture<List<Track>> tracksFuture = trackDAO.searchTracks(query);
        CompletableFuture<List<Author>> authorsFuture = authorDAO.searchAuthorsByName(query);

        currentSearchFutureResult = tracksFuture.thenCombine(authorsFuture, SearchResult::new);

        openSearchViewPending();

        currentSearchFutureResult.thenAccept(result -> {
            Platform.runLater(() -> openSearchView(query, result.getTracks(), result.getAuthors()));
        }).exceptionally(ex -> {
            if (ex.getCause() instanceof CancellationException) return null;
            ex.printStackTrace();
            Platform.runLater(() -> showError("Ошибка поиска."));
            return null;
        });
    }

    private void openSearchViewPending() {

        searchContent.getChildren().clear();
        searchContent.setSpacing(24);
        searchContent.setPadding(new Insets(20));

        Label title = new Label("Результаты поиска");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setPrefSize(50, 50);

        searchContent.getChildren().addAll(title, indicator);
        rootContainer.setCenter(searchContent);
    }

    private void openSearchView(String query, List<Track> tracks, List<Author> authors) {
        String q = query.toLowerCase().trim();

        List<Track> filteredByQuery = tracks.stream().filter(track -> {
                    if (track == null) return false;
                    boolean matchTitle = track.getTitle() != null && track.getTitle().toLowerCase().contains(q);
                    boolean matchAuthor = track.getAuthorName() != null && track.getAuthorName().toLowerCase().contains(q);
                    return matchTitle || matchAuthor;
                })
                .collect(Collectors.toList());


        Set<Integer> uniqueTrackIds = new HashSet<>();
        List<Track> trulyUniqueTracks = new ArrayList<>();
        for (Track track : filteredByQuery) {
            if (track != null && uniqueTrackIds.add(track.getId())) {
                trulyUniqueTracks.add(track);
            }
        }

        searchContent.getChildren().clear();
        searchContent.setSpacing(24);
        searchContent.setPadding(new Insets(20));

        Label title = new Label("Результаты поиска: \"" + query + "\"");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        FlowPane resultPane = new FlowPane();
        resultPane.setHgap(20);
        resultPane.setVgap(20);
        resultPane.setPrefWrapLength(600);

        if (!trulyUniqueTracks.isEmpty()) {
            for (Track track : trulyUniqueTracks) {
                resultPane.getChildren().add(createMusicCard(track));
            }
        }

        if (authors != null && !authors.isEmpty()) {
            for (Author author : authors) {
                VBox authorCard = createAuthorCard(author.getName(), author.getPhotoUrl());
                authorCard.setOnMouseClicked(e -> openAuthorCatalog(author.getName()));
                resultPane.getChildren().add(authorCard);
            }
        }

        if (trulyUniqueTracks.isEmpty() && (authors == null || authors.isEmpty())) {
            Label notFound = new Label("Ничего не найдено");
            notFound.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            resultPane.getChildren().add(notFound);
        }

        searchContent.getChildren().addAll(title, resultPane);
        rootContainer.setCenter(searchContent);
    }


    // Логика Открытия Трека


    public void openTrackDetail(Track track, List<Track> playlist) {
        isFullPlayerOpen = true;
        miniPlayerBar.setVisible(false);
        miniPlayerBar.setManaged(false);

        if (playerManager.getCurrentTrack() == null || playerManager.getCurrentTrack().getId() != track.getId()) {
            playerManager.playTrack(track, playlist);
            if (currentUserId > 0) {
                trackDAO.addToHistory(currentUserId, track.getId())
                        .thenAccept(success -> Platform.runLater(this::loadHistoryCards));
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("track_detail.fxml"));
            VBox detailView = loader.load();
            TrackDetailController controller = loader.getController();
            controller.setMainController(this);
            controller.setTrackList(playlist);
            controller.setTrack(track);

            rootContainer.setCenter(detailView);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Не удалось открыть плеер.");
        }
    }

    public void openTrackDetail(Track track) {
        trackDAO.getAllTracks()
                .thenAccept(allTracks -> Platform.runLater(() -> {
                    if (allTracks.isEmpty()) {
                        showError("Не удалось загрузить плейлист.");
                    } else {
                        openTrackDetail(track, allTracks);
                    }
                }))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    showError("Ошибка сети при загрузке плейлиста.");
                    return null;
                });
    }

    // Создание Карточек и Каталогов


    public void createMusicCards() {
        if (musicCardsPane == null) return;

        musicCardsPane.getChildren().clear();
        musicCardsPane.getChildren().add(new ProgressIndicator());

        trackDAO.getAllTracks()
                .thenAccept(allTracks -> Platform.runLater(() -> renderMainTracks(allTracks)))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        musicCardsPane.getChildren().clear();
                        musicCardsPane.getChildren().add(new Label("Ошибка загрузки треков."));
                        showError("Не удалось загрузить треки.");
                    });
                    return null;
                });
    }

    public StackPane createMusicCard(Track track) {
        StackPane card = new StackPane();
        card.setPrefSize(180, 240);
        card.setStyle(
                "-fx-background-color: #1e1e1e; " +
                        "-fx-border-color: #333333; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0, 0, 2);"
        );


        card.setOnMouseEntered(e ->
                card.setStyle(card.getStyle() + " -fx-effect: dropshadow(gaussian, rgba(179,136,255,0.2), 10, 0, 0, 4);")
        );
        card.setOnMouseExited(e ->
                card.setStyle(card.getStyle().replaceAll(" -fx-effect:.*?;",
                        " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0, 0, 2);"))
        );

        VBox content = new VBox();
        content.setSpacing(8);
        content.setPadding(new Insets(12));
        content.setAlignment(Pos.TOP_CENTER);

        ImageView cover = new ImageView();
        cover.setFitWidth(160);
        cover.setFitHeight(160);
        cover.setPreserveRatio(true);
        cover.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 0, 1);");

        try {
            if (track.getCoverUrl() != null && !track.getCoverUrl().trim().isEmpty()) {

                cover.setImage(new Image(track.getCoverUrl(), 160, 160, true, true, true));
            } else {
                cover.setImage(new Image("https://via.placeholder.com/160x160/2d2d2d/888888?text=No+Cover", true));
            }
        } catch (Exception e) {
            System.err.println("Не удалось загрузить обложку: " + track.getCoverUrl());
            cover.setImage(new Image("https://via.placeholder.com/160x160/444444/b388ff?text=Error", true));
            cover.setOpacity(0.8);
        }

        Text titleText = new Text(track.getTitle());
        titleText.setWrappingWidth(160);
        titleText.setStyle("-fx-font-size: 14px; -fx-fill: #ffffff; -fx-font-weight: bold;");

        Text artistText = new Text(track.getArtist());
        artistText.setWrappingWidth(160);
        artistText.setStyle("-fx-font-size: 12px; -fx-fill: #b3b3b3;");


        content.getChildren().addAll(cover, titleText, artistText);
        card.getChildren().add(content);

        card.setOnMouseClicked(event -> openTrackDetail(track));

        return card;
    }


    private void loadAuthors() {
        if (authorsPane == null) return;
        authorsPane.getChildren().clear();
        authorsPane.getChildren().add(new ProgressIndicator());

        authorDAO.getAllAuthors()
                .thenAccept(authors -> {
                    Platform.runLater(() -> {
                        authorsPane.getChildren().clear();
                        int limit = Math.min(authors.size(), 11);

                        for (int i = 0; i < limit; i++) {
                            Author author = authors.get(i);
                            VBox authorCard = createAuthorCard(author.getName(), author.getPhotoUrl());
                            authorsPane.getChildren().add(authorCard);
                        }
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        authorsPane.getChildren().clear();
                        authorsPane.getChildren().add(new Label("Ошибка загрузки авторов."));
                        showError("Не удалось загрузить авторов.");
                    });
                    return null;
                });
    }


    public VBox createAuthorCard(String artist, String coverUrl) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(140);
        card.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-padding: 10; " +
                        "-fx-cursor: hand;"
        );


        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: #252525; -fx-background-radius: 12; -fx-padding: 10; -fx-cursor: hand;")
        );
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: transparent; -fx-padding: 10; -fx-cursor: hand;")
        );

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(120, 120);
        imageContainer.setMaxSize(120, 120);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(false);

        try {
            if (coverUrl != null && !coverUrl.isEmpty()) {
                imageView.setImage(new Image(coverUrl, true));
            } else {
                imageView.setImage(new Image("https://via.placeholder.com/120x120.png?text=No+Image", true));
            }
        } catch (Exception e) {
            imageView.setImage(new Image("https://via.placeholder.com/120x120.png?text=Error", true));
        }

        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(60, 60, 60);
        imageView.setClip(clip);

        imageContainer.getChildren().add(imageView);

        Label nameLabel = new Label(artist);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setWrapText(true);

        card.getChildren().addAll(imageContainer, nameLabel);
        card.setOnMouseClicked(event -> openAuthorCatalog(artist));

        return card;
    }

    public void openAuthorCatalog(String artist) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AuthorCatalog.fxml"));
            VBox authorView = loader.load();
            AuthorCatalogController controller = loader.getController();
            controller.setMainController(this);
            controller.setAuthor(artist);
            rootContainer.setCenter(authorView);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть каталог автора");
        }
    }

    @FXML
    private void openFullCatalog() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("track_catalog.fxml"));
            VBox fullCatalogView = loader.load();
            TrackCatalogController controller = loader.getController();
            controller.setMainController(this);
            controller.loadAllTracks();
            rootContainer.setCenter(fullCatalogView);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть полный каталог треков.");
        }
    }

    @FXML
    private void openFullAuthorsCatalog() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("authors_catalog.fxml"));
            VBox fullAuthorsView = loader.load();
            AuthorsCatalogController controller = loader.getController();
            controller.setMainController(this);
            controller.loadAllAuthors();
            rootContainer.setCenter(fullAuthorsView);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть полный каталог авторов.");
        }
    }

    @FXML
    private void openHistory() {

        if (currentUserId <= 0) {
            showError("Для просмотра истории необходимо войти в систему.");
            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("history_view.fxml"));
            VBox historyView = loader.load();
            HistoryController controller = loader.getController();
            controller.setMainController(this);
            controller.loadHistory(currentUserId);
            rootContainer.setCenter(historyView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть историю прослушиваний.");
        }
    }


    public void refreshTracks() {
        System.out.println("Запрос на обновление треков");

        if (musicCardsPane == null) return;

        musicCardsPane.getChildren().clear();
        musicCardsPane.getChildren().add(new ProgressIndicator());

        trackDAO.getAllTracks()
                .thenAccept(newTracks -> Platform.runLater(() -> {
                    renderMainTracks(newTracks);
                    System.out.println("Главная обновлена: " + newTracks.size() + " треков в БД.");
                }))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        musicCardsPane.getChildren().clear();
                        musicCardsPane.getChildren().add(new Label("Ошибка загрузки треков"));
                    });
                    return null;
                });
    }

    public FlowPane getAuthorsPane() {
        return authorsPane;
    }

    public void refreshRadios() {

        if (radioCardsPane == null) {
            System.err.println("radioCardsPane не инициализирован");
            return;
        }

        radioCardsPane.getChildren().clear();
        radioCardsPane.getChildren().add(new ProgressIndicator());

        radioDAO.getAllStations()
                .thenAccept(stations -> {
                    Platform.runLater(() -> {
                        radioCardsPane.getChildren().clear();
                        for (RadioStation station : stations) {
                            radioCardsPane.getChildren().add(createRadioCard(station));
                        }
                        System.out.println("Радио обновлено: " + stations.size());
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        radioCardsPane.getChildren().clear();
                        radioCardsPane.getChildren().add(new Label("Ошибка загрузки радио"));
                    });
                    return null;
                });


    }

    private static final int MAIN_PAGE_LIMIT = 9;

    public void renderMainTracks(List<Track> tracks) {
        if (musicCardsPane == null)
            return;

        musicCardsPane.getChildren().clear();

        int limit = Math.min(tracks.size(), MAIN_PAGE_LIMIT);
        for (int i = 0; i < limit; i++) {
            Track track = tracks.get(i);
            musicCardsPane.getChildren().add(createMusicCard(track));
        }
    }

    @FXML
    private ImageView FavoriteImage;

    private void loadfavoriteImage() {
        try {
            Image defaultAvatar = new Image(getClass().getResourceAsStream("/images/favorite.png"));
            FavoriteImage.setImage(defaultAvatar);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки изображения избранное: " + e.getMessage());
        }
    }

    @FXML
    private ImageView AccImage;

    private void loadAccImage() {
        try {
            Image defaultAvatar = new Image(getClass().getResourceAsStream("/images/account.png"));
            AccImage.setImage(defaultAvatar);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки изображения аккаунта " + e.getMessage());
        }
    }


    public void openPlaylistView(Playlist playlist) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("playlist_view.fxml"));
            Parent root = loader.load();

            PlaylistViewController controller = loader.getController();
            controller.setMainController(this);
            controller.setPlaylist(playlist);

            rootContainer.setCenter(root);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Не удалось открыть плейлист: " + e.getMessage());
        }
    }


    @FXML
    private void openPlaylists() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("playlist_view_main.fxml"));
            Parent root = loader.load();
            PlaylistController controller = loader.getController();
            controller.setMainController(this);
            controller.loadUserPlaylists(getCurrentUserId());
            rootContainer.setCenter(root);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка открытия плейлистов: " + e.getMessage());
        }
    }




    private void loadPlaylistImage() {
        try {
            Image playtest = new Image(getClass().getResourceAsStream("/images/library.png"));
            LibraryImage.setImage(playtest);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки изображения аккаунта " + e.getMessage());
        }
    }


    private void loadpowerImage() {
        try {
            Image playtest = new Image(getClass().getResourceAsStream("/images/power.png"));
            LogoutImage.setImage(playtest);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки изображения аккаунта " + e.getMessage());
        }
    }

    @FXML
    public void openFeaturedPlaylists() {


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("featured_playlists.fxml"));
            Parent root = loader.load();
            FeaturedPlaylistsController controller = loader.getController();
            controller.setMainController(this);
            controller.loadFeaturedPlaylists();
            rootContainer.setCenter(root);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка открытия плейлистов");
        }
    }



    private void loadFeaturedAlbums() {
        playlistService.loadFeaturedPlaylists()
                .thenAccept(playlists -> Platform.runLater(() ->
                        PlaylistRenderer.render(
                                featuredPlaylistsPane,
                                playlists,
                                this::openPlaylistView,
                                null
                        )
                ));
    }



    public void loadHistoryCards() {
        if (historyPane == null) return;

        if (currentUserId <= 0) {
            historyPane.getChildren().clear();
            historyPane.getChildren().add(new Label("Войдите в аккаунт, чтобы видеть историю."));
            return;
        }

        historyPane.getChildren().clear();
        historyPane.getChildren().add(new ProgressIndicator());

        trackDAO.getHistoryTracksByUser(currentUserId)
                .thenAccept(tracks -> Platform.runLater(() -> {
                    historyPane.getChildren().clear();
                    if (tracks.isEmpty()) {
                        historyPane.getChildren().add(new Label("История прослушиваний пуста."));
                        return;
                    }

                    int limit = Math.min(tracks.size(), 6);
                    for (int i = 0; i < limit; i++) {
                        historyPane.getChildren().add(createMusicCard(tracks.get(i)));
                    }
                }))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        historyPane.getChildren().clear();
                        historyPane.getChildren().add(new Label("Ошибка загрузки истории."));
                    });
                    return null;
                });
    }

    private void initMiniPlayer() {
        try {
            miniPlayIcon = new ImageView(new Image(getClass().getResource("/images/play.png").toExternalForm()));
            miniPauseIcon = new ImageView(new Image(getClass().getResource("/images/pause.png").toExternalForm()));
            miniPrevIcon = new ImageView(new Image(getClass().getResource("/images/rewind.png").toExternalForm()));
            miniNextIcon = new ImageView(new Image(getClass().getResource("/images/forward.png").toExternalForm()));
            miniFavFillIcon = new ImageView(new Image(getClass().getResource("/images/favoritefill.png").toExternalForm()));
            miniFavUnfillIcon = new ImageView(new Image(getClass().getResource("/images/favoriteunfill.png").toExternalForm()));
            miniLoopIcon = new ImageView(new Image(getClass().getResource("/images/loop.png").toExternalForm()));
            miniLoopActiveIcon = new ImageView(new Image(getClass().getResource("/images/loopactive.png").toExternalForm()));

            miniPlayIcon.setFitWidth(35);
            miniPlayIcon.setFitHeight(35);
            miniPauseIcon.setFitWidth(35);
            miniPauseIcon.setFitHeight(35);
            miniPrevIcon.setFitWidth(22);
            miniPrevIcon.setFitHeight(22);
            miniNextIcon.setFitWidth(22);
            miniNextIcon.setFitHeight(22);
            miniFavFillIcon.setFitWidth(24);
            miniFavFillIcon.setFitHeight(24);
            miniFavUnfillIcon.setFitWidth(24);
            miniFavUnfillIcon.setFitHeight(24);
            miniLoopIcon.setFitWidth(22);
            miniLoopIcon.setFitHeight(20);
            miniLoopActiveIcon.setFitWidth(22);
            miniLoopActiveIcon.setFitHeight(22);


            miniPrevButton.setGraphic(miniPrevIcon);
            miniNextButton.setGraphic(miniNextIcon);
            miniPlayButton.setGraphic(miniPlayIcon);


            miniLoopButton.setGraphic(playerManager.isLooping() ? miniLoopActiveIcon : miniLoopIcon);


            playerManager.loopingProperty().addListener((obs, oldVal, isLooping) -> {
                miniLoopButton.setGraphic(isLooping ? miniLoopActiveIcon : miniLoopIcon);
            });
        } catch (Exception e) {
            System.err.println("Ошибка загрузки иконок мини-плеера: " + e.getMessage());
        }

        playerManager.currentTrackProperty().addListener((obs, oldTrack, newTrack) -> {
            if (newTrack != null) {
                if (!isFullPlayerOpen) {
                    miniPlayerBar.setVisible(true);
                    miniPlayerBar.setManaged(true);
                }

                miniTitleLabel.setText(newTrack.getTitle());
                miniArtistLabel.setText(newTrack.getArtist());

                try {
                    if (newTrack.getCoverUrl() != null && !newTrack.getCoverUrl().isBlank()) {
                        miniCoverImage.setImage(new Image(newTrack.getCoverUrl(), true));
                    }
                } catch (Exception ignored) {
                }

                boolean isRadio = newTrack.isRadio();
                if (miniProgressBar != null) {
                    miniProgressBar.setVisible(!isRadio);
                    miniProgressBar.setManaged(!isRadio);
                }
                if (currentTimeLabel != null) {
                    currentTimeLabel.setVisible(!isRadio);
                    currentTimeLabel.setManaged(!isRadio);
                }
                if (durationLabel != null) {
                    durationLabel.setVisible(!isRadio);
                    durationLabel.setManaged(!isRadio);
                }
                if (miniFavoriteButton != null) {
                    miniFavoriteButton.setVisible(!isRadio);
                    miniFavoriteButton.setManaged(!isRadio);
                }
                if (miniLoopButton != null) {
                    miniLoopButton.setVisible(!isRadio);
                    miniLoopButton.setManaged(!isRadio);
                }

                if (!isRadio && currentUserId > 0) {
                    updateMiniFavoriteUI(newTrack.getId());
                }
            }
        });


        playerManager.playingProperty().addListener((obs, oldVal, isPlaying) -> {
            miniPlayButton.setGraphic(isPlaying ? miniPauseIcon : miniPlayIcon);
        });

        playerManager.progressProperty().addListener((obs, oldVal, progress) -> {
            if (!miniProgressBar.isValueChanging()) {
                miniProgressBar.setValue(progress.doubleValue());
            }
        });
        miniProgressBar.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                playerManager.seek(miniProgressBar.getValue());
            }
        });

        currentTimeLabel.textProperty().bind(playerManager.currentTimeStringProperty());
        durationLabel.textProperty().bind(playerManager.durationStringProperty());
    }

    private void updateMiniFavoriteUI(int trackId) {
        favoriteDAO.isFavorite(currentUserId, trackId).thenAccept(isFav -> {
            Platform.runLater(() -> {
                miniFavoriteButton.setGraphic(isFav ? miniFavFillIcon : miniFavUnfillIcon);
            });
        });
    }

    @FXML
    private void handleMiniFavorite() {
        Track current = playerManager.getCurrentTrack();
        if (current == null || current.isRadio() || currentUserId <= 0) return;

        favoriteDAO.isFavorite(currentUserId, current.getId()).thenAccept(isFav -> {
            CompletableFuture<Boolean> fut = isFav
                    ? favoriteDAO.removeFromFavorites(currentUserId, current.getId())
                    : favoriteDAO.addToFavorites(currentUserId, current.getId());

            fut.thenAccept(success -> {
                if (success) {
                    Platform.runLater(() -> updateMiniFavoriteUI(current.getId()));
                }
            });
        });
    }

    @FXML
    private void handleMiniLoop() {
        playerManager.toggleLoop();
    }

    @FXML
    private void openCurrentTrackDetail() {
        if (playerManager.getCurrentTrack() != null) {
            Track current = playerManager.getCurrentTrack();
            if (current.isRadio()) {
                // Если играет радио, открываем полноэкранный интерфейс радио
                isFullPlayerOpen = true;
                miniPlayerBar.setVisible(false);
                miniPlayerBar.setManaged(false);
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("radio_player.fxml"));
                    Parent playerView = loader.load();
                    RadioPlayerController controller = loader.getController();
                    controller.setMainController(this);
                    rootContainer.setCenter(playerView);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                openTrackDetail(current, playerManager.getCurrentPlaylist());
            }
        }
    }

    @FXML
    private void handleMiniPlayPause() {
        playerManager.togglePlayPause();
    }

    @FXML
    private void handleMiniPrev() {
        playerManager.playPrev();
        loadHistoryCards();
    }

    @FXML
    private void handleMiniNext() {
        playerManager.playNext();
        loadHistoryCards();
    }


    public void refreshAllData() {
        loadInitialData();
        loadFeaturedAlbums();
        loadHistoryCards();

    }

}