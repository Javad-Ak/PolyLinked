package org.aut.polylinked_client.control;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.aut.polylinked_client.SceneManager;
import org.aut.polylinked_client.utils.DataAccess;
import org.aut.polylinked_client.utils.JsonHandler;
import org.aut.polylinked_client.utils.RequestBuilder;
import org.aut.polylinked_client.utils.exceptions.NotAcceptableException;
import org.aut.polylinked_client.utils.exceptions.UnauthorizedException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class LoginController {

    @FXML
    private JFXToggleButton themeToggle;

    @FXML
    private JFXTextField emailText;

    @FXML
    private JFXPasswordField passwordText;

    @FXML
    void initialize() {
        String theme = DataAccess.getTheme();
        themeToggle.setSelected(theme.equalsIgnoreCase("dark"));

        // theme observation
        SceneManager.getThemeProperty().addListener((observable, oldValue, newValue) -> {
            SceneManager.activateTheme(SceneManager.SceneLevel.LOGIN.cssId);
        });

        // theme changer
        themeToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            String val = newValue ? "DARK" : "LIGHT";
            SceneManager.setThemeProperty(SceneManager.Theme.valueOf(val));
        });
    }

    @FXML
    void forgotPasswordPressed(ActionEvent event) {
        SceneManager.showNotification("Info", "Email service not available", 3);
    }

    @FXML
    void loginPressed(ActionEvent event) {
        String email = emailText.getText();
        String password = passwordText.getText();
        new Thread(() -> {
            try {
                loginRequest(email, password);
            } catch (IOException | NotAcceptableException e) {
                Platform.runLater(() -> {
                    SceneManager.showNotification("Failure", "Something went wrong!", 3);
                });
            } catch (UnauthorizedException e) {
                Platform.runLater(() -> {
                    SceneManager.showNotification("Failure", e.getMessage(), 3);
                });
            }
        }).start();
    }

    @FXML
    void signupPressed(ActionEvent event) {
        SceneManager.setScene(SceneManager.SceneLevel.SIGNUP);
    }

    public static void loginRequest(String email, String password) throws IOException, NotAcceptableException, UnauthorizedException {
        JSONObject headers = new JSONObject();
        headers.put("Content-Type", "application/json");
        HttpURLConnection con = RequestBuilder.buildConnection("POST", "users/login",
                headers, true);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", email);
        jsonObject.put("password", password);
        OutputStream outputStream = con.getOutputStream();
        JsonHandler.sendObject(outputStream, jsonObject);
        outputStream.close();

        if (con.getResponseCode() / 100 == 2) {
            InputStream inputStream = con.getInputStream();
            JSONObject jsonObject1 = JsonHandler.getObject(inputStream);

            DataAccess.setJWT(jsonObject1.getString("Authorization"));
            DataAccess.setUserId(jsonObject1.getString("userId"));
            DataAccess.setFullName(jsonObject1.getString("fullName"));
            Platform.runLater(()->{
                SceneManager.setScene(SceneManager.SceneLevel.MAIN);
                SceneManager.showNotification("Success", "Logged in. Welcome back, " + DataAccess.getFullName() + ".", 3);
            });
        } else if (con.getResponseCode() == 401) {
            throw new UnauthorizedException("Invalid email or password");
        } else {
            throw new NotAcceptableException("Unknown");
        }
    }
}
