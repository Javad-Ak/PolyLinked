package org.aut.controllers;

import org.aut.dataAccessors.ConnectAccessor;
import org.aut.dataAccessors.FollowAccessor;
import org.aut.dataAccessors.PostAccessor;
import org.aut.dataAccessors.UserAccessor;
import org.aut.models.Post;
import org.aut.models.User;
import org.aut.utils.exceptions.NotFoundException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class NewsFeedController {
    public static TreeMap<Post, User> fetchFeed(String userId) throws SQLException {
        List<User> related = new ArrayList<>();
        related.addAll(FollowAccessor.getFollowings(userId));
        related.addAll(ConnectAccessor.getConnectionsOf(userId));

        List<User> connectNetwork = ConnectAccessor.getNetworkOf(userId);
        List<User> followNetwork = FollowAccessor.getNetWork(userId);

        ArrayList<Post> posts = new ArrayList<>();
        for (User user : connectNetwork) posts.addAll(PostController.getPostsLikedBy(user.getUserId()));
        for (User user : followNetwork) posts.addAll(PostController.getPostsLikedBy(user.getUserId()));
        for (User user : related) posts.addAll(PostController.getPostsOf(user.getUserId()));
        posts.addAll(PostController.getPostsOf(userId));

        TreeMap<Post, User> feed = new TreeMap<>();
        for (Post post : posts.stream().distinct().toList()) {
            try {
                feed.putIfAbsent(post, UserAccessor.getUserById(post.getUserId()));
            } catch (NotFoundException ignored) {
            }
        }
        return feed;
    }
}
