package org.aut.models;

import org.aut.utils.exceptions.NotAcceptableException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class Comment implements MediaLinked {
    private final String id;
    private final String userId;
    private final String postId;
    private final String text;
    private final Date createDate;

    public Comment(String userId, String postId, String text) throws NotAcceptableException {
        this.id = "comment" + new Random().nextInt(99999) + UUID.randomUUID().toString().substring(10, 23);
        this.postId = postId;
        this.userId = userId;
        this.text = text.trim();
        validateFields(userId, postId, text);
        this.createDate = new Date(System.currentTimeMillis());
    }

    public Comment(JSONObject message) throws NotAcceptableException {
        try {
            this.id = message.getString("id");
            this.userId = message.getString("userId");
            this.postId = message.getString("postId");
            this.text = message.getString("text");
            this.createDate = new Date(message.getLong("createDate"));
            validateFields(userId, postId, text);
        } catch (JSONException e) {
            throw new NotAcceptableException("Wrong jsonObject");
        }
    }

    public String getText() {
        return text;
    }

    public String getPostId() {
        return postId;
    }

    public String getUserId() {
        return userId;
    }

    public String getId() {
        return id;
    }

    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public String getMediaURL() {
        return MediaLinked.SERVER_PREFIX + "comments/" + id;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", userId:" + userId +
                ", postId:" + postId +
                ", text:" + text +
                ", createDate:" + createDate.getTime() +
                "}";
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("userId", userId);
        jsonObject.put("postId", postId);
        jsonObject.put("text", text);
        jsonObject.put("createDate", createDate.getTime());
        return jsonObject;
    }

    public static void validateFields(String userId, String postId, String text) throws NotAcceptableException {
        if (postId == null || postId.isEmpty() || userId == null || userId.isEmpty() || text == null) {
            throw new NotAcceptableException("invalid argument");
        }
    }
}
