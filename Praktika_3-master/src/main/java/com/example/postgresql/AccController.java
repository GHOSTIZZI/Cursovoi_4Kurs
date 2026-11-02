package com.example.postgresql;

import com.example.postgresql.DAO.TrackDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class AccController {

    @FXML private Label loginLabel;
    @FXML private ImageView avatarImage;
    @FXML private ImageView logoutIcon;

    @FXML private Label totalPlaysLabel;
    @FXML private Label topArtistLabel;
    @FXML private Label favoriteGenreLabel;
    @FXML private VBox topTracksContainer;

    private Form1 mainController;
    private final TrackDAO trackDAO = new TrackDAO();

    @FXML
    public void initialize() {
        try {
            Image image = new Image(getClass().getResourceAsStream("/images/logout.png"));
            logoutIcon.setImage(image);
        } catch (Exception ignored) {}
    }

    public void setMainController(Form1 controller) {
        this.mainController = controller;
        loadAccountInfo();
        loadUserStatistics();
    }

    private void loadAccountInfo() {
        loadAvatarImage();
        if (mainController != null) {
            String login = mainController.getCurrentUserLogin();
            loginLabel.setText(login != null ? login : "Гость");
        }
    }

    private void loadAvatarImage() {
        try {
            Image defaultAvatar = new Image(getClass().getResourceAsStream("/images/default_avatar.png"));
            avatarImage.setImage(defaultAvatar);
        } catch (Exception e) {
            avatarImage.setImage(new Image("https://via.placeholder.com/100x100/333333/ffffff?text=User"));
        }
    }

    private void loadUserStatistics() {
        if (mainController == null || mainController.getCurrentUserId() <= 0) return;

        int userId = mainController.getCurrentUserId();
        trackDAO.getUserStats(userId).thenAccept(stats -> Platform.runLater(() -> {
            totalPlaysLabel.setText(String.valueOf(stats.getTotalPlays()));
            topArtistLabel.setText(stats.getTopArtist());
            favoriteGenreLabel.setText(stats.getFavoriteGenre());

            // Заполняем список Топ-5 треков
            topTracksContainer.getChildren().clear();
            List<String> topTracks = stats.getTopTracks();

            if (topTracks == null || topTracks.isEmpty() || (topTracks.size() == 1 && topTracks.get(0).equals("Нет данных"))) {
                Label emptyLbl = new Label("История прослушиваний пуста");
                emptyLbl.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
                topTracksContainer.getChildren().add(emptyLbl);
            } else {
                int rank = 1;
                for (String trackInfo : topTracks) {
                    Label trackLbl = new Label(rank + ". " + trackInfo);
                    trackLbl.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 13px;");
                    topTracksContainer.getChildren().add(trackLbl);
                    rank++;
                }
            }
        }));
    }

    @FXML
    private void goBack() {
        if (mainController != null) {
            mainController.showMainView();
        }
    }

    @FXML
    private void handleLogout() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) avatarImage.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}