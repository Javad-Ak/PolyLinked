package org.aut.dataAccessors;

import org.aut.models.Comment;
import org.aut.models.Post;
import org.aut.utils.JsonHandler;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.json.JSONObject;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;

public class CommentAccessor {
    private static final Connection connection = DataBaseAccessor.getConnection();

    private CommentAccessor() {
    }

    static void createMessageTable() throws IOException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS comments (" +
                    "id TEXT NOT NULL PRIMARY KEY" +
                    ", userId TEXT NOT NULL REFERENCES users (userId) ON UPDATE CASCADE ON DELETE CASCADE" +
                    ", postId REFERENCES posts (postId) ON UPDATE CASCADE ON DELETE CASCADE" +
                    ", text TEXT" +
                    ", createDate BIGINT" +
                    ");");
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    public synchronized static void addComment(Comment comment) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO comments (id, userId, postId, text, createDate) " +
                "VALUES (?, ?, ? , ? ,?)");
        statement.setString(1, comment.getId());
        statement.setString(2, comment.getUserId());
        statement.setString(3, comment.getPostId());
        statement.setString(4, comment.getText());
        statement.setLong(5, comment.getCreateDate().getTime());
        statement.executeUpdate();
        statement.close();
    }

    public synchronized static void deleteComment(String id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM comments WHERE id = ?");
        statement.setString(1, id);
        statement.executeUpdate();
        statement.close();
    }

    public synchronized static ArrayList<Comment> getCommentsOfPost(String postId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM comments WHERE postId = ?");
        statement.setString(1, postId);
        ResultSet resultSet = statement.executeQuery();
        ArrayList<JSONObject> jsonArray = JsonHandler.getArrayFromResultSet(resultSet);

        resultSet.close();
        statement.close();
        ArrayList<Comment> comments = new ArrayList<>();
        for (JSONObject obj : jsonArray) {
            try {
                comments.add(new Comment(obj));
            } catch (NotAcceptableException ignored) {
            }
        }
        return comments;
    }

    public synchronized static int countPostComments(String postId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM comments WHERE postId = ?;");
        statement.setString(1, postId);
        ResultSet resultSet = statement.executeQuery();
        int count = resultSet.getInt(1);

        statement.close();
        resultSet.close();
        return count;
    }

    public synchronized static Comment getCommentById(String id) throws SQLException, NotFoundException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM comments WHERE id = ?;");
        return getCommentFromResultSet(id, statement);
    }

    private static synchronized Comment getCommentFromResultSet(String input, PreparedStatement statement) throws SQLException, NotFoundException {
        statement.setString(1, input);
        ResultSet resultSet = statement.executeQuery();
        JSONObject jsonObject = JsonHandler.getFromResultSet(resultSet);
        resultSet.close();
        statement.close();
        if (jsonObject == null) {
            throw new NotFoundException("User not Found");
        } else {
            Comment comment;
            try {
                comment = new Comment(jsonObject);
            } catch (NotAcceptableException e) {
                throw new NotFoundException("User not Found");
            }
            return comment;
        }
    }
}
