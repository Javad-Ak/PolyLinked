package org.aut.models;

import org.aut.utils.exceptions.NotAcceptableException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class Message implements MediaLinked {
    private final String id;//UUID
    private final String senderId;
    private final String receiverId;
    private final String postId;
    private final String text;
    private final Date createDate;

    public Message(String senderId, String receiverId, String text) throws NotAcceptableException {
        this.id = "msg" + new Random().nextInt(99999) + UUID.randomUUID().toString().substring(10, 23);
        this.senderId = senderId;
        this.receiverId = receiverId;
        postId = "none";
        this.text = text.trim();
        validateFields(senderId, receiverId, text);
        this.createDate = new Date(System.currentTimeMillis());
    }

    public Message(String senderId, String receiverId, String postId, String text) throws NotAcceptableException {
        this.id = "msg" + new Random().nextInt(99999) + UUID.randomUUID().toString().substring(10, 23);
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.postId = postId;
        this.text = text.trim();
        validateFields(senderId, receiverId, text);
        this.createDate = new Date(System.currentTimeMillis());
    }

    public Message(JSONObject message) throws NotAcceptableException {
        try {
            this.id = message.getString("id");
            this.senderId = message.getString("senderId");
            this.receiverId = message.getString("receiverId");
            this.postId = message.getString("postId");
            this.text = message.getString("text");
            this.createDate = new Date(message.getLong("createDate"));
            validateFields(senderId, receiverId, text);
        } catch (JSONException e) {
            throw new NotAcceptableException("Wrong jsonObject");
        }
    }

    public String getText() {
        return text;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getId() {
        return id;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public String getPostId() {
        return postId;
    }

    @Override
    public String getMediaURL() {
        return MediaLinked.SERVER_PREFIX + "messages/" + id;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", senderId:" + senderId +
                ", receiverId:" + receiverId +
                ", postId:" + postId +
                ", text:" + text +
                ", createDate:" + createDate.getTime() +
                "}";
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("senderId", senderId);
        json.put("receiverId", receiverId);
        json.put("postId", postId);
        json.put("text", text);
        json.put("createDate", createDate.getTime());
        return json;
    }

    public static void validateFields(String senderId, String receiverId, String text) throws NotAcceptableException {
        if (senderId == null || senderId.isEmpty() || receiverId == null || receiverId.isEmpty() || text == null || text.length() > 1900) {
            throw new NotAcceptableException("invalid argument");
        }
    }
}

