package com.example.postgresql;

import com.example.postgresql.Models.Track;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class TrackCardFactory {

    public static VBox create(
            Track track,
            Consumer<Track> onOpen,
            Consumer<Track> onDelete
    ) {

        VBox card = new VBox();
        card.setSpacing(6);
        card.setStyle(
                "-fx-background-color:#1a1a1a;" +
                        "-fx-padding:10;" +
                        "-fx-background-radius:10;" +
                        "-fx-cursor:hand;"
        );

        ImageView cover = new ImageView();
        cover.setFitWidth(80);
        cover.setFitHeight(80);
        cover.setPreserveRatio(true);

        if (track.getCoverUrl() != null && !track.getCoverUrl().isEmpty()) {
            try {
                cover.setImage(new Image(track.getCoverUrl(), true));
            } catch (Exception ignored) {}
        }

        Label title = new Label(track.getTitle());
        title.setStyle("-fx-text-fill:white; -fx-font-size:14px; -fx-font-weight: bold;");

        Label artist = new Label(track.getArtist());
        artist.setStyle("-fx-text-fill:#b3b3b3; -fx-font-size:12px;");

        VBox info = new VBox(4, title, artist);
        info.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row;

        if (onDelete != null) {
            Image deleteIcon = new Image(
                    TrackCardFactory.class
                            .getResource("/images/delete.png")
                            .toExternalForm()
            );

            ImageView deleteIconView = new ImageView(deleteIcon);
            deleteIconView.setFitWidth(18);
            deleteIconView.setFitHeight(18);

            Button deleteBtn = new Button();
            deleteBtn.setGraphic(deleteIconView);
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 8; -fx-background-radius: 50%;");

            deleteBtn.setOnMouseEntered(ev -> deleteBtn.setStyle("-fx-background-color: rgba(220, 50, 50, 0.2); -fx-cursor: hand; -fx-padding: 8; -fx-background-radius: 50%;"));
            deleteBtn.setOnMouseExited(ev -> deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 8; -fx-background-radius: 50%;"));

            deleteBtn.setOnAction(ev -> {
                ev.consume();
                onDelete.accept(track);
            });

            row = new HBox(15, cover, info, spacer, deleteBtn);
        } else {
            row = new HBox(15, cover, info, spacer);
        }


        row.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().add(row);
        card.setOnMouseClicked(e -> onOpen.accept(track));
        return card;
    }
}