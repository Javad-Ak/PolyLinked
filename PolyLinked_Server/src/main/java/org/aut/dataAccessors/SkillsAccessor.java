package org.aut.dataAccessors;

import org.aut.models.Education;
import org.aut.models.Skill;
import org.aut.utils.JsonHandler;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;

public class SkillsAccessor {
    private static final Connection connection = DataBaseAccessor.getConnection();

    private SkillsAccessor() {
    }

    static void createTable() throws IOException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS skills (" +
                    "skillId TEXT NOT NULL " +
                    ", userId TEXT NOT NULL REFERENCES users (userId) ON DELETE CASCADE ON UPDATE CASCADE " +
                    ", educationId TEXT NOT NULL REFERENCES educations (educationId) ON UPDATE CASCADE ON DELETE CASCADE " +
                    ", text TEXT NOT NULL" +
                    ", PRIMARY KEY (skillId)" +
                    ");");
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    public synchronized static void addSkill(Skill skill) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO skills (skillId, userId, educationId, text) " +
                "VALUES (?,?,?,?);");

        statement.setString(1, skill.getSkillId());
        statement.setString(2, skill.getUserId());
        statement.setString(3, skill.getEducationId());
        statement.setString(4, skill.getText());

        statement.executeUpdate();
        statement.close();
    }

    public synchronized static void updateSkill(Skill skill) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("Update skills SET userId=?, educationId=?, text=? where skillId=?;");

        statement.setString(1, skill.getUserId());
        statement.setString(2, skill.getEducationId());
        statement.setString(3, skill.getText());
        statement.setString(4, skill.getSkillId());

        statement.executeUpdate();
        statement.close();
    }

    public synchronized static void deleteSkill(String id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM skills WHERE skillId = ?;");
        statement.setString(1, id);

        statement.executeUpdate();
        statement.close();
    }

    public synchronized static boolean skillExists(String id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM skills WHERE skillId = ?;");
        statement.setString(1, id);

        ResultSet set = statement.executeQuery();
        boolean bool = set.next();
        set.close();
        statement.close();
        return bool;
    }

    public synchronized static Skill getSkill(String id) throws SQLException, NotFoundException, NotAcceptableException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM skills WHERE skillId = ?;");
        statement.setString(1, id);

        ResultSet set = statement.executeQuery();
        JSONObject obj = JsonHandler.getFromResultSet(set);
        statement.close();

        if (obj == null || obj.isEmpty()) throw new NotFoundException("Not Found");
        return new Skill(obj);
    }

    public synchronized static ArrayList<Skill> getSkillsOfEducation(String eduId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM skills WHERE educationId = ?;");
        return getSkills(eduId, statement);
    }

    public synchronized static ArrayList<Skill> getSkillsOfUser(String userId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM skills WHERE userId = ?;");
        return getSkills(userId, statement);
    }

    @NotNull
    static ArrayList<Skill> getSkills(String userId, PreparedStatement statement) throws SQLException {
        statement.setString(1, userId);

        ResultSet set = statement.executeQuery();
        ArrayList<JSONObject> objects = JsonHandler.getArrayFromResultSet(set);
        statement.close();

        ArrayList<Skill> skills = new ArrayList<>();
        for (JSONObject obj : objects) {
            try {
                skills.add(new Skill(obj));
            } catch (NotAcceptableException ignored) {
            }
        }
        return skills;
    }
}
