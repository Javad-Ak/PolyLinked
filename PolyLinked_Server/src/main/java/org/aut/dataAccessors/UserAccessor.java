package org.aut.dataAccessors;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;

import org.aut.models.User;
import org.aut.utils.JsonHandler;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class UserAccessor {
    private static final Connection connection = DataBaseAccessor.getConnection();

    private UserAccessor() {
    }

    static void createUserTable() throws IOException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "userId TEXT NOT NULL" +
                    ", email TEXT PRIMARY KEY NOT NULL" +
                    ", password VARCHAR(20) NOT NULL" +
                    ", firstName VARCHAR(20) NOT NULL " +
                    ", lastName VARCHAR(40) NOT NULL" +
                    ", additionalName VARCHAR(20)" +
                    ", createDate BIGINT(20)" +
                    ");");
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    public synchronized static void addUser(User user) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO users (userId, email, password, firstName, LastName, additionalName, createDate) " +
                "VALUES (?,?,?,?,?,?,?);");
        statement.setString(1, user.getUserId());
        statement.setString(2, user.getEmail());
        statement.setString(3, user.getPassword());
        statement.setString(4, user.getFirstName());
        statement.setString(5, user.getLastName());
        statement.setString(6, user.getAdditionalName());
        statement.setLong(7, user.getCreateDate().getTime());
        statement.executeUpdate();
        statement.close();
    }
    public synchronized static void deleteUser(String userId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE userId = ?;");
        statement.setString(1, userId);
        statement.executeUpdate();
        statement.close();
    }


    public synchronized static User getUserByEmail(String email) throws SQLException, NotFoundException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE email = ?;");
        return getUserFromResultSet(email, statement);
    }

    public synchronized static User getUserById(String id) throws SQLException, NotFoundException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE userId = ?;");
        return getUserFromResultSet(id, statement);
    }
    public synchronized static void updateUser(User user) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE users SET email = ? , password = ? , firstName = ? , lastName = ? , additionalName = ?  WHERE userId = ?;");
        statement.setString(1, user.getEmail());
        statement.setString(2, user.getPassword());
        statement.setString(3, user.getFirstName());
        statement.setString(4, user.getLastName());
        statement.setString(5, user.getAdditionalName());
        statement.setString(6, user.getUserId());
        statement.executeUpdate();
        statement.close();
    }

    public synchronized static ArrayList<User> getAllUsers() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM users;");
        return getUserArrayFromSet(statement);
    }

    @NotNull
    static ArrayList<User> getUserArrayFromSet(PreparedStatement statement) throws SQLException {
        ArrayList<JSONObject> array = JsonHandler.getArrayFromResultSet(statement.executeQuery());
        statement.close();

        ArrayList<User> users = new ArrayList<>();
        for (JSONObject object : array) {
            try {
                users.add(new User(object));
            } catch (NotAcceptableException ignored) {
            }
        }
        return users;
    }

    @NotNull
    private static User getUserFromResultSet(String input, PreparedStatement statement) throws SQLException, NotFoundException {
        statement.setString(1, input);
        ResultSet resultSet = statement.executeQuery();
        JSONObject jsonObject = JsonHandler.getFromResultSet(resultSet);
        resultSet.close();
        statement.close();
        if (jsonObject == null) {
            throw new NotFoundException("User not Found");
        } else {
            User user;
            try {
                user = new User(jsonObject);
            } catch (NotAcceptableException e) {
                throw new NotFoundException("User not Found");
            }
            return user;
        }
    }
}
