package org.aut.polylinked_client.control;

import io.github.gleidson28.GNAvatarView;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.aut.polylinked_client.PolyLinked;
import javafx.stage.FileChooser;
import org.aut.polylinked_client.SceneManager;
import org.aut.polylinked_client.model.*;
import org.aut.polylinked_client.utils.DataAccess;
import org.aut.polylinked_client.utils.JsonHandler;
import org.aut.polylinked_client.utils.RequestBuilder;
import org.aut.polylinked_client.utils.exceptions.NotAcceptableException;
import org.aut.polylinked_client.utils.exceptions.UnauthorizedException;
import org.aut.polylinked_client.view.ContentCell;
import org.aut.polylinked_client.view.MapListView;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProfileController {
    private final static String fileId = "profile";
    private final static Path defaultAvatar = Path.of("src/main/resources/org/aut/polylinked_client/images/avatar.png");
    private final static Path defaultBG = Path.of("src/main/resources/org/aut/polylinked_client/images/background.jpeg");

    private final BooleanProperty switched = new SimpleBooleanProperty(false);

    private User user = null;
    private Profile profile = null;
    private CallInfo callInfo = null;
    private Education education = null;
    private Skill skill = null;

    @FXML
    private GNAvatarView avatar;

    @FXML
    private Hyperlink connectionsLink;

    @FXML
    private BorderPane borderPane;

    @FXML
    private HBox buttonBox;

    @FXML
    private Button backButton;

    @FXML
    private Button avatarButton;

    @FXML
    private ImageView background;

    @FXML
    private Button bannerButton;

    @FXML
    private Text bioLabel;

    @FXML
    private Text callInfoText;

    @FXML
    private Button connectButton;

    @FXML
    private Button editButton;

    @FXML
    private Text educationText;

    @FXML
    private Button followButton;

    @FXML
    private Hyperlink followersLink;

    @FXML
    private Hyperlink followingsLink;

    @FXML
    private Label joinedDateLabel;

    @FXML
    private Label locationLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private ScrollPane root;

    @FXML
    private VBox postBox;

    @FXML
    void initialize() {
        // theme observation
        SceneManager.activateTheme(root, fileId);
        SceneManager.getThemeProperty().addListener((observable, oldValue, newValue) -> {
            SceneManager.activateTheme(root, fileId);
        });

        background.fitWidthProperty().bind(SceneManager.getWidthProperty().multiply(0.9973));
        background.fitHeightProperty().bind(SceneManager.getHeightProperty().multiply(0.25));
    }

    public void setData(String userId) {
        if (DataAccess.getUserId().equals(userId)) {
            buttonBox.getChildren().clear();
            buttonBox.getChildren().addAll(avatarButton, bannerButton, editButton);
            backButton.setDisable(true);
            backButton.setVisible(false);

            setUpEdit(editButton, userId);
            setUpImagePicker(userId);
        } else {
            buttonBox.getChildren().clear();
            buttonBox.getChildren().addAll(followButton, connectButton);
            setUpFollow(followButton, userId);
            setUpConnect(connectButton, userId);
            activateBackButton();
        }

        new Thread(() -> {
            try {
                JSONObject headers = JsonHandler.createJson("Authorization", DataAccess.getJWT());

                try {
                    this.user = new User(RequestBuilder.jsonFromGetRequest("users/" + userId, headers));
                } catch (NotAcceptableException e) {
                    return;
                }

                try {
                    this.profile = new Profile(RequestBuilder.jsonFromGetRequest("users/profiles/" + userId, headers));
                } catch (NotAcceptableException ignored) {
                }

                try {
                    this.callInfo = new CallInfo(RequestBuilder.jsonFromGetRequest("users/callInfo/" + userId, headers));
                } catch (NotAcceptableException ignored) {
                }

                List<Education> educations = RequestBuilder.arrayFromGetRequest(Education.class, "users/educations/" + userId, headers);
                List<Skill> skills = RequestBuilder.arrayFromGetRequest(Skill.class, "users/skills/" + userId, headers);

                List<User> followers = RequestBuilder.arrayFromGetRequest(User.class, "users/followers/" + userId, headers);
                List<User> followings = RequestBuilder.arrayFromGetRequest(User.class, "users/followings/" + userId, headers);
                List<User> connections = RequestBuilder.arrayFromGetRequest(User.class, "connections/" + userId, headers);
                List<Post> posts = RequestBuilder.arrayFromGetRequest(Post.class, "posts/user/" + userId, headers);

                if (!educations.isEmpty()) this.education = educations.getFirst();
                if (!skills.isEmpty()) this.skill = skills.getFirst();

                User user = this.user;
                Profile profile = this.profile;
                Skill skill = this.skill;
                CallInfo callInfo = this.callInfo;
                Platform.runLater(() -> {
                    if (user != null) {
                        File image = DataAccess.getFile(userId, user.getMediaURL());
                        if (image != null)
                            avatar.setImage(new Image(image.toURI().toString()));
                        else
                            avatar.setImage(new Image(defaultAvatar.toUri().toString()));


                        File bg = DataAccess.getFile("bg" + userId, user.getBannerURL());
                        if (bg != null)
                            background.setImage(new Image(bg.toURI().toString()));
                        else
                            background.setImage(new Image(defaultBG.toUri().toString()));

                        nameLabel.setText(user.getFirstName() + " " + user.getAdditionalName() + " " + user.getLastName());
                        Date date = user.getCreateDate();
                        joinedDateLabel.setText("Joined at " + new SimpleDateFormat("yyyy-MM-dd").format(date));
                    }

                    if (user != null && profile != null) {
                        bioLabel.setText(profile.getBio());
                        locationLabel.setText(profile.getCity() + ", " + profile.getCountry());
                    }

                    if (callInfo != null) callInfoText.setText(callInfo.toString());

                    StringBuilder builder = new StringBuilder();
                    if (!educations.isEmpty() && educations.getFirst() != null) {
                        this.education = educations.getFirst();
                        builder.append(education.toString());
                    }
                    if (!skills.isEmpty() && skills.getFirst() != null) {
                        this.skill = skills.getFirst();
                        builder.append(skill.toString());
                    }
                    educationText.setText(builder.toString());
                    followersLink.setText(followers.size() + " Followers");
                    followingsLink.setText(followings.size() + " Followings");
                    connectionsLink.setText(connections.size() + " Connections");

                    if (!followers.isEmpty()) followersLink.setOnAction((ActionEvent event) -> {
                        new Thread(() -> {
                            try {
                                List<User> users = RequestBuilder.arrayFromGetRequest(User.class, "users/followers/" + userId,
                                        JsonHandler.createJson("Authorization", DataAccess.getJWT()));

                                UserListController controller = new UserListController(users);
                                UserListController.initiatePage(controller.getListView());
                            } catch (UnauthorizedException e) {
                                Platform.runLater(() -> {
                                    SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                                    SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                                });
                            }
                        }).start();
                    });

                    if (!followings.isEmpty()) followingsLink.setOnAction((ActionEvent event) -> {
                        new Thread(() -> {
                            try {
                                List<User> users = RequestBuilder.arrayFromGetRequest(User.class, "users/followings/" + userId,
                                        JsonHandler.createJson("Authorization", DataAccess.getJWT()));

                                UserListController controller = new UserListController(users);
                                UserListController.initiatePage(controller.getListView());
                            } catch (UnauthorizedException e) {
                                Platform.runLater(() -> {
                                    SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                                    SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                                });
                            }
                        }).start();
                    });

                    if (!posts.isEmpty()) {
                        ListView<ContentCell<Post>> postsListView = new ListView<>();
                        new Thread(() -> {
                            TreeMap<Post, User> postsData = new TreeMap<>();
                            posts.forEach(value -> postsData.put(value, user));

                            ArrayList<Post> sortedKeys = new ArrayList<>(postsData.keySet().stream().toList());
                            sortedKeys.sort(Comparator.comparing(Post::getDate).reversed());
                            MapListView<Post> mapListView = new MapListView<>(postsListView, postsData, sortedKeys);
                            mapListView.activate(10);
                        }).start();

                        postBox.getChildren().clear();
                        postBox.getChildren().add(postsListView);
                    } else {
                        postBox.getChildren().clear();
                        postBox.getChildren().add(new Label("No posts found"));
                    }

                    if (!connections.isEmpty()) connectionsLink.setOnAction((ActionEvent event) -> {
                        new Thread(() -> {
                            try {
                                List<User> users = RequestBuilder.arrayFromGetRequest(User.class, "connections/" + userId,
                                        JsonHandler.createJson("Authorization", DataAccess.getJWT()));

                                UserListController controller = new UserListController(users);
                                UserListController.initiatePage(controller.getListView());
                            } catch (UnauthorizedException e) {
                                Platform.runLater(() -> {
                                    SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                                    SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                                });
                            }
                        }).start();
                    });
                });
            } catch (UnauthorizedException e) {
                Platform.runLater(() -> {
                    SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                    SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                });
            }
        }).start();
    }

    public static void setUpFollow(Button followButton, String userId) {
        if (userId.equals(DataAccess.getUserId())) {
            followButton.setDisable(true);
            followButton.setVisible(false);
            return;
        }

        JSONObject followedHeader = RequestBuilder.buildHeadRequest("follows/" + userId, JsonHandler.createJson("Authorization", DataAccess.getJWT()));
        if (followedHeader != null && followedHeader.getString("Exists") != null && followedHeader.getString("Exists").equalsIgnoreCase("true")) {
            followButton.setText("Unfollow");
        } else {
            followButton.setText("  Follow  ");
        }

        followButton.setOnAction((ActionEvent event) -> {
            String method;
            String newText = followButton.getText();
            if (newText.contains("Follow")) {
                method = "POST";
                newText = "Unfollow";
            } else {
                method = "DELETE";
                newText = "  Follow  ";
            }

            String finalNewText = newText;
            new Thread(() -> {
                try {
                    RequestBuilder.sendJsonRequest(
                            method, "follows",
                            JsonHandler.createJson("Authorization", DataAccess.getJWT()),
                            new Follow(DataAccess.getUserId(), userId).toJson());

                    Platform.runLater(() -> {
                        followButton.setText(finalNewText);
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
    }

    private static void setUpConnect(Button connectButton, String userId) {
        JSONObject header = RequestBuilder.buildHeadRequest("connections/" + userId, JsonHandler.createJson("Authorization", DataAccess.getJWT()));
        if (header == null || header.getString("Exists") == null) return;

        if (header.getString("Exists").equalsIgnoreCase("true")) {
            connectButton.setText("Disconnect");
        } else if (header.getString("Exists").equalsIgnoreCase("waiting")) {
            connectButton.setText("Appending");
        } else {
            connectButton.setText("Connect");
        }

        connectButton.setOnAction((ActionEvent event) -> {
            String method;
            String newText = connectButton.getText();
            Connect.AcceptState state;
            if (newText.contains("Connect")) {
                method = "POST";
                state = Connect.AcceptState.WAITING;
                newText = "Appending";
            } else if (newText.contains("Disconnect")) {
                method = "DELETE";
                state = Connect.AcceptState.ACCEPTED;
                newText = "Connect";
            } else {
                method = "DELETE";
                state = Connect.AcceptState.WAITING;
                newText = "Connect";
            }

            String finalNewText = newText;
            new Thread(() -> {
                try {
                    RequestBuilder.sendJsonRequest(
                            method, "connections",
                            JsonHandler.createJson("Authorization", DataAccess.getJWT()),
                            new Connect(DataAccess.getUserId(), userId, " ", state).toJson());

                    Platform.runLater(() -> {
                        connectButton.setText(finalNewText);
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
    }

    private void setUpEdit(Button editButton, String userId) {
        editButton.setOnAction((ActionEvent event) -> {
            try {
                FXMLLoader loader = new FXMLLoader(PolyLinked.class.getResource("fxmls/editProfile.fxml"));
                Parent parent = loader.load();

                EditProfileController controller = loader.getController();
                controller.setData(user, profile, callInfo, education, skill);

                SceneManager.switchRoot(parent, controller.isSwitched());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    private void setUpImagePicker(String userId) {
        avatarButton.setOnAction((e) -> {
            File file = SceneManager.showFileChooser(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

            if (file != null && (!file.isFile() || file.length() > 2000000)) {
                SceneManager.showNotification("Failure", "image is too long or corrupted.", 3);
            } else if (file != null) {
                try {
                    RequestBuilder.sendFileRequest("POST", "resources",
                            JsonHandler.createJson("Root", "Profile", "ID", userId), file);

                    SceneManager.showNotification("Success", "Avatar changed.", 3);
                    avatar.setImage(new Image(file.toURI().toString()));
                    DataAccess.deleteFile(userId);
                } catch (NotAcceptableException ex) {
                    SceneManager.showNotification("Failure", "Unknown.", 3);
                } catch (UnauthorizedException ex) {
                    SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                    SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                }
            }
        });

        bannerButton.setOnAction((e) -> {
            File banner = SceneManager.showFileChooser(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

            if (banner != null && (!banner.isFile() || banner.length() > 2000000)) {
                SceneManager.showNotification("Failure", "Banner is too long or corrupted.", 3);
            } else if (banner != null) {
                try {
                    RequestBuilder.sendFileRequest("POST", "resources",
                            JsonHandler.createJson("Root", "Background", "ID", "bg" + userId), banner);

                    SceneManager.showNotification("Success", "Banner changed.", 3);
                    background.setImage(new Image(banner.toURI().toString()));
                    DataAccess.deleteFile("bg" + userId);
                } catch (NotAcceptableException ex) {
                    SceneManager.showNotification("Failure", "Unknown.", 3);
                } catch (UnauthorizedException ex) {
                    SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                    SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                }
            }
        });
    }

    public BooleanProperty isSwitched() {
        return switched;
    }

    public void activateBackButton() {
        backButton.setDisable(false);
        backButton.setVisible(true);

        backButton.setOnAction((ActionEvent event) -> {
            switched.set(true);
        });
    }
}