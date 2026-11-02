package com.example.postgresql;

import com.example.postgresql.Models.Playlist;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class PlaylistCardFactory {

    private static final Image DEFAULT_COVER =
            new Image(PlaylistCardFactory.class
                    .getResource("/images/default_playlist.jpg")
                    .toExternalForm());

    public static VBox create(
            Playlist playlist,
            Consumer<Playlist> onOpen,
            Consumer<Playlist> onDelete
    ) {

        VBox card = new VBox(8);
        card.setStyle("""
                    -fx-background-color: #1e1e1e;
                    -fx-background-radius: 10;
                    -fx-padding: 10;
                    -fx-cursor: hand;
                """);
        card.setMaxWidth(160);

        Image cover;
        if (playlist.getCoverUrl() != null &&
                !playlist.getCoverUrl().trim().isEmpty()) {
            cover = new Image(playlist.getCoverUrl(), true);
        } else {
            cover = DEFAULT_COVER;
        }

        ImageView coverView = new ImageView(cover);
        coverView.setFitWidth(140);
        coverView.setFitHeight(140);
        coverView.setPreserveRatio(true);
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().add(coverView);


        if (onDelete != null) {
            Image deleteIcon = new Image(
                    PlaylistCardFactory.class
                            .getResource("/images/delete.png")
                            .toExternalForm()
            );

            ImageView deleteIconView = new ImageView(deleteIcon);
            deleteIconView.setFitWidth(14);
            deleteIconView.setFitHeight(14);

            Button deleteBtn = new Button();
            deleteBtn.setGraphic(deleteIconView);

            deleteBtn.setStyle("""
                -fx-background-color: rgba(0, 0, 0, 0.65);
                -fx-background-radius: 50%;
                -fx-cursor: hand;
                -fx-padding: 7;
            """);


            deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("""
                -fx-background-color: rgba(220, 50, 50, 0.85);
                -fx-background-radius: 50%;
                -fx-cursor: hand;
                -fx-padding: 7;
            """));
            deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("""
                -fx-background-color: rgba(0, 0, 0, 0.65);
                -fx-background-radius: 50%;
                -fx-cursor: hand;
                -fx-padding: 7;
            """));


            deleteBtn.setOnAction(e -> {
                e.consume();
                onDelete.accept(playlist);
            });

            StackPane.setAlignment(deleteBtn, Pos.TOP_RIGHT);
            StackPane.setMargin(deleteBtn, new Insets(8, 8, 0, 0));

            imageContainer.getChildren().add(deleteBtn);
        }

        Label title = new Label(playlist.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label subtitle = new Label("Плейлист");
        subtitle.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");

        card.setOnMouseClicked(e -> onOpen.accept(playlist));
        card.getChildren().addAll(imageContainer, title, subtitle);

        return card;
    }
}