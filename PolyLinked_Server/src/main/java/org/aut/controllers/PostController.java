package org.aut.controllers;

import org.aut.dataAccessors.*;
import org.aut.models.Comment;
import org.aut.models.Post;
import org.aut.models.User;
import org.aut.utils.exceptions.NotFoundException;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

public class PostController {
    public static TreeMap<Comment, User> getCommentsOfPost(String postId) throws SQLException {
        TreeMap<Comment, User> map = new TreeMap<>();
        ArrayList<Comment> comments = CommentAccessor.getCommentsOfPost(postId);
        for (Comment comment : comments.stream().distinct().toList()) {
            try {
                User user = UserAccessor.getUserById(comment.getUserId());
                map.putIfAbsent(comment, user);
            } catch (NotFoundException ignored) {
            }
        }
        return map;
    }

    public static ArrayList<Post> getPostsOf(String userId) throws SQLException {
        ArrayList<Post> posts = PostAccessor.getPostsOfUser(userId);
        return fillPosts(posts);
    }

    public static ArrayList<Post> getPostsLikedBy(String userId) throws SQLException {
        ArrayList<Post> posts = PostAccessor.getPostsLikedByUser(userId);
        return fillPosts(posts);
    }

    public static ArrayList<Post> getPostsWithHashtag() throws SQLException {
        ArrayList<Post> posts = PostAccessor.getPostsWithHashtags();
        return fillPosts(posts);
    }

    public static ArrayList<Post> fillPosts (ArrayList<Post> posts) throws SQLException {
        for (Post post : posts) {
            post.setLikesCount(LikeAccessor.countPostLikes(post.getPostId()));
            post.setCommentsCount(CommentAccessor.countPostComments(post.getPostId()));
        }
        return posts;
    }
}
