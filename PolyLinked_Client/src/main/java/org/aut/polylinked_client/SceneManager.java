package org.aut.polylinked_client;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.aut.polylinked_client.utils.DataAccess;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class SceneManager {
    public enum Theme {
        LIGHT("light"), DARK("dark");

        public final String value;

        Theme(String value) {
            this.value = value;
        }
    }

    private static Stage stage;
    private static final StringProperty theme = new SimpleStringProperty();
    public static final double FONT_SIZE = Font.getDefault().getSize();
    private static final String SCENE_CSS = "scene";

    public SceneManager(Stage primaryStage) {
        stage = primaryStage;
        theme.set(DataAccess.getTheme());
    }

    public static void activateTheme(String cssID) { // on current scene: for login, signup and main
        Scene scene = stage.getScene();
        URL css = PolyLinked.class.getResource("styles/" + theme.getValue() + "/" + SCENE_CSS + ".css");
        if (css != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().setAll(css.toExternalForm());
        }

        Parent root = scene.getRoot();
        activateTheme(root, cssID);
        root.setStyle("-fx-font-size: " + FONT_SIZE + ";");
    }

    public static void activateTheme(Parent root, String cssID) {
        URL css = PolyLinked.class.getResource("styles/" + theme.getValue() + "/" + cssID + ".css");
        if (css != null) {
            root.getStylesheets().clear();
            root.getStylesheets().setAll(css.toExternalForm());
        }
    }

    public static StringProperty getThemeProperty() {
        return theme;
    }

    public static void setThemeProperty(SceneManager.Theme newTheme) {
        theme.set(newTheme.value);
        DataAccess.setTheme(newTheme);
    }

    public static void switchRoot(Parent root, BooleanProperty switched) {
        switched.set(false);
        Parent prev = stage.getScene().getRoot();
        stage.getScene().setRoot(root);

        switched.addListener((observable, oldValue, newValue) -> {
            if (newValue) stage.getScene().setRoot(prev);
        });
    }

    public void setScene() {
        if (DataAccess.getJWT().equals("none"))
            setScene(SceneLevel.LOGIN);
        else
            setScene(SceneLevel.MAIN);
    }

    public static void setScene(SceneLevel sceneLevel) {
        if (stage == null) {
            System.err.println("Primary stage not set");
            System.exit(1);
        }

        double width = 0;
        double height = 0;
        if (stage.getScene() != null) {
            width = stage.getWidth();
            height = stage.getHeight();
        }

        Scene scene = sceneLevel.getScene();
        makeResponsive(scene, sceneLevel.cssId);
        stage.setScene(scene);
        if (width > 0 && height > 0) {
            stage.setWidth(width);
            stage.setHeight(height);
        } else {
            stage.setWidth(800 * FONT_SIZE / 13);
            stage.setHeight(600 * FONT_SIZE / 13);
            stage.setMinHeight(80 * FONT_SIZE / 13);
            stage.setMinWidth(600 * FONT_SIZE / 13);
        }
    }

    private static void makeResponsive(Scene scene, String rootCssId) {
        String theme = DataAccess.getTheme();
        URL rootCss = PolyLinked.class.getResource("styles/" + theme + "/" + rootCssId + ".css");
        URL sceneCss = PolyLinked.class.getResource("styles/" + theme + "/" + SCENE_CSS + ".css");

        Parent parent = scene.getRoot();
        if (rootCss != null && sceneCss != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().setAll(sceneCss.toExternalForm());

            parent.getStylesheets().clear();
            parent.getStylesheets().setAll(rootCss.toExternalForm());
            parent.setStyle("-fx-font-size: " + FONT_SIZE + ";");
        }
    }

    public enum SceneLevel {
        LOGIN("fxmls/login.fxml", "login"),
        SIGNUP("fxmls/signup.fxml", "login"),
        MAIN("fxmls/main.fxml", "main");

        private final URL fxmlURL;
        public final String cssId;

        SceneLevel(String fxmlURL, String cssId) {
            this.fxmlURL = PolyLinked.class.getResource(fxmlURL);
            this.cssId = cssId;
        }

        Scene getScene() {
            try {
                FXMLLoader loader = new FXMLLoader(fxmlURL);
                return new Scene(loader.load());
            } catch (IOException e) {
                System.err.println("Error loading fxml: " + fxmlURL);
                System.exit(1);
            }
            return null;
        }
    }

    public static File showFileChooser(FileChooser.ExtensionFilter extFilter) {
        if (stage == null) return null;

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser.showOpenDialog(stage);
    }

    public static void showNotification(String title, String text, int seconds) {
        Notifications.create()
                .title(title)
                .text(text)
                .hideAfter(Duration.seconds(seconds))
                .position(Pos.BOTTOM_RIGHT)
                .owner(stage)
                .show();
    }

    public static ReadOnlyDoubleProperty getWidthProperty() {
        return stage.widthProperty();
    }

    public static ReadOnlyDoubleProperty getHeightProperty() {
        return stage.heightProperty();
    }
}
