package org.aut.polylinked_client.control;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import org.aut.polylinked_client.SceneManager;
import org.aut.polylinked_client.model.*;
import org.aut.polylinked_client.utils.DataAccess;
import org.aut.polylinked_client.utils.JsonHandler;
import org.aut.polylinked_client.utils.RequestBuilder;
import org.aut.polylinked_client.utils.exceptions.NotAcceptableException;
import org.json.JSONObject;
import org.aut.polylinked_client.utils.exceptions.UnauthorizedException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class EditProfileController {
    private final static String fileId = "editProfile";
    private final BooleanProperty switched = new SimpleBooleanProperty(false);

    private User user;
    private Education education;
    private Skill skill;

    @FXML
    private JFXTextArea aboutTA;

    @FXML
    private JFXTextArea activitiesTA;

    @FXML
    private JFXTextField additionalNameTF;

    @FXML
    private JFXTextArea addressTA;

    @FXML
    private JFXTextArea bioTA;

    @FXML
    private DatePicker birthdayDP;

    @FXML
    private Button cancelBtn;

    @FXML
    private JFXTextField cityTF;

    @FXML
    private JFXPasswordField confirmPasswordTF;

    @FXML
    private JFXComboBox<String> countryCB;

    @FXML
    private JFXTextField emailTF;

    @FXML
    private DatePicker endDateDP;

    @FXML
    private JFXTextField fieldTF;

    @FXML
    private JFXTextField firstNameTF;

    @FXML
    private JFXTextField gradeTF;

    @FXML
    private JFXTextField homeNumberTF;

    @FXML
    private JFXTextField instituteTF;

    @FXML
    private JFXTextField lastNameTF;

    @FXML
    private JFXTextField mobileNumberTF;

    @FXML
    private JFXPasswordField passwordTF;

    @FXML
    private JFXComboBox<String> privacyCB;

    @FXML
    private JFXComboBox<String> professionCB;

    @FXML
    private BorderPane root;

    @FXML
    private Button saveBtn;

    @FXML
    private JFXTextArea skillTA;

    @FXML
    private JFXTextField socialMediaTF;

    @FXML
    private DatePicker startDateDP;

    @FXML
    private JFXComboBox<String> statusCB;

    @FXML
    private JFXTextField workNumberTF;

    @FXML
    void initialize() {
        SceneManager.activateTheme(root, fileId);
        SceneManager.getThemeProperty().addListener((observable, oldValue, newValue) -> {
            SceneManager.activateTheme(root, fileId);
        });

        countryCB.setItems(FXCollections.observableList(List.of("IRAN", "USA", "UK", "TURKEY", "IRAQ")));
        countryCB.setValue("IRAN");

        professionCB.setItems(FXCollections.observableList(List.of("DOCTOR", "TEACHER", "ENGINEER", "LAWYER", "MECHANIC")));
        professionCB.setValue("ENGINEER");

        statusCB.setItems(FXCollections.observableList(List.of("RECRUITER", "SERVICE_PROVIDER", "JOB_SEARCHER")));
        statusCB.setValue("JOB_SEARCHER");

        privacyCB.setItems(FXCollections.observableList(List.of("ONLY_ME", "MY_CONNECTIONS", "FURTHER_CONNECTIONS", "EVERYONE")));
        privacyCB.setValue("EVERYONE");
    }

    public void setData(User user, Profile profile, CallInfo callInfo, Education education, Skill skill) {
        if (user == null) return;

        this.user = user;
        firstNameTF.setText(user.getFirstName());
        lastNameTF.setText(user.getLastName());
        additionalNameTF.setText(user.getAdditionalName());
        emailTF.setText(user.getEmail());
        passwordTF.setText(user.getPassword());

        if (profile != null) {
            bioTA.setText(profile.getBio());
            countryCB.setValue(profile.getCountry());
            cityTF.setText(profile.getCity());
            professionCB.setValue(profile.getProfession());
            statusCB.setValue(profile.getStatus());
        }

        if (callInfo != null) {
            mobileNumberTF.setText(callInfo.getMobileNumber());
            homeNumberTF.setText(callInfo.getHomeNumber());
            workNumberTF.setText(callInfo.getWorkNumber());
            addressTA.setText(callInfo.getAddress());
            birthdayDP.setValue(LocalDate.ofInstant(Instant.ofEpochMilli(callInfo.getBirthDay()), ZoneId.systemDefault()));
            socialMediaTF.setText(callInfo.getSocialMedia());
            privacyCB.setValue(callInfo.getPrivacyPolitics());
        }

        if (education != null) {
            this.education = education;
            instituteTF.setText(education.getInstitute());
            fieldTF.setText(education.getField());
            startDateDP.setValue(Instant.ofEpochMilli(education.getStart()).atZone(ZoneId.systemDefault()).toLocalDate());
            endDateDP.setValue(Instant.ofEpochMilli(education.getEnd()).atZone(ZoneId.systemDefault()).toLocalDate());
            gradeTF.setText(String.valueOf(education.getGrade()));
            activitiesTA.setText(education.getActivities());
            aboutTA.setText(education.getAbout());
        }

        if (skill != null) {
            this.skill = skill;
            skillTA.setText(skill.getText());
        }
    }

    @FXML
    void cancelBtnPressed(ActionEvent event) {
        switched.set(true);
    }

    @FXML
    void saveBtnPressed(ActionEvent event) {
        if (user == null) {
            SceneManager.showNotification("Info", "Not Found!", 3);
            switched.set(true);
            return;
        }

        if (personalChanged()) {
            if (firstNameTF.getText().isEmpty() || lastNameTF.getText().isEmpty() || (emailTF.getText().isEmpty()) || passwordTF.getText().isEmpty() || confirmPasswordTF.getText().isEmpty()) {
                SceneManager.showNotification("Info", "Please fill all the fields of personal section.", 3);
                return;
            }

            if (!emailTF.getText().matches("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")) {
                SceneManager.showNotification("Info", "Invalid email address.", 3);
                return;
            }

            if (!additionalNameTF.getText().matches("(?i)^[a-z]{1,20}$") || additionalNameTF.getText().isEmpty()) {
                SceneManager.showNotification("Info", "Additional name must be a maximum of 20 characters and consist of only letters.", 3);
                return;
            }

            if (!firstNameTF.getText().matches("(?i)^[a-z]{1,20}$")) {
                SceneManager.showNotification("Info", "First name must be a maximum of 20 characters and consist of only letters.", 3);
                return;
            }

            if (!lastNameTF.getText().matches("(?i)^[a-z]{1,40}$")) {
                SceneManager.showNotification("Info", "Last name must be a maximum of 40 characters and consist of only letters.", 3);
                return;
            }

            if (!(passwordTF.getText().matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$"))) {
                SceneManager.showNotification("Info", "The password must contain 8-20 characters, at least one letter and one digit.", 3);
                return;
            }

            if (!passwordTF.getText().equals(confirmPasswordTF.getText())) {
                SceneManager.showNotification("Info", "Passwords don't match.", 3);
                return;
            }

        } else if (!confirmPasswordTF.getText().equals(passwordTF.getText())) {
            SceneManager.showNotification("Info", "Passwords don't match.", 3);
            return;
        }

        user.setFirstName(firstNameTF.getText());
        user.setLastName(lastNameTF.getText());
        user.setAdditionalName(additionalNameTF.getText());
        user.setEmail(emailTF.getText());
        user.setPassword(passwordTF.getText());

        JSONObject header = JsonHandler.createJson("Authorization", DataAccess.getJWT());
        new Thread(() -> {
            try {
                RequestBuilder.sendJsonRequest("PUT", "users", header, user.toJson());
                Platform.runLater(() -> {
                    SceneManager.showNotification("Success", "Personal info Added.", 3);
                });
            } catch (UnauthorizedException e) {
                Platform.runLater(() -> {
                    SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                    SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                });
            } catch (NotAcceptableException e) {
                Platform.runLater(() -> {
                    SceneManager.showNotification("Failure", "Request failed. Try Again", 3);
                });
            }
        }).start();

        AtomicBoolean bool = new AtomicBoolean(false);
        new Thread(() -> {
            try {
                if (isProfileFilled()) {
                    Profile newProfile = new Profile(user.getUserId(), bioTA.getText(), countryCB.getValue(),
                            cityTF.getText(), Profile.Status.valueOf(statusCB.getValue()),
                            Profile.Profession.valueOf(professionCB.getValue()), 1);

                    RequestBuilder.sendJsonRequest("POST", "users/profiles", header, newProfile.toJson());
                    bool.set(true);
                }
            } catch (UnauthorizedException e) {
                Platform.runLater(() -> {
                    SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                    SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                });
            } catch (NotAcceptableException e) {
                Platform.runLater(() -> {
                    SceneManager.showNotification("Failure", "Request failed. Try Again", 3);
                });
            }
        }).start();

        new Thread(() -> {
            try {
                if (isCallInfoFilled()) {
                    CallInfo newCallInfo = new CallInfo(user.getUserId(), user.getEmail(),
                            mobileNumberTF.getText(), homeNumberTF.getText(), workNumberTF.getText(), addressTA.getText(),
                            Date.from(birthdayDP.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                            CallInfo.PrivacyPolitics.valueOf(privacyCB.getValue()), socialMediaTF.getText());

                    RequestBuilder.sendJsonRequest("POST", "users/callInfo", header, newCallInfo.toJson());
                    bool.set(true);
                }
            } catch (UnauthorizedException e) {
                Platform.runLater(() -> {
                    SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                    SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                });
            } catch (NotAcceptableException e) {
                Platform.runLater(() -> {
                    SceneManager.showNotification("Failure", "Request failed. Try Again", 3);
                });
            }
        }).start();

        new Thread(() -> {
            try {
                if (isEducationFilled()) {
                    Education newEducation;
                    if (education == null) {
                        newEducation = new Education(user.getUserId(), instituteTF.getText(), fieldTF.getText(),
                                Date.from(startDateDP.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                                Date.from(startDateDP.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                                Integer.parseInt(gradeTF.getText()), activitiesTA.getText(), aboutTA.getText());
                    } else {
                        newEducation = new Education(this.education.getEducationId(), user.getUserId(), instituteTF.getText(), fieldTF.getText(),
                                Date.from(startDateDP.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                                Date.from(startDateDP.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                                Integer.parseInt(gradeTF.getText()), activitiesTA.getText(), aboutTA.getText());
                    }
                    RequestBuilder.sendJsonRequest("POST", "users/educations", header, newEducation.toJson());

                    if (skillTA.getText() != null) {
                        Skill newSkill;
                        if (skill == null) {
                            newSkill = new Skill(user.getUserId(), newEducation.getEducationId(), skillTA.getText());
                        } else {
                            newSkill = new Skill(this.skill.getSkillId(), user.getUserId(), newEducation.getEducationId(), skillTA.getText());
                        }

                        RequestBuilder.sendJsonRequest("POST", "users/skills", header, newSkill.toJson());
                    }
                    bool.set(true);
                }
            } catch (UnauthorizedException e) {
                Platform.runLater(() -> {
                    SceneManager.setScene(SceneManager.SceneLevel.LOGIN);
                    SceneManager.showNotification("Info", "Your Authorization has failed or expired.", 3);
                });
            } catch (NotAcceptableException e) {
                Platform.runLater(() -> {
                    SceneManager.showNotification("Failure", "Request failed. Try Again", 3);
                });
            }
        }).start();

        if (bool.get()) SceneManager.showNotification("Success", "Profile updated.", 3);
        switched.set(true);
    }

    private boolean isEducationFilled() {
        return instituteTF.getText() != null && fieldTF.getText() != null &&
                startDateDP.getValue() != null && endDateDP.getValue() != null && gradeTF.getText() != null &&
                activitiesTA.getText() != null && aboutTA.getText() != null &&
                skillTA.getText() != null;
    }

    private boolean isProfileFilled() {
        return bioTA.getText() != null || countryCB.getValue() != null ||
                cityTF.getText() != null || professionCB != null ||
                statusCB.getValue() != null;
    }

    private boolean isCallInfoFilled() {
        return mobileNumberTF.getText() != null && homeNumberTF.getText() != null &&
                workNumberTF.getText() != null && addressTA.getText() != null &&
                birthdayDP.getValue() != null && socialMediaTF.getText() != null;
    }

    private boolean personalChanged() {
        return !(firstNameTF.getText().equals(user.getFirstName()) && lastNameTF.getText().equals(user.getLastName()) &&
                additionalNameTF.getText().equals(user.getAdditionalName()) && emailTF.getText().equals(user.getEmail()) &&
                passwordTF.getText().equals(user.getPassword()));
    }

    public BooleanProperty isSwitched() {
        return switched;
    }
}
