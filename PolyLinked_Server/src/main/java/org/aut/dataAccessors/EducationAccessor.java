package org.aut.dataAccessors;

import org.aut.models.Education;
import org.aut.utils.JsonHandler;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.json.JSONObject;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;

public class EducationAccessor {
    private static final Connection connection = DataBaseAccessor.getConnection();

    private EducationAccessor() {
    }

    static void createTable() throws IOException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS educations (" +
                    "educationId TEXT NOT NULL" +
                    ", userId TEXT NOT NULL" +
                    ", institute TEXT NOT NULL" +
                    ", field TEXT NOT NULL" +
                    ", start BIGINT NOT NULL" +
                    ", end BIGINT NOT NULL" +
                    ", grade INT NOT NULL" +
                    ", activities TEXT NOT NULL" +
                    ", about TEXT NOT NULL" +
                    ", PRIMARY KEY (educationId)" +
                    ", FOREIGN KEY (userId)" +
                    " REFERENCES users (userId)" +
                    " ON UPDATE CASCADE" +
                    " ON DELETE CASCADE" +
                    ");");
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    public synchronized static void addEducation(Education education) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO educations (educationId, userId, institute, field, start, end, grade, activities, about) " +
                "VALUES (?,?,?,?,?,?,?,?,?);");

        statement.setString(1, education.getEducationId());
        statement.setString(2, education.getUserId());
        statement.setString(3, education.getInstitute());
        statement.setString(4, education.getField());
        statement.setLong(5, education.getStart());
        statement.setLong(6, education.getEnd());
        statement.setInt(7, education.getGrade());
        statement.setString(8, education.getActivities());
        statement.setString(9, education.getAbout());

        statement.executeUpdate();
        statement.close();
    }

    public synchronized static void updateEducation(Education education) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE educations SET userId=?, institute=?, field=?, start=?, end=?, grade=?, activities=?, about=? WHERE educationId = ?;");

        statement.setString(1, education.getUserId());
        statement.setString(2, education.getInstitute());
        statement.setString(3, education.getField());
        statement.setLong(4, education.getStart());
        statement.setLong(5, education.getEnd());
        statement.setInt(6, education.getGrade());
        statement.setString(7, education.getActivities());
        statement.setString(8, education.getAbout());
        statement.setString(9, education.getEducationId());

        statement.executeUpdate();
        statement.close();
    }

    public synchronized static void deleteEducation(String id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM educations WHERE educationId = ?;");
        statement.setString(1, id);

        statement.executeUpdate();
        statement.close();
    }

    public synchronized static boolean educationExists(String id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM educations WHERE educationId = ?;");
        statement.setString(1, id);

        ResultSet set = statement.executeQuery();
        boolean bool = set.next();
        set.close();
        statement.close();
        return bool;
    }

    public synchronized static Education getEducation(String id) throws SQLException, NotFoundException, NotAcceptableException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM educations WHERE educationId = ?;");
        statement.setString(1, id);

        ResultSet set = statement.executeQuery();
        JSONObject obj = JsonHandler.getFromResultSet(set);
        statement.close();

        if (obj == null || obj.isEmpty()) throw new NotFoundException("Not Found");
        return new Education(obj);
    }

    public synchronized static ArrayList<Education> getEducationsOf(String userId) throws SQLException, NotFoundException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM educations WHERE userId = ?;");
        statement.setString(1, userId);

        ResultSet set = statement.executeQuery();
        ArrayList<JSONObject> objects = JsonHandler.getArrayFromResultSet(set);
        statement.close();

        ArrayList<Education> educations = new ArrayList<>();
        for (JSONObject obj : objects) {
            try {
                educations.add(new Education(obj));
            } catch (NotAcceptableException ignored) {
            }
        }
        return educations;
    }
}
