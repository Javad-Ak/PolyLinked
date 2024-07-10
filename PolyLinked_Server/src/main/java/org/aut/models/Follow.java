
package org.aut.models;

import org.aut.utils.exceptions.NotAcceptableException;
import org.json.JSONException;
import org.json.JSONObject;

public final class Follow implements JsonSerializable {
    private final String follower_id;
    private final String followed_id;

    public Follow(String follower_id, String followed_id) throws NotAcceptableException {
        validateFields(follower_id, followed_id);
        this.follower_id = follower_id;
        this.followed_id = followed_id;
    }

    public Follow(JSONObject json) throws NotAcceptableException {
        try {
            validateFields(json.getString("follower_id"), json.getString("followed_id"));
            follower_id = json.getString("follower_id");
            followed_id = json.getString("followed_id");
        } catch (JSONException e) {
            throw new NotAcceptableException("Wrong jsonObject");
        }
    }

    public String getFollowed_id() {
        return followed_id;
    }

    public String getFollower_id() {
        return follower_id;
    }

    @Override
    public String toString() {
        return "{" +
                "follower_id:" + follower_id
                + ", followed_id:" + followed_id
                + "}";
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("follower_id", follower_id);
        json.put("followed_id", followed_id);
        return json;
    }

    private static void validateFields(String follower, String followed) throws NotAcceptableException {
        if (follower == null || followed == null || followed.equals(follower))
            throw new NotAcceptableException("invalid argument");
    }

    public String follower() {
        return follower_id;
    }

    public String followed() {
        return followed_id;
    }
}


