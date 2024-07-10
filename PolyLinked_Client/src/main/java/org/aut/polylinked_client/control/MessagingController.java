package org.aut.polylinked_client.control;

import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.aut.polylinked_client.SceneManager;
import org.aut.polylinked_client.model.Message;
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
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class MessagingController {
    private final static String fileId = "messaging"; // home.css

    private static MapListView<Message> mapListView;

    private File pickedFile;

    private User user;

    private User owner;

    @FXML
    private Button fileButton;

    @FXML
    private VBox mediaBox;

    @FXML
    private JFXTextArea messageText;

    @FXML
    private BorderPane root;

    @FXML
    private BorderPane pane;

    @FXML
    void initialize() {
        // theme observation
        SceneManager.getThemeProperty().addListener((observable, oldValue, newValue) -> {
            SceneManager.activateTheme(root, fileId);
        });
        SceneManager.activateTheme(root, fileId);

        fileButton.setOnAction(e -> {
            fileButton.setText("");
            fileButton.setVisible(false);
            pickedFile = null;
            mediaBox.getChildren().clear();
        });
        fileButton.fire();
        pane.setDisable(true);
        pane.setVisible(false);

        new Thread(() -> {
            JSONObject jsonObject = null;
            try {
                jsonObject = RequestBuilder.jsonFromGetRequest("users/" + DataAccess.getUserId(),
                        JsonHandler.createJson("Authorization", DataAccess.getJWT()));
                owner = new User(jsonObject);
            } catch (Exception ignored) {
            }
        }).start();

        new Thread(() -> {
            try {
                JSONObject headers = JsonHandler.createJson("Authorization", DataAccess.getJWT());
                List<User> connections = RequestBuilder.arrayFromGetRequest(User.class, "connections/" + DataAccess.getUserId(), headers);

                if (connections.isEmpty()) {
                    Platform.runLater(() -> {
                        root.setCenter(new Label("No connections found"));
                    });
                } else {
                    UserListController controller = new UserListController(connections);
                    Platform.runLater(() -> {
                        root.setLeft(controller.getListView());
                    });

                    controller.getListView().getSelectionModel().selectedItemProperty().addListener((observable, oldUser, newUser) -> {
                        if (owner == null) return;
                        new Thread(() -> {
                            try {
                                user = newUser;
                                List<Message> messages = RequestBuilder.arrayFromGetRequest(Message.class,
                                        "messages/" + DataAccess.getUserId() + "&" + user.getUserId(), headers);
                                messages.sort(Comparator.comparing(Message::getDate));
                                TreeMap<Message, User> messageData = new TreeMap<>();
                                messages.forEach((e) -> {
                                    if (e.getSenderId().equals(owner.getUserId()))
                                        messageData.put(e, owner);
                                    else
                                        messageData.put(e, user);
                                });

                                ListView<ContentCell<Message>> listView = new ListView<>();
                                mapListView = new MapListView<>(listView, messageData, messages);
                                mapListView.activate(10);
                                Platform.runLater(() -> {
                                    pane.setCenter(listView);
                                    pane.setVisible(true);
                                    pane.setDisable(false);
                                });
                            } catch (UnauthorizedException e) {
                                Platform.runLater(() -> {
                                    SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                                    SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                                });
                            }
                        }).start();
                    });
                }
            } catch (UnauthorizedException e) {
                Platform.runLater(() -> {
                    SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                    SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                });
            }
        }).start();
    }

    @FXML
    void sendPressed(ActionEvent event) {
        if (pickedFile == null && messageText.getText().trim().isEmpty()) {
            SceneManager.showNotification("Info", "You cannot send empty content!", 3);
            return;
        }

        String text = messageText.getText().trim();
        new Thread(() -> {
            try {
                Message message = new Message(DataAccess.getUserId(), user.getUserId(), text);
                sendMessage(message, pickedFile);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    SceneManager.showNotification("Failure", "message Couldn't be added. Please try again later.", 3);
                });
            } finally {
                Platform.runLater(() -> {
                    pickedFile = null;
                    messageText.setText("");
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
            fileButton.setVisible(false);
            fileButton.setText("");
            mediaBox.getChildren().clear();
        } else if (!file.isFile()) {
            SceneManager.showNotification("Failure", "File seems corrupted.", 3);
        } else if (file.length() > 1000000000) {
            SceneManager.showNotification("Failure", "File seems too large.", 3);
        } else {
            fileButton.setText(file.getName());
            fileButton.setVisible(true);
            pickedFile = file;
            Platform.runLater(() -> {
                MediaWrapper wrapper = MediaWrapper.getMediaViewer(file, 0.45);
                mediaBox.getChildren().clear();
                mediaBox.getChildren().add(wrapper);
            });
        }
    }

    private void sendMessage(Message message, File pickedFile) {
        new Thread(() -> {
            try {
                RequestBuilder.sendMediaLinkedRequest("POST", "messages",
                        JsonHandler.createJson("Authorization", DataAccess.getJWT()), message, pickedFile);
                JSONObject jsonObject = RequestBuilder.jsonFromGetRequest("users/" + message.getSenderId(),
                        JsonHandler.createJson("Authorization", DataAccess.getJWT()));

                if (jsonObject != null) {
                    User user = new User(jsonObject);
                    Platform.runLater(() -> {
                        if (mapListView != null) mapListView.addFirst(new ContentCell<>(message, user));
                    });
                } else
                    throw new NotAcceptableException("UnKnown");
            } catch (NotAcceptableException e) {
                Platform.runLater(() -> {
                    SceneManager.showNotification("Failure", "Message Couldn't be added. Please try again later.", 3);
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