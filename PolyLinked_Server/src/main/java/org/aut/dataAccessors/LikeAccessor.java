package org.aut.dataAccessors;

import org.aut.models.Like;
import org.aut.models.User;
import org.aut.utils.JsonHandler;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.json.JSONObject;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;

public class LikeAccessor {
    private static final Connection connection = DataBaseAccessor.getConnection();

    private LikeAccessor() {
    }

    static void createTable() throws IOException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS likes (" +
                    "postId TEXT NOT NULL REFERENCES posts (postId) ON UPDATE CASCADE ON DELETE CASCADE" +
                    ", userId TEXT NOT NULL REFERENCES users (userId) ON UPDATE CASCADE ON DELETE CASCADE" +
                    ", date BIGINT NOT NULL" +
                    ");");
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    public synchronized static void addLike(Like like) throws SQLException, NotAcceptableException {
        try {
            getLike(like.getPostId(), like.getUserId());
            throw new NotAcceptableException("Like already exists.");
        } catch (NotFoundException e) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO likes (postId, userId, date) " +
                    "VALUES (?,?,?);");
            statement.setString(1, like.getPostId());
            statement.setString(2, like.getUserId());
            statement.setLong(3, like.getDate());
            statement.executeUpdate();
            statement.close();
        }
    }

    public synchronized static void deleteLike(String postId, String userId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM likes WHERE postId = ? AND userId = ?;");
        statement.setString(1, postId);
        statement.setString(2, userId);
        statement.executeUpdate();
        statement.close();
    }

    public synchronized static Like getLike(String postId, String userId) throws SQLException, NotFoundException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM likes WHERE postId = ? AND userId = ?;");
        statement.setString(1, postId);
        statement.setString(2, userId);
        Like like = getLikeFromResultSet(statement.executeQuery());
        statement.close();
        return like;
    }

    public synchronized static int countPostLikes(String postId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM likes WHERE postId = ?;");
        statement.setString(1, postId);
        ResultSet resultSet = statement.executeQuery();
        int count = resultSet.getInt(1);

        statement.close();
        resultSet.close();
        return count;
    }

    public synchronized static ArrayList<User> getLikersOfPost(String postId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE userId = (SELECT likes.userId FROM likes where postId = ?);");
        statement.setString(1, postId);
        return UserAccessor.getUserArrayFromSet(statement);
    }

    private static Like getLikeFromResultSet(ResultSet resultSet) throws SQLException, NotFoundException {
        JSONObject jsonObject = JsonHandler.getFromResultSet(resultSet);
        resultSet.close();
        if (jsonObject == null) {
            throw new NotFoundException("User not Found");
        } else {
            Like like;
            try {
                like = new Like(jsonObject);
            } catch (NotAcceptableException e) {
                throw new NotFoundException("User not Found");
            }
            return like;
        }
    }
}
