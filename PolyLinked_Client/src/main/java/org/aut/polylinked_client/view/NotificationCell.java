package org.aut.polylinked_client.view;

import io.github.gleidson28.AvatarType;
import io.github.gleidson28.GNAvatarView;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.aut.polylinked_client.PolyLinked;
import org.aut.polylinked_client.SceneManager;
import org.aut.polylinked_client.model.Connect;
import org.aut.polylinked_client.model.User;
import org.aut.polylinked_client.utils.DataAccess;
import org.aut.polylinked_client.utils.JsonHandler;
import org.aut.polylinked_client.utils.RequestBuilder;
import org.aut.polylinked_client.utils.exceptions.NotAcceptableException;
import org.aut.polylinked_client.utils.exceptions.UnauthorizedException;

import java.io.File;
import java.util.Objects;

public class NotificationCell extends ListCell<User> {
    BorderPane root;
    GNAvatarView avatar;
    Button button;
    Label label;

    public NotificationCell() {
        root = new BorderPane();
        button = new Button("Accept Connection");
        label = new Label("Notification");

        avatar = new GNAvatarView();
        avatar.setType(AvatarType.CIRCLE);
        avatar.setImage(new Image(Objects.requireNonNull(PolyLinked.class.getResourceAsStream("images/avatar.png"))));
        avatar.setPrefWidth(45);
        avatar.setPrefHeight(45);

        HBox leftBox = new HBox();
        leftBox.getChildren().addAll(avatar, label);
        leftBox.setPrefWidth(HBox.USE_COMPUTED_SIZE);
        leftBox.setPrefHeight(HBox.USE_COMPUTED_SIZE);
        leftBox.setSpacing(12);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        HBox rightBox = new HBox();
        rightBox.getChildren().addAll(button);
        rightBox.setPrefWidth(HBox.USE_COMPUTED_SIZE);
        rightBox.setPrefHeight(HBox.USE_COMPUTED_SIZE);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        root.setBackground(Background.EMPTY);
        root.setCenter(leftBox);
        root.setRight(rightBox);
    }

    @Override
    protected void updateItem(User item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
        } else {
            label.setText(item.getFirstName() + " " + item.getLastName() + " requests to connect.");
            File file = DataAccess.getFile(item.getUserId(), item.getMediaURL());
            if (file != null) avatar.setImage(new Image(file.toURI().toString()));

            button.setOnAction((event) -> {
                new Thread(() -> {
                    try {
                        RequestBuilder.sendJsonRequest(
                                "PUT", "connections",
                                JsonHandler.createJson("Authorization", DataAccess.getJWT()),
                                new Connect(item.getUserId(), DataAccess.getUserId(), " ", Connect.AcceptState.ACCEPTED).toJson());

                        Platform.runLater(() -> {
                            button.setDisable(true);
                            button.setVisible(false);
                            label.setText(item.getFirstName() + " " + item.getLastName() + " is in your network.");
                        });
                    } catch (NotAcceptableException e) {
                        Platform.runLater(() -> {
                            SceneManager.showNotification("Failure", "Request failed.", 3);
                        });
                    } catch (UnauthorizedException e) {
                        Platform.runLater(() -> {
                            SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                            SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                        });
                    }
                }).start();
            });

            setGraphic(root);
        }
    }
}
