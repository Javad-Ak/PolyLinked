package org.aut.dataAccessors;

import org.aut.models.Post;
import org.aut.models.User;
import org.aut.utils.JsonHandler;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;

public class PostAccessor {
    private static final Connection connection = DataBaseAccessor.getConnection();

    private PostAccessor() {
    }

    static void createTable() throws IOException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS posts (" +
                    "postId TEXT NOT NULL" +
                    ", repostFrom TEXT NOT NULL" +
                    ", userId TEXT NOT NULL" +
                    ", text TEXT NOT NULL" +
                    ", date BIGINT NOT NULL" +
                    ", FOREIGN KEY (userId)" +
                    " REFERENCES users (userId)" +
                    " ON UPDATE CASCADE" +
                    " ON DELETE CASCADE" +
                    ");");
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    public synchronized static void addPost(Post post) throws SQLException, NotAcceptableException {
        try {
            getPostById(post.getPostId());
            throw new NotAcceptableException("Already Exists.");
        } catch (NotFoundException e) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO posts (postId, repostFrom, userId, text, date) " +
                    "VALUES (?,?,?,?,?);");
            statement.setString(1, post.getPostId());
            statement.setString(2, post.getRepostFrom());
            statement.setString(3, post.getUserId());
            statement.setString(4, post.getText());
            statement.setLong(5, post.getDate());
            statement.executeUpdate();
            statement.close();
        }
    }

    public synchronized static void deletePost(String postId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM posts WHERE postId = ?;");
        statement.setString(1, postId);
        statement.executeUpdate();
        statement.close();
    }

    public synchronized static Post getPostById(String postId) throws SQLException, NotFoundException, NotAcceptableException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM posts WHERE postId = ?;");
        statement.setString(1, postId);
        ResultSet resultSet = statement.executeQuery();
        JSONObject jsonObject = JsonHandler.getFromResultSet(resultSet);
        if (jsonObject == null || jsonObject.isEmpty()) throw new NotFoundException("Post not found.");
        resultSet.close();
        statement.close();

        jsonObject.put("likesCount", LikeAccessor.countPostLikes(postId));
        jsonObject.put("commentsCount", CommentAccessor.countPostComments(postId));

        return new Post(jsonObject);
    }

    public synchronized static ArrayList<Post> getPostsWithHashtags() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM posts WHERE text like '%#_%';");
        return getPostsFromSet(statement, resultSet);
    }

    public synchronized static ArrayList<Post> getPostsLikedByUser(String userId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM posts WHERE postId = (SELECT likes.postId FROM likes WHERE likes.userId = ?);");
        statement.setString(1, userId);
        ResultSet resultSet = statement.executeQuery();
        return getPostsFromSet(statement, resultSet);
    }

    public synchronized static ArrayList<Post> getPostsOfUser(String userId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM posts WHERE userId = ?;");
        statement.setString(1, userId);
        ResultSet resultSet = statement.executeQuery();
        return getPostsFromSet(statement, resultSet);
    }

    public synchronized static void updatePost(Post post) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE posts SET repostFrom = ?, userId = ? , text = ? , date = ? WHERE postId = ?;");
        statement.setString(1, post.getRepostFrom());
        statement.setString(2, post.getUserId());
        statement.setString(3, post.getText());
        statement.setLong(4, post.getDate());
        statement.setString(5, post.getPostId());
        statement.executeUpdate();
        statement.close();
    }

    @NotNull
    private static ArrayList<Post> getPostsFromSet(Statement statement, ResultSet resultSet) throws SQLException {
        ArrayList<JSONObject> jsonArray = JsonHandler.getArrayFromResultSet(resultSet);
        ArrayList<Post> posts = new ArrayList<>();
        if (jsonArray.isEmpty()) return posts;

        resultSet.close();
        statement.close();

        for (JSONObject jsonObject : jsonArray) {
            jsonObject.put("likesCount", LikeAccessor.countPostLikes(jsonObject.getString("postId")));
            jsonObject.put("commentsCount", CommentAccessor.countPostComments(jsonObject.getString("postId")));

            try {
                posts.add(new Post(jsonObject));
            } catch (Exception ignored) {
            }
        }

        return posts;
    }
}
