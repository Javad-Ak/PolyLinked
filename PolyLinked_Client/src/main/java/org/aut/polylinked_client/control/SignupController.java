package org.aut.polylinked_client.control;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.aut.polylinked_client.SceneManager;
import org.aut.polylinked_client.model.User;
import org.aut.polylinked_client.utils.DataAccess;
import org.aut.polylinked_client.utils.JsonHandler;
import org.aut.polylinked_client.utils.RequestBuilder;
import org.aut.polylinked_client.utils.exceptions.NotAcceptableException;
import org.aut.polylinked_client.utils.exceptions.UnauthorizedException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class SignupController {
    @FXML
    private JFXTextField firstNameTF;

    @FXML
    private JFXTextField lastNameTF;

    @FXML
    private JFXTextField additionalNameTF;

    @FXML
    private JFXTextField emailTF;

    @FXML
    private JFXPasswordField passwordTF;

    @FXML
    private JFXPasswordField confirmPasswordTF;

    @FXML
    private JFXToggleButton themeToggle;

    @FXML
    void initialize() {
        String theme = DataAccess.getTheme();
        themeToggle.setSelected(theme.equalsIgnoreCase("dark"));

        // theme changer
        themeToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            String val = newValue ? "DARK" : "LIGHT";
            SceneManager.setThemeProperty(SceneManager.Theme.valueOf(val));
        });

        // theme observation
        SceneManager.getThemeProperty().addListener((observable, oldValue, newValue) -> {
            SceneManager.activateTheme(SceneManager.SceneLevel.LOGIN.cssId);
        });
    }

    @FXML
    void loginPressed(ActionEvent event) {
        SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
    }

    @FXML
    void signupPressed(ActionEvent event) {
        if (firstNameTF.getText().isEmpty() || lastNameTF.getText().isEmpty() || (emailTF.getText().isEmpty()) || passwordTF.getText().isEmpty() || confirmPasswordTF.getText().isEmpty()) {
            SceneManager.showNotification("Info","Please fill all the fields.", 3);
            return;
        }

        if (!firstNameTF.getText().matches("(?i)^[a-z]{1,20}$")) {
            SceneManager.showNotification("Info","First name must be a maximum of 20 characters and consist of only letters.", 3);
            return;
        }
        if (!lastNameTF.getText().matches("(?i)^[a-z]{1,40}$")) {
            SceneManager.showNotification("Info","Last name must be a maximum of 40 characters and consist of only letters.", 3);
            return;
        }

        if (!additionalNameTF.getText().matches("(?i)^[a-z]{1,20}$") || additionalNameTF.getText().isEmpty()) {
            SceneManager.showNotification("Info","Additional name must be a maximum of 20 characters and consist of only letters.", 3);
            return;
        }

        if (!emailTF.getText().matches("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")) {
            SceneManager.showNotification("Info","Invalid email address.", 3);
            return;
        }


        if (!(passwordTF.getText().matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$"))) {
            SceneManager.showNotification("Info","The password must contain 8-20 characters, at least one letter and one digit.", 3);
            return;
        }

        if (!passwordTF.getText().equals(confirmPasswordTF.getText())) {
            SceneManager.showNotification("Info","Password is wrong.", 3);
            return;
        }
        User user;
        try {
            user = new User(emailTF.getText(), passwordTF.getText(), firstNameTF.getText(), lastNameTF.getText(), additionalNameTF.getText());
        } catch (Exception e) {
            SceneManager.showNotification("Info","Something went wrong.", 3);
            return;
        }
        firstNameTF.clear();
        lastNameTF.clear();
        additionalNameTF.clear();
        passwordTF.clear();
        confirmPasswordTF.clear();
        emailTF.clear();

        new Thread(()->{
            try {
                JSONObject headers = new JSONObject();
                headers.put("Content-Type", "application/json");
                HttpURLConnection con = RequestBuilder.buildConnection("POST", "users",
                        headers, true);
                JSONObject jsonObj = user.toJson();
                OutputStream os = con.getOutputStream();
                JsonHandler.sendObject(os, jsonObj);
                os.close();
                if (con.getResponseCode() / 100 == 2) {
                    Platform.runLater(()->{
                        SceneManager.showNotification("Info","Congratulations! You have successfully signed up!.", 3);
                    });
                } else {
                    throw new NotAcceptableException("Invalid signup request");
                }
                LoginController.loginRequest(user.getEmail(), user.getPassword());
            } catch (IOException | NotAcceptableException e) {
                Platform.runLater(()->{
                    SceneManager.showNotification("Info","Something went wrong.", 3);
                });
            } catch (UnauthorizedException e) {
                Platform.runLater(()->{
                    SceneManager.showNotification("Info",e.getMessage(), 3);
                });
            }
        }).start();
    }
}