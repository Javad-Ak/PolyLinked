package org.aut.polylinked_client.view;

import com.jfoenix.controls.JFXSlider;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.aut.polylinked_client.SceneManager;
import org.aut.polylinked_client.utils.DataAccess;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.HashMap;

public class MediaWrapper extends BorderPane {
    private static final HashMap<String, MediaWrapper> mediaWrappers = new HashMap<>();
    private String id;
    private MediaPlayer mediaPlayer;
    Button resetButton;
    Button playButton;
    private ImageView imageView;

    public static MediaWrapper getMediaViewer(File file, double relativeWidth) {
        if (relativeWidth <= 0 || relativeWidth >= 1) relativeWidth = 0.45;

        if (mediaWrappers.containsKey(file.getName()))
            return mediaWrappers.get(file.getName());
        else
            return new MediaWrapper(file, relativeWidth);
    }

    private MediaWrapper(File file, double relativeWidth) {
        switch (DataAccess.getFileType(file)) {
            case DataAccess.FileType.IMAGE -> imageViewer(file, relativeWidth);
            case DataAccess.FileType.VIDEO -> videoViewer(file, relativeWidth);
            case DataAccess.FileType.AUDIO -> audioViewer(file);
            default -> setCenter(null);
        }

        if (file != null) id = file.getName();
        mediaWrappers.put(file.getName(), this);
    }

    private void imageViewer(File file, double relativeWidth) {
        imageView = new ImageView(new Image(file.toURI().toString()));
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        imageView.fitWidthProperty().bind(SceneManager.getWidthProperty().multiply(relativeWidth));
        setCenter(imageView);
    }

    private void videoViewer(File file, double relativeWidth) {
        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);

        mediaView.setPreserveRatio(true);
        mediaView.setSmooth(true);
        mediaView.fitWidthProperty().bind(SceneManager.getWidthProperty().multiply(relativeWidth));

        HBox hBox = createControlBar(mediaPlayer);

        setCenter(mediaView);
        setBottom(hBox);
    }

    private void audioViewer(File file) {
        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        HBox hBox = createControlBar(mediaPlayer);
        setCenter(hBox);
    }

    private HBox createControlBar(MediaPlayer mediaPlayer) {
        JFXSlider timeSlider = new JFXSlider();
        timeSlider.setValue(0);
        Label label = new Label("time");

        FontIcon playIcon = new FontIcon("mdi-play");
        playIcon.setId("icon");
        FontIcon pauseIcon = new FontIcon("mdi-pause");
        pauseIcon.setId("icon");
        FontIcon refreshIcon = new FontIcon("mdi-refresh");
        refreshIcon.setId("icon");

        playButton = new Button();
        playButton.setGraphic(playIcon);
        resetButton = new Button();
        resetButton.setGraphic(refreshIcon);

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!timeSlider.isValueChanging()) {
                timeSlider.setValue(newTime.toSeconds());
            }
        });
        timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (playButton.getGraphic().equals(playIcon)) {
                timeSlider.setValue(0);
            } else if (timeSlider.isValueChanging()) {
                mediaPlayer.seek(Duration.seconds(newVal.doubleValue()));
            }
        });

        playButton.setOnAction(event -> {
            MediaWrapper.resetOthers(id);
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playButton.setGraphic(playIcon);
            } else {
                mediaPlayer.play();
                playButton.setGraphic(pauseIcon);
            }
        });

        resetButton.setOnAction(event -> {
            mediaPlayer.seek(Duration.seconds(0));
            mediaPlayer.stop();
            playButton.setGraphic(playIcon);
            timeSlider.setValue(0);
        });

        mediaPlayer.setOnReady(() -> {
            int fullTime = (int) mediaPlayer.getMedia().getDuration().toSeconds();
            int seconds = fullTime % 60;
            int minutes = fullTime / 60;

            timeSlider.setMax(seconds);
            label.setText(minutes + ":" + seconds);
        });
        mediaPlayer.setOnEndOfMedia(() -> {
            resetButton.fire();
        });

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(playButton, resetButton, timeSlider, label);

        hBox.setSpacing(4);
        hBox.setPadding(new Insets(0, 4, 0, 4));
        hBox.setStyle("-fx-background-color: rgba(160, 160, 160, 0.4);");
        return hBox;
    }

    private static void resetOthers(String id) {
        mediaWrappers.forEach((fileName, mediaViewer) -> {
            if (!fileName.equals(id)) {
                if (mediaViewer.mediaPlayer != null && mediaViewer.resetButton != null) mediaViewer.resetButton.fire();
            }
        });
    }

    public static void clearMedias() {
        mediaWrappers.forEach((fileName, mediaWrapper) -> {
            if (mediaWrapper.mediaPlayer != null && mediaWrapper.resetButton != null) mediaWrapper.resetButton.fire();
        });
        mediaWrappers.clear();
    }
}