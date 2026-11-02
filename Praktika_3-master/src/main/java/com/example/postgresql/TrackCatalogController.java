 package com.example.postgresql;

import com.example.postgresql.DAO.TrackDAO;
import com.example.postgresql.Models.Author;
import com.example.postgresql.Models.Track;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TrackCatalogController {

    @FXML private FlowPane fullTrackFlowPane;
    @FXML private ComboBox<String> authorComboBox;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private ComboBox<String> yearComboBox;
    @FXML private ComboBox<String> sortComboBox;

    private Form1 mainController;
    private final TrackDAO trackDAO = new TrackDAO();

    private List<Track> allTracks = List.of();
    private List<Track> currentFilteredTracks = List.of();

    public void setMainController(Form1 controller) {
        this.mainController = controller;
    }

    public void loadAllTracks() {
        fullTrackFlowPane.getChildren().clear();
        fullTrackFlowPane.setPrefWrapLength(1820);

        Label loadingLabel = new Label("Загрузка каталога...");
        loadingLabel.setStyle("-fx-text-fill: #ffffff;");
        fullTrackFlowPane.getChildren().add(loadingLabel);

        trackDAO.getAllTracks()
                .thenAccept(tracks -> Platform.runLater(() -> {
                    this.allTracks = tracks;
                    setupFilters();
                    applyFilters();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        fullTrackFlowPane.getChildren().clear();
                        Label errLabel = new Label("Ошибка загрузки каталога: " + ex.getMessage());
                        errLabel.setStyle("-fx-text-fill: #ff5555;");
                        fullTrackFlowPane.getChildren().add(errLabel);
                    });
                    return null;
                });
    }

    private void setupFilters() {
        if (genreComboBox == null || yearComboBox == null || authorComboBox == null || sortComboBox == null) return;

        List<String> authors = allTracks.stream()
                .flatMap(t -> t.getAuthors() != null && !t.getAuthors().isEmpty()
                        ? t.getAuthors().stream().map(Author::getName)
                        : List.of(t.getArtist() != null ? t.getArtist() : "").stream())
                .filter(a -> a != null && !a.trim().isEmpty() && !"Неизвестный автор".equalsIgnoreCase(a))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        authors.add(0, "Все исполнители");

        // 2. Собираем уникальные жанры
        List<String> genres = allTracks.stream()
                .map(Track::getGenre)
                .filter(g -> g != null && !g.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        genres.add(0, "Все жанры");


        List<String> years = allTracks.stream()
                .map(Track::getReleaseYear)
                .filter(y -> y > 0)
                .distinct()
                .sorted((a, b) -> b.compareTo(a))
                .map(String::valueOf)
                .collect(Collectors.toList());
        years.add(0, "Все годы");


        List<String> sortOptions = List.of(
                "По умолчанию",
                "Название (А - Я)",
                "Название (Я - А)",
                "Год (Сначала новые)",
                "Год (Сначала старые)"
        );

        authorComboBox.getItems().setAll(authors);
        authorComboBox.setValue("Все исполнители");

        genreComboBox.getItems().setAll(genres);
        genreComboBox.setValue("Все жанры");
yearComboBox.getItems().setAll(years);
        yearComboBox.setValue("Все годы");

        sortComboBox.getItems().setAll(sortOptions);
        sortComboBox.setValue("По умолчанию");

        // Подписываемся на события изменения выбора
        authorComboBox.setOnAction(e -> applyFilters());
        genreComboBox.setOnAction(e -> applyFilters());
        yearComboBox.setOnAction(e -> applyFilters());
        sortComboBox.setOnAction(e -> applyFilters());
    }

    @FXML
    private void applyFilters() {
        String selectedAuthor = authorComboBox != null ? authorComboBox.getValue() : "Все исполнители";
        String selectedGenre = genreComboBox != null ? genreComboBox.getValue() : "Все жанры";
        String selectedYear = yearComboBox != null ? yearComboBox.getValue() : "Все годы";
        String selectedSort = sortComboBox != null ? sortComboBox.getValue() : "По умолчанию";


        currentFilteredTracks = allTracks.stream()
                .filter(t -> {
                    if ("Все исполнители".equals(selectedAuthor) || selectedAuthor == null) return true;
                    if (t.getArtist() != null && t.getArtist().toLowerCase().contains(selectedAuthor.toLowerCase())) return true;
                    return t.getAuthors() != null && t.getAuthors().stream()
                            .anyMatch(a -> a.getName() != null && a.getName().equalsIgnoreCase(selectedAuthor));
                })
                .filter(t -> "Все жанры".equals(selectedGenre) || selectedGenre == null || (t.getGenre() != null && t.getGenre().equalsIgnoreCase(selectedGenre)))
                .filter(t -> "Все годы".equals(selectedYear) || selectedYear == null ||  (String.valueOf(t.getReleaseYear()).equals(selectedYear)))
                .collect(Collectors.toList());


        if ("Название (А - Я)".equals(selectedSort)) {
            currentFilteredTracks.sort(Comparator.comparing(t -> t.getTitle() != null ? t.getTitle().toLowerCase() : ""));
        } else if ("Название (Я - А)".equals(selectedSort)) {
            currentFilteredTracks.sort((t1, t2) -> {
                String title1 = t1.getTitle() != null ? t1.getTitle().toLowerCase() : "";
                String title2 = t2.getTitle() != null ? t2.getTitle().toLowerCase() : "";
                return title2.compareTo(title1);
            });
        } else if ("Год (Сначала новые)".equals(selectedSort)) {
            currentFilteredTracks.sort(Comparator.comparingInt(Track::getReleaseYear).reversed());
        } else if ("Год (Сначала старые)".equals(selectedSort)) {
            currentFilteredTracks.sort(Comparator.comparingInt(Track::getReleaseYear));
        }

        renderTracks(currentFilteredTracks);
    }

    @FXML
    private void resetFilters() {
        if (authorComboBox != null) authorComboBox.setValue("Все исполнители");
        if (genreComboBox != null) genreComboBox.setValue("Все жанры");
        if (yearComboBox != null) yearComboBox.setValue("Все годы");
        if (sortComboBox != null) sortComboBox.setValue("По умолчанию");
    }

    private void renderTracks(List<Track> tracks) {
        fullTrackFlowPane.getChildren().clear();
        fullTrackFlowPane.setPrefWrapLength(1820);

        if (tracks.isEmpty()) {
            Label emptyLabel = new Label("Треки по заданным критериям не найдены.");
            emptyLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 15px;");
            fullTrackFlowPane.getChildren().add(emptyLabel);
        } else {
            for (Track track : tracks) {
                StackPane card = mainController.createMusicCard(track);
                card.setOnMouseClicked(e -> mainController.openTrackDetail(track, currentFilteredTracks));
                fullTrackFlowPane.getChildren().add(card);
            }
        }
    }


    @FXML
    private void goBack() {
        if (mainController != null) {
            mainController.showMainView();
        }
    }
}