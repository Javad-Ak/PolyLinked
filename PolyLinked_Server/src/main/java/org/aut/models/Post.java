package org.aut.models;

import org.aut.utils.exceptions.NotAcceptableException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class Post implements MediaLinked {
    private final String postId;
    private final String repostFrom;
    private final String userId;
    private final String text;
    private final Date date;
    private int likesCount;
    private int commentsCount;
    // + media file in resources

    public Post(String userId, String text) throws NotAcceptableException {
        if (text == null || text.trim().length() > 3000) {
            throw new NotAcceptableException("invalid arguments");
        }

        postId = "post" + new Random().nextInt(99999) + UUID.randomUUID().toString().substring(10, 23);
        repostFrom = "none";
        this.userId = userId;
        this.text = text.trim();
        date = new Date(System.currentTimeMillis());
        likesCount = 0;
        commentsCount = 0;
    }

    public Post(String repostFrom, String userId, String text) throws NotAcceptableException {
        if (text == null || text.trim().length() > 3000) {
            throw new NotAcceptableException("invalid arguments");
        }

        postId = "post" + new Random().nextInt(99999) + UUID.randomUUID().toString().substring(10, 23);
        this.repostFrom = repostFrom;
        this.userId = userId;
        this.text = text.trim();
        date = new Date(System.currentTimeMillis());
        likesCount = 0;
        commentsCount = 0;
    }

    public Post(JSONObject json) throws NotAcceptableException {
        try {
            postId = json.getString("postId");
            repostFrom = json.getString("repostFrom");
            userId = json.getString("userId");
            text = json.getString("text").trim();
            date = new Date(json.getLong("date"));
            try {
                likesCount = json.getInt("likesCount");
                commentsCount = json.getInt("commentsCount");
            } catch (JSONException ignored) {
                likesCount = 0;
                commentsCount = 0;
            }
        } catch (JSONException e) {
            throw new NotAcceptableException("invalid arguments");
        }

        if (text.trim().isEmpty() || text.trim().length() > 3000)
            throw new NotAcceptableException("invalid arguments");
    }

    @Override
    public String toString() {
        return "{" +
                "postId:" + postId +
                ", repostFrom:" + repostFrom +
                ", userId:" + userId +
                ", text:" + text +
                ", date:" + date.getTime() +
                ", likesCount:" + likesCount +
                ", commentsCount:" + commentsCount +
                '}';
    }

    public String getPostId() {
        return postId;
    }

    public String getUserId() {
        return userId;
    }

    public String getText() {
        return text;
    }

    public long getDate() {
        return date.getTime();
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public String getRepostFrom() {
        return repostFrom;
    }

    public boolean isReposted(){
        return repostFrom.equals("null");
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    @Override
    public String getMediaURL() {
        return MediaLinked.SERVER_PREFIX + "posts/" + postId;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("postId", postId);
        json.put("repostFrom", repostFrom);
        json.put("userId", userId);
        json.put("text", text);
        json.put("date", date.getTime());
        json.put("likesCount", likesCount);
        json.put("commentsCount", commentsCount);
        return json;
    }
}
