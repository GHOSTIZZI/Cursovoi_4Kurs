package com.example.postgresql;

import com.example.postgresql.DAO.*;
import com.example.postgresql.Models.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdminPanelController {

    @FXML private TextField releaseYearField;
    @FXML private TableColumn<Track, Integer> colTrackYear;

    @FXML private TextField albumTitleField;
    @FXML private TextField albumCoverField;
    @FXML private Label albumStatusLabel;
    @FXML private Label playlistFormModeLabel;
    @FXML private Button savePlaylistButton;
    @FXML private TableView<Playlist> adminPlaylistsTable;
    @FXML private TableColumn<Playlist, Integer> colAdminPlaylistId;
    @FXML private TableColumn<Playlist, String> colAdminPlaylistTitle;

    @FXML private Label managePlaylistHeaderLabel;
    @FXML private ComboBox<Track> tracksToAddComboBox;
    @FXML private TableView<Track> playlistTracksTable;
    @FXML private TableColumn<Track, Integer> colPlTrackId;
    @FXML private TableColumn<Track, String> colPlTrackTitle;
    @FXML private TableColumn<Track, String> colPlTrackArtist;

    private Integer editingPlaylistId = null;
    private Playlist activeManagedPlaylist = null;

    @FXML private Label radioFormModeLabel;
    @FXML private Button saveRadioButton;
    private Integer editingRadioId = null;

    @FXML private TextField titleField;
    @FXML private TextField albumField;
    @FXML private TextField genreField;
    @FXML private TextField coverUrlField;
    @FXML private TextField audioUrlField;
    @FXML private TextArea lyricsField;

    @FXML private ListView<Author> trackAuthorsList;
    @FXML private Label formModeLabel;
    @FXML private Button saveTrackButton;
    @FXML private Label statusLabel;

    @FXML private TableColumn<Track, String> colTrackAlbum;
    @FXML private TableColumn<Track, String> colTrackGenre;
    @FXML private TableColumn<Track, String> colTrackCover;
    @FXML private TableColumn<Track, String> colTrackAudio;

    @FXML private TableView<Track> tracksTable;
    @FXML private TableColumn<Track, Integer> colTrackId;
    @FXML private TableColumn<Track, String> colTrackTitle;
    @FXML private TableColumn<Track, String> colTrackArtist;

    @FXML private TextField authorNameField;
    @FXML private TextField authorPhotoUrlField;
    @FXML private Label authorStatusLabel;
    @FXML private TableView<Author> authorsTable;

    @FXML private TableView<RadioStation> radioTable;
    @FXML private TableColumn<RadioStation, Integer> colRadioId;
    @FXML private TableColumn<RadioStation, String> colRadioName;
    @FXML private TableColumn<RadioStation, String> colRadioStream;
    @FXML private TableColumn<RadioStation, String> colRadioCover;

    @FXML private TextField radioNameField;
    @FXML private TextField radioStreamField;
    @FXML private TextField radioCoverField;
    @FXML private Label radioStatusLabel;

    @FXML private ListView<Track> albumTracksList;
    @FXML private ComboBox<Playlist> existingAlbumsBox;


    private final PlaylistDAO playlistDAO = new PlaylistDAO();
    private final TrackDAO trackDAO = new TrackDAO();

    private final AuthorDAO authorDAO = new AuthorDAO();
    private final RadioDAO radioDAO = new RadioDAO();

    private Form1 mainController;
    private List<User> allUsers;
    private List<Track> allTracks;
    private Integer editingTrackId = null;

    public void setMainController(Form1 controller) {
        this.mainController = controller;
        loadData();
    }

    @FXML
    public void initialize() {
        // Таблица треков
        colTrackId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTrackTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTrackArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        if (colTrackAlbum != null) colTrackAlbum.setCellValueFactory(new PropertyValueFactory<>("album"));
        if (colTrackGenre != null) colTrackGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        if (colTrackCover != null) colTrackCover.setCellValueFactory(new PropertyValueFactory<>("coverUrl"));
        if (colTrackAudio != null) colTrackAudio.setCellValueFactory(new PropertyValueFactory<>("trackUrl"));
        if (colTrackYear != null) colTrackYear.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));


        if (trackAuthorsList != null) {
            trackAuthorsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        }

        if (albumTracksList != null) {
            albumTracksList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        }

        colRadioId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRadioName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRadioStream.setCellValueFactory(new PropertyValueFactory<>("streamUrl"));
        colRadioCover.setCellValueFactory(new PropertyValueFactory<>("coverUrl"));

        // Таблица публичных альбомов в админке
        if (colAdminPlaylistId != null) colAdminPlaylistId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colAdminPlaylistTitle != null) colAdminPlaylistTitle.setCellValueFactory(new PropertyValueFactory<>("title"));

        // Таблица треков внутри выбранного альбома
        if (colPlTrackId != null) colPlTrackId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colPlTrackTitle != null) colPlTrackTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (colPlTrackArtist != null) colPlTrackArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));

        // Слушатель выбора альбома для управления его треками
        if (adminPlaylistsTable != null) {
            adminPlaylistsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    activeManagedPlaylist = newVal;
                    loadTracksForManagedPlaylist(newVal);
                }
            });
        }
    }

    private void loadData() {
        loadTracksTable();
        loadAuthorsTable();
        loadRadioTable();
        loadTracksForAlbumCreation();
        loadAlbumsBox();
        loadAuthorsForTrackCreation();
        loadAdminPlaylistsTable();
        loadTracksIntoComboBox();
    }

    private void loadAuthorsForTrackCreation() {
        authorDAO.getAllAuthors().thenAccept(authors -> Platform.runLater(() -> {
            if (trackAuthorsList != null) {
                trackAuthorsList.getItems().setAll(authors);
            }
        }));
    }

    private void loadTracksIntoComboBox() {
        trackDAO.getAllTracks().thenAccept(tracks -> Platform.runLater(() -> {
            if (tracksToAddComboBox != null) {
                tracksToAddComboBox.getItems().setAll(tracks);
            }
        }));
    }


    // ЛОГИКА ТРЕКОВ (СОЗДАНИЕ, РЕДАКТИРОВАНИЕ, УДАЛЕНИЕ)


    private void loadTracksTable() {
        tracksTable.getItems().clear();
        tracksTable.setPlaceholder(new ProgressIndicator());
        trackDAO.getAllTracks()
                .thenAccept(tracks -> {
                    allTracks = tracks;
                    Platform.runLater(() -> {
                        tracksTable.setPlaceholder(new Label("Нет данных о треках."));
                        tracksTable.getItems().addAll(tracks);
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> tracksTable.setPlaceholder(new Label("Ошибка загрузки треков")));
                    return null;
                });
    }

    @FXML
    private void saveTrack() {
        if (titleField.getText().isEmpty() || audioUrlField.getText().isEmpty()) {
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            statusLabel.setText("Название и ссылка на аудио обязательны.");
            return;
        }

        List<Author> selectedAuthors = trackAuthorsList.getSelectionModel().getSelectedItems();
        if (selectedAuthors.isEmpty()) {
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            statusLabel.setText("Выберите хотя бы одного автора трека.");
            return;
        }

        Track track = new Track();
        track.setTitle(titleField.getText().trim());
        track.setCoverUrl(coverUrlField.getText() != null ? coverUrlField.getText().trim() : "");
        track.setTrackUrl(audioUrlField.getText().trim());
        if (albumField != null) track.setAlbum(albumField.getText().trim());
        if (genreField != null) track.setGenre(genreField.getText().trim());
        int year = 2026;
        if (releaseYearField != null && releaseYearField.getText() != null && !releaseYearField.getText().trim().isEmpty()) {
            try {
                year = Integer.parseInt(releaseYearField.getText().trim());
            } catch (NumberFormatException ignored) {}
        }
        track.setReleaseYear(year);
        if (lyricsField != null) track.setLyrics(lyricsField.getText());


        if (editingTrackId == null) {
            // --- СОЗДАНИЕ НОВОГО ТРЕКА ---
            trackDAO.addTrack(track)
                    .thenCompose(success -> {
                        if (!success) return CompletableFuture.completedFuture(false);
                        return trackDAO.getAllTracks().thenCompose(tracks -> {
                            Track created = tracks.stream()
                                    .filter(t -> t.getAudioUrl() != null && t.getAudioUrl().equals(track.getTrackUrl()))
                                    .reduce((first, second) -> second)
                                    .orElse(null);

                            if (created == null) return CompletableFuture.completedFuture(false);

                            CompletableFuture<Boolean> chain = CompletableFuture.completedFuture(true);
                            for (Author author : selectedAuthors) {
                                chain = chain.thenCompose(r -> trackDAO.addAuthorToTrack(created.getId(), author.getId()));
                            }
                            return chain;
                        });
                    })
                    .thenAccept(success -> Platform.runLater(() -> {
                        if (success) {
                            statusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                            statusLabel.setText("Трек успешно создан и привязан к авторам!");
                            clearTrackFields();
                            loadTracksTable();
                            loadTracksIntoComboBox();
                            if (mainController != null) mainController.refreshTracks();
                        } else {
                            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
                            statusLabel.setText("Ошибка при сохранении трека.");
                        }
                    }));
        } else {
            // --- РЕДАКТИРОВАНИЕ СУЩЕСТВУЮЩЕГО ТРЕКА ---
            track.setId(editingTrackId);
            trackDAO.updateTrack(track)
                    .thenCompose(success -> {
                        if (!success) return CompletableFuture.completedFuture(false);
                        return trackDAO.removeAllAuthorsForTrack(editingTrackId).thenCompose(r -> {
                            CompletableFuture<Boolean> chain = CompletableFuture.completedFuture(true);
                            for (Author author : selectedAuthors) {
                                chain = chain.thenCompose(res -> trackDAO.addAuthorToTrack(editingTrackId, author.getId()));
                            }
                            return chain;
                        });
                    })
                    .thenAccept(success -> Platform.runLater(() -> {
                        if (success) {
                            statusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                            statusLabel.setText("Трек успешно обновлен!");
                            clearTrackFields();
                            loadTracksTable();
                            loadTracksIntoComboBox();
                            if (mainController != null) mainController.refreshTracks();
                        } else {
                            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
                            statusLabel.setText("Ошибка при обновлении трека.");
                        }
                    }));
        }
    }

    @FXML
    private void prepareTrackForEdit() {
        Track selected = tracksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (mainController != null) mainController.showError("Выберите трек в таблице для редактирования.");
            return;
        }

        editingTrackId = selected.getId();
        if (formModeLabel != null) formModeLabel.setText("Режим: Редактирование трека ID: " + editingTrackId);
        if (saveTrackButton != null) saveTrackButton.setText("Сохранить изменения");

        titleField.setText(selected.getTitle());
        if (albumField != null) albumField.setText(selected.getAlbum() != null ? selected.getAlbum() : "");
        if (genreField != null) genreField.setText(selected.getGenre() != null ? selected.getGenre() : "");
        if (coverUrlField != null) coverUrlField.setText(selected.getCoverUrl() != null ? selected.getCoverUrl() : "");
        if (audioUrlField != null) audioUrlField.setText(selected.getTrackUrl() != null ? selected.getTrackUrl() : "");
        if (lyricsField != null) lyricsField.setText(selected.getLyrics() != null ? selected.getLyrics() : "");

        trackAuthorsList.getSelectionModel().clearSelection();
        List<Author> trackAuthors = selected.getAuthors();
        if (trackAuthors != null && !trackAuthors.isEmpty()) {
            for (Author author : trackAuthors) {
                for (Author item : trackAuthorsList.getItems()) {
                    if (item.getId() == author.getId()) {
                        trackAuthorsList.getSelectionModel().select(item);
                        break;
                    }
                }
            }
        }

        statusLabel.setText("");
    }

    @FXML
    private void deleteSelectedTrack() {
        Track selected = tracksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mainController.showError("Выберите трек для удаления.");
            return;
        }

        trackDAO.deleteTrack(selected.getId())
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        mainController.showAlert("Успех", "Трек успешно удален.");
                        loadTracksTable();
                        loadTracksIntoComboBox();
                        if (mainController != null) mainController.refreshTracks();
                    } else {
                        mainController.showError("Не удалось удалить трек.");
                    }
                }));
    }

    @FXML
    private void clearTrackFields() {
        editingTrackId = null;
        if (formModeLabel != null) formModeLabel.setText("Режим: Добавление нового трека");
        if (saveTrackButton != null) saveTrackButton.setText("Добавить трек");

        titleField.clear();
        if (albumField != null) albumField.clear();
        if (genreField != null) genreField.clear();
        if (coverUrlField != null) coverUrlField.clear();
        if (audioUrlField != null) audioUrlField.clear();
        if (lyricsField != null) lyricsField.clear();
        if (trackAuthorsList != null) trackAuthorsList.getSelectionModel().clearSelection();
        if (statusLabel != null) statusLabel.setText("");
    }


    // ОСТАЛЬНЫЕ МЕТОДЫ ( испольитнли, радио, плейлисты)





    private void loadAuthorsTable() {
        authorsTable.getItems().clear();
        authorDAO.getAllAuthors().thenAccept(authors -> Platform.runLater(() -> authorsTable.getItems().addAll(authors)));
    }

    @FXML
    public void goBack() {
        if (mainController != null) mainController.showMainView();
    }

    @FXML
    private void addAuthor() {
        String name = authorNameField.getText();
        String photo = authorPhotoUrlField.getText();
        if (name.isEmpty()) { authorStatusLabel.setText("Имя обязательно."); return; }

        authorDAO.addAuthor(name, photo).thenAccept(success -> Platform.runLater(() -> {
            if (success) {
                authorStatusLabel.setText("Автор добавлен.");
                authorNameField.clear();
                authorPhotoUrlField.clear();
                loadAuthorsTable();
                loadAuthorsForTrackCreation();
                if (mainController != null) loadAuthorsIntoPane();
            }
        }));
    }

    @FXML
    private void deleteSelectedAuthor() {
        Author selected = authorsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { mainController.showError("Выберите автора."); return; }

        authorDAO.deleteAuthor(selected.getId()).thenAccept(success -> Platform.runLater(() -> {
            if (success) {
                loadAuthorsTable();
                loadAuthorsForTrackCreation();
                if (mainController != null) loadAuthorsIntoPane();
            }
        }));
    }

    public void loadAuthorsIntoPane() {
        authorDAO.getAllAuthors().thenAccept(authors -> Platform.runLater(() -> {
            if (mainController.getAuthorsPane() != null) {
                mainController.getAuthorsPane().getChildren().clear();
                for (Author author : authors) {
                    Node authorCard = mainController.createAuthorCard(author.getName(), author.getPhotoUrl());
                    mainController.getAuthorsPane().getChildren().add(authorCard);
                }
            }
        }));
    }

    private void loadRadioTable() {
        radioTable.getItems().clear();
        radioDAO.getAllStations().thenAccept(stations -> Platform.runLater(() -> radioTable.getItems().addAll(stations)));
    }

    @FXML
    private void deleteSelectedRadio() {
        RadioStation selected = radioTable.getSelectionModel().getSelectedItem();
        if (selected == null) { mainController.showError("Выберите радиостанцию."); return; }

        radioDAO.deleteStation(selected.getId()).thenAccept(success -> Platform.runLater(() -> {
            loadRadioTable();
            if (mainController != null) mainController.refreshRadios();
        }));
    }

    private void loadAlbumsBox() {
        playlistDAO.getFeaturedPlaylists().thenAccept(playlists -> Platform.runLater(() -> {
            if (existingAlbumsBox != null) existingAlbumsBox.getItems().setAll(playlists);
        }));
    }

    private void loadTracksForAlbumCreation() {
        trackDAO.getAllTracks().thenAccept(tracks -> Platform.runLater(() -> {
            if (albumTracksList != null) albumTracksList.getItems().setAll(tracks);
        }));
    }

    @FXML
    private void saveRadioStation() {
        String name = radioNameField.getText().trim();
        String stream = radioStreamField.getText().trim();
        String cover = radioCoverField.getText() != null ? radioCoverField.getText().trim() : "";

        if (name.isEmpty() || stream.isEmpty()) {
            radioStatusLabel.setTextFill(javafx.scene.paint.Color.RED);
            radioStatusLabel.setText("Название и ссылка на поток обязательны.");
            return;
        }

        if (editingRadioId == null) {
            radioDAO.addStation(name, stream, cover)
                    .thenAccept(success -> Platform.runLater(() -> {
                        if (success) {
                            radioStatusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                            radioStatusLabel.setText("Радиостанция успешно добавлена!");
                            clearRadioFields();
                            loadRadioTable();
                            if (mainController != null) mainController.refreshRadios();
                        } else {
                            radioStatusLabel.setTextFill(javafx.scene.paint.Color.RED);
                            radioStatusLabel.setText("Ошибка добавления радиостанции.");
                        }
                    }));
        } else {
            RadioStation station = new RadioStation();
            station.setId(editingRadioId);
            station.setName(name);
            station.setStreamUrl(stream);
            station.setCoverUrl(cover);

            radioDAO.updateStation(station)
                    .thenAccept(success -> Platform.runLater(() -> {
                        if (success) {
                            radioStatusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                            radioStatusLabel.setText("Радиостанция успешно обновлена!");
                            clearRadioFields();
                            loadRadioTable();
                            if (mainController != null) mainController.refreshRadios();
                        } else {
                            radioStatusLabel.setTextFill(javafx.scene.paint.Color.RED);
                            radioStatusLabel.setText("Ошибка обновления радиостанции.");
                        }
                    }));
        }
    }

    @FXML
    private void prepareRadioForEdit() {
        RadioStation selected = radioTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (mainController != null) mainController.showError("Выберите радиостанцию в таблице для редактирования.");
            return;
        }

        editingRadioId = selected.getId();
        if (radioFormModeLabel != null) radioFormModeLabel.setText("Режим: Редактирование ID: " + editingRadioId);
        if (saveRadioButton != null) saveRadioButton.setText("Сохранить изменения");

        radioNameField.setText(selected.getName());
        radioStreamField.setText(selected.getStreamUrl());
        radioCoverField.setText(selected.getCoverUrl() != null ? selected.getCoverUrl() : "");
        if (radioStatusLabel != null) radioStatusLabel.setText("");
    }

    @FXML
    private void clearRadioFields() {
        editingRadioId = null;
        if (radioFormModeLabel != null) radioFormModeLabel.setText("Режим: Добавление радиостанции");
        if (saveRadioButton != null) saveRadioButton.setText("Сохранить радио");

        radioNameField.clear();
        radioStreamField.clear();
        radioCoverField.clear();
        if (radioStatusLabel != null) radioStatusLabel.setText("");
    }


    // ПУБЛИЧНЫЕ ПЛЕЙЛИСТЫ


    private void loadAdminPlaylistsTable() {
        if (adminPlaylistsTable == null) return;
        adminPlaylistsTable.getItems().clear();
        adminPlaylistsTable.setPlaceholder(new ProgressIndicator());

        playlistDAO.getFeaturedPlaylists()
                .thenAccept(playlists -> Platform.runLater(() -> {
                    adminPlaylistsTable.setPlaceholder(new Label("Нет публичных альбомов."));
                    adminPlaylistsTable.getItems().setAll(playlists);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> adminPlaylistsTable.setPlaceholder(new Label("Ошибка загрузки альбомов")));
                    return null;
                });
    }

    @FXML
    private void saveAdminPlaylist() {
        String title = albumTitleField.getText().trim();
        String cover = albumCoverField.getText() != null ? albumCoverField.getText().trim() : "";

        if (title.isEmpty()) {
            albumStatusLabel.setTextFill(javafx.scene.paint.Color.RED);
            albumStatusLabel.setText("Название альбома обязательно.");
            return;
        }

        if (editingPlaylistId == null) {
            playlistDAO.createPlaylist(title, cover, 1, true)
                    .thenAccept(success -> Platform.runLater(() -> {
                        if (success) {
                            albumStatusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                            albumStatusLabel.setText("Альбом успешно создан!");
                            clearPlaylistFields();
                            loadAdminPlaylistsTable();
                            loadAlbumsBox();
                            if (mainController != null) mainController.openFeaturedPlaylists();
                        } else {
                            albumStatusLabel.setTextFill(javafx.scene.paint.Color.RED);
                            albumStatusLabel.setText("Ошибка при создании альбома.");
                        }
                    }));
        } else {
            playlistDAO.updatePlaylist(editingPlaylistId, title, cover)
                    .thenAccept(success -> Platform.runLater(() -> {
                        if (success) {
                            albumStatusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                            albumStatusLabel.setText("Альбом успешно обновлен!");
                            clearPlaylistFields();
                            loadAdminPlaylistsTable();
                            loadAlbumsBox();
                            if (mainController != null) mainController.openFeaturedPlaylists();
                        } else {
                            albumStatusLabel.setTextFill(javafx.scene.paint.Color.RED);
                            albumStatusLabel.setText("Ошибка при обновлении альбома.");
                        }
                    }));
        }
    }

    @FXML
    private void preparePlaylistForEdit() {
        Playlist selected = adminPlaylistsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (mainController != null) mainController.showError("Выберите альбом в таблице для редактирования.");
            return;
        }

        editingPlaylistId = selected.getId();
        if (playlistFormModeLabel != null) playlistFormModeLabel.setText("Редактирование альбома ID: " + editingPlaylistId);
        if (savePlaylistButton != null) savePlaylistButton.setText("Сохранить изменения");

        albumTitleField.setText(selected.getTitle());
        albumCoverField.setText(selected.getCoverUrl() != null ? selected.getCoverUrl() : "");
        if (albumStatusLabel != null) albumStatusLabel.setText("");
    }

    @FXML
    private void deleteSelectedAdminPlaylist() {
        Playlist selected = adminPlaylistsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (mainController != null) mainController.showError("Выберите альбом для удаления.");
            return;
        }

        playlistDAO.deletePlaylist(selected.getId())
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        if (mainController != null) mainController.showAlert("Успех", "Альбом успешно удален.");
                        if (activeManagedPlaylist != null && activeManagedPlaylist.getId() == selected.getId()) {
                            activeManagedPlaylist = null;
                            playlistTracksTable.getItems().clear();
                            managePlaylistHeaderLabel.setText("Управление треками в альбоме: (выберите альбом слева)");
                        }
                        clearPlaylistFields();
                        loadAdminPlaylistsTable();
                        loadAlbumsBox();
                        if (mainController != null) mainController.openFeaturedPlaylists();
                    } else {
                        if (mainController != null) mainController.showError("Не удалось удалить альбом.");
                    }
                }));
    }

    @FXML
    private void clearPlaylistFields() {
        editingPlaylistId = null;
        if (playlistFormModeLabel != null) playlistFormModeLabel.setText("Создание нового альбома");
        if (savePlaylistButton != null) savePlaylistButton.setText("Сохранить альбом");

        albumTitleField.clear();
        albumCoverField.clear();
        if (albumStatusLabel != null) albumStatusLabel.setText("");
    }

    private void loadTracksForManagedPlaylist(Playlist playlist) {
        managePlaylistHeaderLabel.setText("Треки в альбоме: " + playlist.getTitle());
        playlistTracksTable.getItems().clear();
        playlistTracksTable.setPlaceholder(new ProgressIndicator());

        playlistDAO.getTracksByPlaylistId(playlist.getId())
                .thenAccept(tracks -> Platform.runLater(() -> {
                    playlistTracksTable.setPlaceholder(new Label("В этом альбоме пока нет треков."));
                    playlistTracksTable.getItems().setAll(tracks);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> playlistTracksTable.setPlaceholder(new Label("Ошибка загрузки треков альбома")));
                    return null;
                });
    }

    @FXML
    private void addTrackToManagedPlaylist() {
        if (activeManagedPlaylist == null) {
            if (mainController != null) mainController.showError("Сначала выберите альбом слева в таблице.");
            return;
        }

        Track trackToAdd = tracksToAddComboBox.getValue();
        if (trackToAdd == null) {
            if (mainController != null) mainController.showError("Выберите трек из выпадающего списка.");
            return;
        }

        playlistDAO.addTrackToPlaylist(activeManagedPlaylist.getId(), trackToAdd.getId())
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        loadTracksForManagedPlaylist(activeManagedPlaylist);
                    } else {
                        if (mainController != null) mainController.showError("Не удалось добавить трек (возможно, он уже есть в альбоме).");
                    }
                }));
    }

    @FXML
    private void removeTrackFromManagedPlaylist() {
        if (activeManagedPlaylist == null) {
            if (mainController != null) mainController.showError("Альбом не выбран.");
            return;
        }

        Track trackToRemove = playlistTracksTable.getSelectionModel().getSelectedItem();
        if (trackToRemove == null) {
            if (mainController != null) mainController.showError("Выберите трек в таблице альбома для удаления.");
            return;
        }

        playlistDAO.removeTrackFromPlaylist(activeManagedPlaylist.getId(), trackToRemove.getId())
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        loadTracksForManagedPlaylist(activeManagedPlaylist);
                    } else {
                        if (mainController != null) mainController.showError("Не удалось удалить трек из альбома.");
                    }
                }));
    }
}