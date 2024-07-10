package org.aut.polylinked_client.control;

import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.aut.polylinked_client.SceneManager;
import org.aut.polylinked_client.model.Post;
import org.aut.polylinked_client.model.User;
import org.aut.polylinked_client.utils.DataAccess;
import org.aut.polylinked_client.utils.JsonHandler;
import org.aut.polylinked_client.utils.RequestBuilder;
import org.aut.polylinked_client.utils.exceptions.NotAcceptableException;
import org.aut.polylinked_client.utils.exceptions.UnauthorizedException;
import org.aut.polylinked_client.view.ContentCell;
import org.aut.polylinked_client.view.MapListView;
import org.aut.polylinked_client.view.MediaWrapper;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

public class HomeController {
    private final static String fileId = "home"; // home.css

    private static MapListView<Post> mapListView;

    private File pickedFile;

    @FXML
    private Button fileButton;

    @FXML
    private VBox mediaBox;

    @FXML
    private JFXTextArea postText;

    @FXML
    private BorderPane root;

    @FXML
    private void initialize() {
        // theme observation
        SceneManager.activateTheme(root, fileId);
        SceneManager.getThemeProperty().addListener((observable, oldValue, newValue) -> {
            SceneManager.activateTheme(root, fileId);
        });

        fileButton.setOnAction(e -> {
            pickedFile = null;
            fileButton.setText("");
            fileButton.setVisible(false);
            mediaBox.getChildren().clear();
        });
        fileButton.fire();

        new Thread(() -> {
            try {
                TreeMap<Post, User> postsData = RequestBuilder.mapFromGetRequest(Post.class, "newsfeed", JsonHandler.createJson("Authorization", DataAccess.getJWT()));
                Platform.runLater(() -> {
                    ListView<ContentCell<Post>> postsListView = new ListView<>();
                    ArrayList<Post> sortedKeys = new ArrayList<>(postsData.keySet().stream().toList());
                    sortedKeys.sort(Comparator.comparing(Post::getDate).reversed());

                    mapListView = new MapListView<>(postsListView, postsData, sortedKeys);
                    mapListView.activate(10);
                    root.setCenter(postsListView);
                });
            } catch (UnauthorizedException e) {
                Platform.runLater(() -> {
                    SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                    SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                });
            }
        }).start();
    }

    @FXML
    private void postPressed() {
        if (pickedFile == null && postText.getText().trim().isEmpty()) {
            SceneManager.showNotification("Info", "You cannot post empty content!", 3);
            return;
        }

        String text = postText.getText().trim();
        new Thread(() -> {
            try {
                Post post = new Post(DataAccess.getUserId(), text);
                sendPost(post, pickedFile);
            } catch (NotAcceptableException e) {
                Platform.runLater(() -> {
                    SceneManager.showNotification("Failure", "Post Couldn't be added. Please try again later.", 3);
                });
            } finally {
                Platform.runLater(() -> {
                    postText.setText("");
                    pickedFile = null;
                    fileButton.setText("");
                    fileButton.setVisible(false);
                    mediaBox.getChildren().clear();
                });
            }
        }).start();
    }

    @FXML
    void audioPressed(ActionEvent event) {
        File file = SceneManager.showFileChooser(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac"));
        pickFile(file);
    }

    @FXML
    void videoPressed(ActionEvent event) {
        File file = SceneManager.showFileChooser(new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.m4v", "*.flv"));
        pickFile(file);
    }

    @FXML
    void photoPressed(ActionEvent event) {
        File file = SceneManager.showFileChooser(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        pickFile(file);
    }

    private void pickFile(File file) {
        if (file == null) {
            fileButton.setText("");
            fileButton.setVisible(false);
            mediaBox.getChildren().clear();
        } else if (!file.isFile()) {
            SceneManager.showNotification("Failure", "File is corrupted.", 3);
        } else if (file.length() > 1000000000) {
            SceneManager.showNotification("Failure", "File is too large.", 3);
        } else {
            pickedFile = file;
            fileButton.setText(file.getName());
            fileButton.setVisible(true);
            Platform.runLater(() -> {
                MediaWrapper wrapper = MediaWrapper.getMediaViewer(file, 0.45);
                mediaBox.getChildren().clear();
                mediaBox.getChildren().add(wrapper);
            });
        }
    }

    public static void sendPost(Post post, File pickedFile) {
        new Thread(() -> {
            try {
                RequestBuilder.sendMediaLinkedRequest("POST", "posts",
                        JsonHandler.createJson("Authorization", DataAccess.getJWT()), post, pickedFile);
                JSONObject jsonObject = RequestBuilder.jsonFromGetRequest("users/" + post.getUserId(),
                        JsonHandler.createJson("Authorization", DataAccess.getJWT()));

                if (jsonObject != null) {
                    User user = new User(jsonObject);
                    Platform.runLater(() -> {
                        if (mapListView != null) mapListView.addFirst(new ContentCell<>(post, user));
                        SceneManager.showNotification("Success", "Your new Post Added.", 3);
                    });
                } else
                    throw new NotAcceptableException("UnKnown");
            } catch (NotAcceptableException e) {
                Platform.runLater(() -> {
                    SceneManager.showNotification("Failure", "Post Couldn't be added. Please try again later.", 3);
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
