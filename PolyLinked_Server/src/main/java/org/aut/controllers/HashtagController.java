package org.aut.controllers;

import org.aut.dataAccessors.PostAccessor;
import org.aut.dataAccessors.UserAccessor;
import org.aut.models.Post;
import org.aut.models.User;
import org.aut.utils.exceptions.NotFoundException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashtagController {
    public static TreeMap<Post, User> hashtagDetector(String input) throws SQLException, NotFoundException {
        TreeMap<Post, User> map = new TreeMap<>();
        ArrayList<Post> posts = PostController.getPostsWithHashtag();
        for (Post post : posts) {
            if (hashtagExists(post.getText(), input)) map.put(post, UserAccessor.getUserById(post.getUserId()));
        }
        return map;
    }

    private static boolean hashtagExists(String text, String input) {
        String regex = "(?i)\\b" + Pattern.quote(input) + "[a-zA-z0-9]*\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }
}
