package org.aut.polylinked_client.control;

import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.aut.polylinked_client.SceneManager;
import org.aut.polylinked_client.model.Comment;
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

public class CommentsController {

    private final static String fileId = "home"; // home.css

    private final BooleanProperty switched = new SimpleBooleanProperty(false);

    private MapListView<Comment> mapListView;

    private File pickedFile;

    private Post post;

    private final IntegerProperty count = new SimpleIntegerProperty(0);;

    @FXML
    private JFXTextArea commentText;

    @FXML
    private Button fileButton;

    @FXML
    private VBox mediaBox;

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
            fileButton.setText("");
            fileButton.setVisible(false);
            pickedFile = null;
            mediaBox.getChildren().clear();
        });
        fileButton.fire();
    }

    void setData(Post post) {
        this.post = post;
        new Thread(() -> {
            try {
                TreeMap<Comment, User> commentsData = RequestBuilder.mapFromGetRequest(Comment.class, "comments/" + post.getPostId(), JsonHandler.createJson("Authorization", DataAccess.getJWT()));
                Platform.runLater(() -> {
                        ListView<ContentCell<Comment>> listView = new ListView<>();
                        ArrayList<Comment> sortedKeys = new ArrayList<>(commentsData.keySet().stream().toList());
                        sortedKeys.sort(Comparator.comparing(Comment::getCreateDate).reversed());

                        mapListView = new MapListView<>(listView, commentsData, sortedKeys);
                        mapListView.activate(10);
                        root.setCenter(listView);
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
    void commentPressed(ActionEvent event) {
        if (pickedFile == null && commentText.getText().trim().isEmpty()) {
            SceneManager.showNotification("Info", "You cannot comment empty content!", 3);
            return;
        } else if (post == null) {
            SceneManager.showNotification("Info", "Not Found!", 3);
            return;
        }

        String text = commentText.getText().trim();
        new Thread(() -> {
            try {
                Comment comment = new Comment(DataAccess.getUserId(), post.getPostId(), text);
                RequestBuilder.sendMediaLinkedRequest("POST", "comments", JsonHandler.createJson("Authorization", DataAccess.getJWT()), comment, pickedFile);

                JSONObject jsonObject = RequestBuilder.jsonFromGetRequest("users/" + post.getUserId(), JsonHandler.createJson("Authorization", DataAccess.getJWT()));
                if (jsonObject != null) {
                    User user = new User(jsonObject);
                    Platform.runLater(() -> {
                        mapListView.addFirst(new ContentCell<>(comment, user));
                        SceneManager.showNotification("Success", "Your new comment Added.", 3);
                        count.set(count.get() + 1);
                    });
                } else
                    throw new NotAcceptableException("UnKnown");
            } catch (NotAcceptableException e) {
                Platform.runLater(() -> {
                    SceneManager.showNotification("Failure", "Comment Couldn't be added. Please try again later.", 3);
                });
            } catch (UnauthorizedException e) {
                Platform.runLater(() -> {
                    SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                    SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                });
            } finally {
                Platform.runLater(() -> {
                    commentText.setText("");
                    pickedFile = null;
                    fileButton.setText("");
                    fileButton.setVisible(false);
                    mediaBox.getChildren().clear();
                });
            }
        }).start();
    }

    @FXML
    void backPressed(ActionEvent event) {
        switched.set(true);
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
            fileButton.setVisible(false);
            fileButton.setText("");
            mediaBox.getChildren().clear();
        } else if (!file.isFile()) {
            SceneManager.showNotification("Failure", "File is corrupted.", 3);
        } else if (file.length() > 1000000000) {
            SceneManager.showNotification("Failure", "File is too large.", 3);
        } else {
            fileButton.setText(file.getName());
            pickedFile = file;
            fileButton.setVisible(true);
            Platform.runLater(() -> {
                MediaWrapper wrapper = MediaWrapper.getMediaViewer(file, 0.45);
                mediaBox.getChildren().clear();
                mediaBox.getChildren().add(wrapper);
            });
        }
    }

    public BooleanProperty isSwitched() {
        return switched;
    }

    public IntegerProperty countProperty() {
        return count;
    }
}
