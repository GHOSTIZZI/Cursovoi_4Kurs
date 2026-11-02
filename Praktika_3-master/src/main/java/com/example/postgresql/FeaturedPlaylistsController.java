package com.example.postgresql;

import com.example.postgresql.Models.Playlist;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;

public class FeaturedPlaylistsController {

    @FXML
    private FlowPane playlistsPane;

    private final PlaylistService playlistService = new PlaylistService();
    private Form1 mainController;

    public void setMainController(Form1 controller) {
        this.mainController = controller;
    }



    public void loadFeaturedPlaylists() {
        playlistService.loadFeaturedPlaylists()
                .thenAccept(playlists -> Platform.runLater(() ->
                        PlaylistRenderer.render(
                                playlistsPane,
                                playlists,
                                this::openPlaylist,
                                null
                        )
                ));
    }

    private void openPlaylist(Playlist playlist) {
        mainController.openPlaylistView(playlist);
    }


    @FXML
    private void goBack() {
        if (mainController != null) {
            mainController.showMainView();
        }
    }
}