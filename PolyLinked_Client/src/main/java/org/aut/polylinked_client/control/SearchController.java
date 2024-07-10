package org.aut.polylinked_client.control;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.aut.polylinked_client.SceneManager;
import org.aut.polylinked_client.model.Message;
import org.aut.polylinked_client.model.Post;
import org.aut.polylinked_client.model.User;
import org.aut.polylinked_client.utils.DataAccess;
import org.aut.polylinked_client.utils.JsonHandler;
import org.aut.polylinked_client.utils.RequestBuilder;
import org.aut.polylinked_client.utils.exceptions.UnauthorizedException;
import org.aut.polylinked_client.view.ContentCell;
import org.aut.polylinked_client.view.MapListView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class SearchController {

    @FXML
    private BorderPane root;

    @FXML
    private TextField textField;

    @FXML
    void initialize() {
        // theme observation
        SceneManager.activateTheme(root, "home");
        SceneManager.getThemeProperty().addListener((observable, oldValue, newValue) -> {
            SceneManager.activateTheme(root, "home");
        });
    }

    @FXML
    void searchPressed(ActionEvent event) {
        String text = textField.getText();
        if (text.startsWith("#")) {
            new Thread(() -> {
                try {
                    TreeMap<Post, User> postsData = RequestBuilder.mapFromGetRequest(Post.class, "search/hashtags/" +
                            text.substring(1), JsonHandler.createJson("Authorization", DataAccess.getJWT()));

                    ListView<ContentCell<Post>> postsListView = new ListView<>();
                    ArrayList<Post> sortedKeys = new ArrayList<>(postsData.keySet().stream().toList());
                    sortedKeys.sort(Comparator.comparing(Post::getDate).reversed());
                    Platform.runLater(()->{
                        if (sortedKeys.isEmpty()) {
                            root.setCenter(new Label("Nothing found"));
                        } else {
                            MapListView<Post> mapListView = new MapListView<>(postsListView, postsData, sortedKeys);
                            mapListView.activate(10);
                            root.setCenter(postsListView);
                        }
                    });
                } catch (UnauthorizedException e) {
                    Platform.runLater(() -> {
                        SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                        SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                    });
                }
            }).start();
        } else if (!text.isEmpty()) {
            new Thread(() -> {
                try {
                    List<User> users = RequestBuilder.arrayFromGetRequest(User.class, "search/users/" +
                            text, JsonHandler.createJson("Authorization", DataAccess.getJWT()));

                    Platform.runLater(() -> {
                        if (users.isEmpty()) {
                            root.setCenter(new Label("Nothing found"));
                        } else {
                            UserListController controller = new UserListController(users);
                            root.setCenter(controller.getListView());
                        }
                    });
                } catch (UnauthorizedException e) {
                    Platform.runLater(() -> {
                        SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                        SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                    });
                }
            }).start();
        }
    }
}
