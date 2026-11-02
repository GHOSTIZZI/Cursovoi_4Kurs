package com.example.postgresql;

import com.example.postgresql.DAO.TrackDAO;
import com.example.postgresql.Models.Track;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;


public class HistoryController {

    @FXML private FlowPane historyFlowPane;

    private Form1 mainController;
    private final TrackDAO trackDAO = new TrackDAO();

    public void setMainController(Form1 controller) {
        this.mainController = controller;
    }

    public void loadHistory(long userId) {
        historyFlowPane.getChildren().clear();
        historyFlowPane.setPrefWrapLength(1820);

        Label loadingLabel = new Label("Загрузка истории прослушиваний...");
        loadingLabel.setStyle("-fx-text-fill: #ffffff;");
        historyFlowPane.getChildren().add(loadingLabel);


        trackDAO.getHistoryTracksByUser(userId)
                .thenAccept(tracks -> Platform.runLater(() -> {
                    historyFlowPane.getChildren().clear();

                    if (tracks == null || tracks.isEmpty()) {
                        Label emptyLabel = new Label("История прослушиваний пока пуста.");
                        emptyLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 15px;");
                        historyFlowPane.getChildren().add(emptyLabel);
                    } else {
                        for (Track track : tracks) {
                            StackPane card = mainController.createMusicCard(track);
                            card.setOnMouseClicked(e -> mainController.openTrackDetail(track, tracks));
                            historyFlowPane.getChildren().add(card);
                        }
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        historyFlowPane.getChildren().clear();
                        Label errLabel = new Label("Ошибка загрузки истории: " + ex.getMessage());
                        errLabel.setStyle("-fx-text-fill: #ff5555;");
                        historyFlowPane.getChildren().add(errLabel);
                    });
                    return null;
                });
    }

    @FXML
    private void goBack() {
        if (mainController != null) {
            mainController.showMainView();
        }
    }
}