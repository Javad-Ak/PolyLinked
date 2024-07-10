package org.aut.polylinked_client.control;

import io.github.gleidson28.GNAvatarView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.aut.polylinked_client.PolyLinked;
import org.aut.polylinked_client.SceneManager;
import org.aut.polylinked_client.model.*;
import org.aut.polylinked_client.utils.DataAccess;
import org.aut.polylinked_client.utils.JsonHandler;
import org.aut.polylinked_client.utils.RequestBuilder;
import org.aut.polylinked_client.utils.exceptions.NotAcceptableException;
import org.aut.polylinked_client.utils.exceptions.UnauthorizedException;
import org.aut.polylinked_client.view.MediaWrapper;
import org.json.JSONObject;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ContentController {
    private final static String fileId = "content"; // content.css file reference
    private final static Path defaultAvatar = Path.of("src/main/resources/org/aut/polylinked_client/images/avatar.png");

    @FXML
    private GNAvatarView avatar;

    @FXML
    private Hyperlink commentsLink;

    @FXML
    private Label dateLabel;

    @FXML
    private Button followButton;

    @FXML
    private Button likeButton;

    @FXML
    private Hyperlink likesLink;

    @FXML
    private Hyperlink nameLink;

    @FXML
    private Hyperlink repostLink;

    @FXML
    private GridPane pane;

    @FXML
    private VBox root;

    @FXML
    private Text textArea;

    @FXML
    private HBox mediaBox;

    @FXML
    void initialize() {
        SceneManager.activateTheme(pane, fileId);

        // theme observation
        SceneManager.getThemeProperty().addListener((observable, oldValue, newValue) -> {
            SceneManager.activateTheme(pane, fileId);
        });
    }

    public <T extends MediaLinked> void setData(T content, User user) {
        if (content instanceof Post post)
            setData(post, user);
        else if (content instanceof Comment comment)
            setData(comment, user);
        else if (content instanceof Message message)
            setData(message, user);
    }

    // fill data into fxml using fxmlLoader.getController
    private void setData(Post post, User user) {
        if (post == null || user == null) return;
        pane.setUserData(post);

        // fill data into fxml
        nameLink.setText(user.getFirstName() + " " + user.getLastName());
        setUpProfileLink(user, nameLink);

        likesLink.setText(post.getLikesCount() + " likes");
        commentsLink.setText(post.getCommentsCount() + " comments");
        commentsLink.setOnAction(this::commentPressed);
        textArea.setText(post.getText());

        Date date = new Date(post.getDate());
        dateLabel.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date));

        setUpLike(post);
        ProfileController.setUpFollow(followButton, post.getUserId());
        setUpRepost(post, user);
    }

    private void setData(Comment comment, User user) {
        if (comment == null || user == null) return;
        deleteRows();

        nameLink.setText(user.getFirstName() + " " + user.getLastName());
        setUpProfileLink(user, nameLink);
        textArea.setText(comment.getText());

        Date date = comment.getCreateDate();
        dateLabel.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date));

        File file = DataAccess.getFile(user.getUserId(), user.getMediaURL());
        File media = DataAccess.getFile(comment.getId(), comment.getMediaURL());
        setUpMedias(file, media);
    }

    private void setData(Message message, User user) {
        if (message == null || user == null) return;
        deleteRows();
        pane.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == 0);

        if (message.getSenderId().equals(DataAccess.getUserId()))
            root.setPadding(new Insets(0, 0, 0, 100));
        else
            root.setPadding(new Insets(0, 100, 0, 0));

        nameLink.setText(user.getFirstName() + " " + user.getLastName());
        setUpProfileLink(user, nameLink);
        textArea.setText(message.getText());

        Date date = new Date(message.getDate());
        dateLabel.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date));

        File file = DataAccess.getFile(user.getUserId(), user.getMediaURL());
        File media = DataAccess.getFile(message.getId(), message.getMediaURL());
        setUpMedias(file, media);
    }

    @FXML
    void commentPressed(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(PolyLinked.class.getResource("fxmls/comments.fxml"));
            Parent parent = loader.load();

            CommentsController controller = loader.getController();
            Post post = (Post) pane.getUserData();
            controller.setData(post);

            SceneManager.switchRoot(parent, controller.isSwitched());
            controller.countProperty().addListener((observable, oldValue, newValue) -> {
                post.setCommentsCount(post.getCommentsCount() + newValue.intValue() - oldValue.intValue());
                commentsLink.setText(post.getCommentsCount() + " comments");
            });
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    @FXML
    void repostPressed(ActionEvent event) {
        try {
            Post post = (Post) pane.getUserData();
            Post rePost = post.repost(DataAccess.getUserId());
            HomeController.sendPost(rePost, DataAccess.getFile(post.getPostId(), post.getMediaURL()));
        } catch (NotAcceptableException e) {
            SceneManager.showNotification("Failure", "Post Couldn't be added. Please try again later.", 3);
        }
    }

    void setUpLike(Post post) {
        FontIcon likeIcon = new FontIcon("mdi-thumb-up-outline");
        likeIcon.setId("icon");
        FontIcon likedIcon = new FontIcon("mdi-thumb-up");
        likedIcon.setId("icon");

        JSONObject likedHeader = RequestBuilder.buildHeadRequest("likes/" + post.getPostId(), JsonHandler.createJson("Authorization", DataAccess.getJWT()));
        if (likedHeader != null && likedHeader.getString("Exists") != null && likedHeader.getString("Exists").equalsIgnoreCase("true")) {
            likeButton.setGraphic(likedIcon);
        } else {
            likeButton.setGraphic(likeIcon);
        }

        likeButton.setOnAction((ActionEvent event) -> {
            String method;
            FontIcon newIcon = (FontIcon) likeButton.getGraphic();
            int count;
            if (newIcon.getIconLiteral().equals(likeIcon.getIconLiteral())) {
                method = "POST";
                count = 1;
                newIcon = likedIcon;
            } else {
                method = "DELETE";
                count = -1;
                newIcon = likeIcon;
            }

            FontIcon finalNewIcon = newIcon;
            new Thread(() -> {
                try {
                    post.setLikesCount(post.getLikesCount() + count);
                    RequestBuilder.sendJsonRequest(
                            method, "likes",
                            JsonHandler.createJson("Authorization", DataAccess.getJWT()),
                            new Like(post.getPostId(), DataAccess.getUserId()).toJson());

                    Platform.runLater(() -> {
                        likesLink.setText(post.getLikesCount() + " likes");
                        likeButton.setGraphic(finalNewIcon);
                    });
                } catch (NotAcceptableException e) {
                    Platform.runLater(() -> {
                        post.setLikesCount(post.getLikesCount() - count);
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

        likesLink.setOnAction((ActionEvent event) -> {
            new Thread(() -> {
                try {
                    List<User> users = RequestBuilder.arrayFromGetRequest(User.class, "likes/" + post.getPostId(), JsonHandler.createJson("Authorization", DataAccess.getJWT()));
                    if (users.isEmpty()) return;

                    UserListController userListController = new UserListController(users);
                    UserListController.initiatePage(userListController.getListView());
                } catch (UnauthorizedException e) {
                    Platform.runLater(() -> {
                        SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                        SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                    });
                }
            }).start();
        });
    }

    private void deleteRows() {
        pane.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) >= 2 &&
                !(node.getId() != null && node.getId().equals("date")));

        VBox vBox = (VBox) nameLink.getParent();
        vBox.getChildren().clear();
        vBox.getChildren().add(nameLink);
        followButton.setVisible(false);
        followButton.setDisable(true);
    }

    private void setUpMedias(File file, File media) {
        DataAccess.FileType type = DataAccess.getFileType(file);
        if (file != null && file.length() > 0 && type == DataAccess.FileType.IMAGE)
            avatar.setImage(new Image(file.toURI().toString()));
        else
            avatar.setImage(new Image(defaultAvatar.toUri().toString()));

        if (media != null && media.length() > 0) {
            MediaWrapper viewer = MediaWrapper.getMediaViewer(media, 0.45);
            mediaBox.getChildren().clear();
            mediaBox.getChildren().add(viewer);
        } else {
            mediaBox.getChildren().clear();
        }
    }

    private void setUpRepost(Post post, User user) {
        new Thread(() -> {
            if (post.isReposted()) {
                try {
                    JSONObject from = RequestBuilder.jsonFromGetRequest("posts/" + post.getRepostFrom(),
                            JsonHandler.createJson("Authorization", DataAccess.getJWT()));
                    Post repostFrom = new Post(from);

                    JSONObject owner = RequestBuilder.jsonFromGetRequest("users/" + repostFrom.getUserId(),
                            JsonHandler.createJson("Authorization", DataAccess.getJWT()));
                    User ownerUser = new User(owner);

                    Platform.runLater(() -> {
                        repostLink.setText(ownerUser.getFirstName() + " " + ownerUser.getLastName());
                        VBox vBox = (VBox) nameLink.getParent();
                        vBox.getChildren().clear();
                        vBox.getChildren().addAll(nameLink, repostLink);
                    });

                    setUpProfileLink(ownerUser, repostLink);
                } catch (NotAcceptableException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                } catch (UnauthorizedException e) {
                    Platform.runLater(() -> {
                        SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                        SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                    });
                }
            } else {
                Platform.runLater(() -> {
                    VBox vBox = (VBox) nameLink.getParent();
                    vBox.getChildren().clear();
                    vBox.getChildren().add(nameLink);
                });
            }

            File file = DataAccess.getFile(user.getUserId(), user.getMediaURL());
            File mediaFile = DataAccess.getFile(post.getPostId(), post.getMediaURL());
            Platform.runLater(() -> {
                setUpMedias(file, mediaFile);
            });
        }).start();
    }

    private void setUpProfileLink(User user, Hyperlink repostLink) {
        repostLink.setOnAction((ActionEvent event) -> {
            new Thread(() -> {
                FXMLLoader loader = new FXMLLoader(PolyLinked.class.getResource("fxmls/profile.fxml"));
                try {
                    Parent root = loader.load();
                    ProfileController profileController = loader.getController();
                    profileController.setData(user.getUserId());
                    profileController.activateBackButton();
                    Platform.runLater(() -> {
                        SceneManager.switchRoot(root, profileController.isSwitched());
                    });
                } catch (IOException e) {
                    System.err.println("Failed to load profile fxml");
                    System.exit(1);
                }
            }).start();
        });
    }
}
