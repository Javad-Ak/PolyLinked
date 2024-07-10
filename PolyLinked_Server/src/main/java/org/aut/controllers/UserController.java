package org.aut.controllers;

import org.aut.dataAccessors.UserAccessor;
import org.aut.models.User;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserController {
    private UserController() {
    }

    public static void addUser(User user) throws SQLException, NotAcceptableException {
        if (!userExistsByEmail(user.getEmail())) {
            UserAccessor.addUser(user);
        } else {
            throw new NotAcceptableException("User already exists");
        }

    }
    public static void deleteUser(User user) throws SQLException, NotFoundException {
        if (userExistsByEmail(user.getEmail())) {
            UserAccessor.deleteUser(user.getUserId());
        } else {
            throw new NotFoundException("User Not Found");
        }
    }

    public static boolean userExistsByEmail(String email) throws SQLException {
        try {
            UserAccessor.getUserByEmail(email);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    public static boolean userExistsById(String id) throws SQLException {
        try {
            UserAccessor.getUserById(id);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    public static void updateUser(User user) throws SQLException, NotFoundException {
        if (userExistsById(user.getUserId())) {
            UserAccessor.updateUser(user);
        } else {
            throw new NotFoundException("User not found");
        }
    }

    public static boolean authenticate(String email, String password) throws SQLException, NotFoundException {
        User user = UserAccessor.getUserByEmail(email);
        return user.getPassword().equals(password);
    }

    public static List<User> searchUsers(String input) throws SQLException {
        ArrayList<User> users = UserAccessor.getAllUsers();
        users.removeIf(user -> !(user.getFirstName().toLowerCase().contains(input.toLowerCase())
        || user.getLastName().toLowerCase().contains(input.toLowerCase())
        || user.getEmail().toLowerCase().contains(input.toLowerCase())
        || user.getAdditionalName().toLowerCase().contains(input.toLowerCase())
        || input.toLowerCase().contains(user.getFirstName().toLowerCase())
        || input.toLowerCase().contains(user.getLastName().toLowerCase())
        || input.toLowerCase().contains(user.getEmail().toLowerCase())
        || input.toLowerCase().contains(user.getAdditionalName().toLowerCase())));
        return users;
    }
}
