package org.aut.models;

import java.util.Date;
import java.util.Random;

import org.aut.utils.exceptions.NotAcceptableException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class User implements MediaLinked {
    private final String userId; // UUID
    private String email; // valid
    private String password; // > 7 ch, int
    private String firstName; // 20 ch
    private String lastName; // 40 ch
    private String additionalName; // 20 ch
    private Date createDate;

    public User(String email, String password, String firstName, String lastName, String additionalName) throws NotAcceptableException {
        String id = "user" + new Random().nextInt(99999) + UUID.randomUUID().toString().substring(10, 23);

        validateFields(email, password, firstName, lastName, additionalName);
        this.userId = id.trim();
        this.email = email.trim();
        this.password = password.trim();
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.additionalName = additionalName.trim();
        createDate = new Date(System.currentTimeMillis());
    }

    public User(JSONObject json) throws NotAcceptableException {
        try {
            validateFields(json.getString("email"), json.getString("password"), json.getString("firstName"), json.getString("lastName"), json.getString("additionalName"));
            userId = json.getString("userId").trim();
            email = json.getString("email").trim();
            password = json.getString("password").trim();
            firstName = json.getString("firstName").trim();
            lastName = json.getString("lastName").trim();
            additionalName = json.getString("additionalName").trim();
            createDate = new Date(json.getLong("createDate"));
        } catch (JSONException e) {
            throw new NotAcceptableException("Wrong jsonObject");

        }
    }

    @Override
    public String toString() {
        return "{" +
                "userId:" + userId +
                ", email:" + email +
                ", password:" + password +
                ", firstName:" + firstName +
                ", lastName:" + lastName +
                ", additionalName:" + additionalName +
                ", createDate:" + createDate.getTime() +
                '}';
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", userId);
        jsonObject.put("email", email);
        jsonObject.put("password", password);
        jsonObject.put("firstName", firstName);
        jsonObject.put("lastName", lastName);
        jsonObject.put("additionalName", additionalName);
        jsonObject.put("createDate", createDate.getTime());
        return jsonObject;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User user) {
            return userId.equals(user.userId);
        } else return false;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getUserId() {
        return userId;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getAdditionalName() {
        return additionalName;
    }

    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public String getMediaURL() {
        return MediaLinked.SERVER_PREFIX + "profiles/" + userId;
    }

    private static void validateFields(String email, String password, String firstName, String lastName, String additionalName) throws NotAcceptableException {
        if ((firstName == null || lastName == null || email == null || password == null || additionalName == null) ||
                (!email.matches("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")) ||
                (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$")) ||
                (!firstName.matches("(?i)^[a-z]{1,20}$")) ||
                (!lastName.matches("(?i)^[a-z]{1,40}$")) ||
                (!additionalName.matches("(?i)^[a-z]{0,20}$")))
            throw new NotAcceptableException("invalid argument");
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
