package org.aut.polylinked_client.control;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import org.aut.polylinked_client.SceneManager;
import org.aut.polylinked_client.model.User;
import org.aut.polylinked_client.utils.DataAccess;
import org.aut.polylinked_client.utils.JsonHandler;
import org.aut.polylinked_client.utils.RequestBuilder;
import org.aut.polylinked_client.utils.exceptions.UnauthorizedException;
import org.aut.polylinked_client.view.NotificationCell;
import java.util.List;

public class NotificationController {
    BorderPane root;
    ObservableList<User> notifications = FXCollections.observableArrayList();

    public NotificationController() {
        root = new BorderPane();
        root.setPadding(new Insets(15, 15, 15, 15));
        SceneManager.activateTheme(root, "profile");
        SceneManager.getThemeProperty().addListener((observable, oldValue, newValue) -> {
            SceneManager.activateTheme(root, "profile");
        });

        new Thread(() -> {
            try {
                List<User> users = RequestBuilder.arrayFromGetRequest(User.class,
                        "connections", JsonHandler.createJson("Authorization", DataAccess.getJWT()));

                if (!users.isEmpty()) {
                    Platform.runLater(() -> {
                        ListView<User> listView = new ListView<>();
                        notifications.addAll(users);
                        listView.setItems(notifications);
                        listView.setCellFactory(view -> new NotificationCell());
                        root.setCenter(listView);
                    });
                } else {
                    Platform.runLater(() -> {
                        root.setCenter(new Label("Nothing Found"));
                    });
                }
            } catch (UnauthorizedException e) {
                SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
            }
        }).start();
    }

    public BorderPane getRoot() {
        return root;
    }
}
