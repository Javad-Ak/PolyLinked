package org.aut.polylinked_client.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.aut.polylinked_client.PolyLinked;
import org.aut.polylinked_client.SceneManager;
import org.aut.polylinked_client.utils.DataAccess;
import org.aut.polylinked_client.utils.RequestBuilder;
import org.aut.polylinked_client.view.MediaWrapper;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class MainController {
    enum Tabs {
        HOME, MESSAGING, NOTIFICATIONS, PROFILE, SEARCH
    }

    @FXML
    private BorderPane borderPane;

    @FXML
    private ToggleButton homeToggle;

    @FXML
    private ToggleButton messagingToggle;

    @FXML
    private ToggleButton notificationsToggle;

    @FXML
    private ToggleButton profileToggle;

    @FXML
    private ToggleButton searchToggle;

    @FXML
    private ToggleGroup tabs;

    @FXML
    private ImageView appIcon;

    @FXML
    void initialize() {
        appIcon.setFitHeight(SceneManager.FONT_SIZE * 40 / 13);
        appIcon.setFitWidth(SceneManager.FONT_SIZE * 40 / 13);

        homeToggle.setUserData(Tabs.HOME);
        messagingToggle.setUserData(Tabs.MESSAGING);
        notificationsToggle.setUserData(Tabs.NOTIFICATIONS);
        profileToggle.setUserData(Tabs.PROFILE);
        searchToggle.setUserData(Tabs.SEARCH);

        initializeTabs();
        tabs.selectToggle(homeToggle);

        // theme observation
        SceneManager.getThemeProperty().addListener((observable, oldValue, newValue) -> {
            SceneManager.activateTheme(SceneManager.SceneLevel.MAIN.cssId);
        });
    }

    private void initializeTabs() {
        tabs.selectToggle(profileToggle);
        tabs.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                tabs.selectToggle(oldValue);
            } else if (oldValue != null) {
                FXMLLoader loader;
                try {
                    switch ((Tabs) newValue.getUserData()) {
                        case Tabs.HOME: {
                            loader = new FXMLLoader(PolyLinked.class.getResource("fxmls/home.fxml"));
                            Parent root = loader.load();
                            borderPane.setCenter(root);
                        }
                        break;
                        case Tabs.MESSAGING: {
                            loader = new FXMLLoader(PolyLinked.class.getResource("fxmls/messaging.fxml"));
                            Parent root = loader.load();
                            borderPane.setCenter(root);
                        }
                        break;
                        case Tabs.NOTIFICATIONS: {
                            NotificationController controller = new NotificationController();
                            borderPane.setCenter(controller.getRoot());
                        }
                        break;
                        case Tabs.SEARCH: {
                            loader = new FXMLLoader(PolyLinked.class.getResource("fxmls/search.fxml"));
                            Parent root = loader.load();
                            borderPane.setCenter(root);
                        }
                        break;
                        case Tabs.PROFILE: {
                            loader = new FXMLLoader(PolyLinked.class.getResource("fxmls/profile.fxml"));
                            Parent root = loader.load();
                            ProfileController controller = loader.getController();
                            controller.setData(DataAccess.getUserId());
                            borderPane.setCenter(root);
                        }
                        break;
                        default: {
                            System.err.println("Failed to load fxml: ");
                            System.exit(1);
                        }
                        break;
                    }
                } catch (IOException e) {
                    System.err.println("Failed to load fxml: ");
                    System.exit(1);
                }

            }
        });
    }

    @FXML
    void aboutAppPressed(ActionEvent event) {
        String message = """
                PolyLinked is a LinkedIn clone desktop Application developed in javafx.\
                               \s
                This Application was the project of 2024 AP course at @AUT-CE.)\
                               \s
                Authors: Alireza Atharifard, MohammadJavad Akbari""";

        Dialog<ButtonType> dialog = createDialogue("About PolyLinked", message, ButtonType.CLOSE);
        dialog.showAndWait();
    }

    @FXML
    void deleteAccountPressed(ActionEvent event) {
        Dialog<ButtonType> dialog = createDialogue("Confirm", "Are you sure you want to Delete this Account?", ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            JSONObject json = new JSONObject();
            json.put("Authorization", DataAccess.getJWT());
            try {
                RequestBuilder.buildConnection("DELETE", "users", json, false).getResponseCode();
            } catch (IOException ignored) {
            }

            DataAccess.clearCacheData();
            DataAccess.clearUserData();
            SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
        }
    }

    @FXML
    void logOutPressed(ActionEvent event) {
        Dialog<ButtonType> dialog = createDialogue("Confirm", "Are you sure you want to log out?", ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DataAccess.clearCacheData();
            DataAccess.clearUserData();
            SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
        }
    }

    @FXML
    void switchThemePressed(ActionEvent event) {
        if (DataAccess.getTheme().equals("light"))
            SceneManager.setThemeProperty(SceneManager.Theme.DARK);
        else
            SceneManager.setThemeProperty(SceneManager.Theme.LIGHT);
    }

    public static Dialog<ButtonType> createDialogue(String title, String message, ButtonType... buttonTypes) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setContentText(message);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypes);

        dialog.getDialogPane().getChildren().forEach(n -> n.setStyle("-fx-text-fill: black;"));
        if (DataAccess.getTheme().equals("light"))
            dialog.getDialogPane().setStyle("-fx-background-color: #e6e6e6;");
        else
            dialog.getDialogPane().setStyle("-fx-background-color: #b9b9b9;");

        return dialog;
    }
}
