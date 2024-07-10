package org.aut.dataAccessors;

import org.aut.models.Profile;
import org.aut.utils.JsonHandler;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.json.JSONObject;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;

public class ProfileAccessor {
    private static final Connection connection = DataBaseAccessor.getConnection();

    private ProfileAccessor() {
    }

    static void createTable() throws IOException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS profiles (" +
                    "userId TEXT NOT NULL" +
                    ", bio TEXT" +
                    ", country TEXT" +
                    ", city TEXT" +
                    ", status TEXT" +
                    ", profession TEXT" +
                    ", notify INT" +
                    ", FOREIGN KEY (userId)" +
                    " REFERENCES users (userId)" +
                    " ON UPDATE CASCADE" +
                    " ON DELETE CASCADE" +
                    ");");
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    public synchronized static void addProfile(Profile profile) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO profiles (userId, bio, country, city, status, profession, notify) " +
                "VALUES (?,?,?,?,?,?,?);");

        statement.setString(1, profile.getUserId());
        statement.setString(2, profile.getBio());
        statement.setString(3, profile.getCountry());
        statement.setString(4, profile.getCity());
        statement.setString(5, profile.getStatus());
        statement.setString(6, profile.getProfession());
        statement.setInt(7, profile.getNotify());

        statement.executeUpdate();
        statement.close();
    }

    public synchronized static Profile getProfile(String userId) throws SQLException, NotFoundException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM profiles WHERE userId = ?;");
        return getProfileFromResultSet(userId, statement);
    }

    public synchronized static void updateProfile(Profile profile) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE profiles SET bio = ?, " +
                "country = ?, city = ?, status = ?, profession = ?, notify = ? WHERE userId = ?;");

        statement.setString(1, profile.getBio());
        statement.setString(2, profile.getCountry());
        statement.setString(3, profile.getCity());
        statement.setString(4, profile.getStatus());
        statement.setString(5, profile.getProfession());
        statement.setInt(6, profile.getNotify());
        statement.setString(7, profile.getUserId());

        statement.executeUpdate();
        statement.close();
    }

    private static Profile getProfileFromResultSet(String input, PreparedStatement statement) throws SQLException, NotFoundException {
        statement.setString(1, input);
        ResultSet resultSet = statement.executeQuery();
        JSONObject jsonObject = JsonHandler.getFromResultSet(resultSet);
        resultSet.close();
        statement.close();
        if (jsonObject == null) {
            throw new NotFoundException("Profile not Found");
        } else {
            Profile profile;
            try {
                profile = new Profile(jsonObject);
            } catch (NotAcceptableException e) {
                throw new NotFoundException("Profile not found.");
            }
            return profile;
        }
    }
}
