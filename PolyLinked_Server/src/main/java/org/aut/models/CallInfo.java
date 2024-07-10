package org.aut.models;

import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class CallInfo implements JsonSerializable {
    private final String userId; // foreign key
    private final String email; // foreign key
    private final String mobileNumber; // valid 40 chars
    private final String homeNumber;
    private final String workNumber;
    private final String address; // 220
    private final Date birthDay;
    private final PrivacyPolitics privacyPolitics;
    private final String socialMedia;


    public CallInfo(String userId, String email, String mobileNumber, String homeNumber, String workNumber, String address, Date birthDay, PrivacyPolitics privacyPolitics, String socialMedia) throws NotAcceptableException {
        validateFields(userId, email, mobileNumber, homeNumber, workNumber, address, socialMedia);
        this.userId = userId;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.homeNumber = homeNumber;
        this.workNumber = workNumber;
        this.address = address;
        this.birthDay = birthDay;
        this.privacyPolitics = privacyPolitics;
        this.socialMedia = socialMedia;
    }

    public CallInfo(JSONObject jsonObject) throws NotAcceptableException, NotFoundException {
        try {
            userId = jsonObject.getString("userId");
            email = jsonObject.getString("emailAddress");
            mobileNumber = jsonObject.getString("mobileNumber");
            homeNumber = jsonObject.getString("homeNumber");
            workNumber = jsonObject.getString("workNumber");
            address = jsonObject.getString("address");
            birthDay = new Date(jsonObject.getLong("birthDay"));
            privacyPolitics = PrivacyPolitics.valueOf(jsonObject.getString("privacyPolitics"));
            socialMedia = jsonObject.getString("socialMedia");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new NotAcceptableException("Wrong jsonObject");
        }
        validateFields(userId, email, mobileNumber, homeNumber, workNumber, address, socialMedia);
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getHomeNumber() {
        return homeNumber;
    }

    public String getWorkNumber() {
        return workNumber;
    }

    public String getAddress() {
        return address;
    }

    public long getBirthDay() {
        return birthDay.getTime();
    }

    public String getPrivacyPolitics() {
        return privacyPolitics.value;
    }

    public String getSocialMedia() {
        return socialMedia;
    }

    @Override
    public String toString() {
        return '{' +
                "userId: " + userId +
                ", emailAddress: " + email +
                ", mobileNumber: " + mobileNumber +
                ", homeNumber: " + homeNumber +
                ", workNumber: " + workNumber +
                ", address: " + address +
                ", birthDay: " + birthDay.getTime() +
                ", privacyPolitics: " + privacyPolitics +
                ", socialMedia: " + socialMedia +
                '}';
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", userId);
        jsonObject.put("emailAddress", email);
        jsonObject.put("mobileNumber", mobileNumber);
        jsonObject.put("homeNumber", homeNumber);
        jsonObject.put("workNumber", workNumber);
        jsonObject.put("address", address);
        jsonObject.put("birthDay", birthDay.getTime());
        jsonObject.put("privacyPolitics", privacyPolitics.value);
        jsonObject.put("socialMedia", socialMedia);
        return jsonObject;
    }

    private void validateFields(String userId, String email, String mobileNumber, String homeNumber, String workNumber, String address, String socialMedia) throws NotAcceptableException {
        if (userId == null || email == null ||
                (mobileNumber != null && !mobileNumber.matches("^[0-9]{1,40}$")) ||
                (workNumber != null && !workNumber.matches("^[0-9]{1,40}$")) ||
                (homeNumber != null && !homeNumber.matches("^[0-9]{1,40}$")) ||
                (address != null && address.length() > 40) ||
                (!email.matches("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")) ||
                (socialMedia != null && socialMedia.length() > 40)
        ) throw new NotAcceptableException("Illegal args");
    }

    public enum PrivacyPolitics {
        ONLY_ME("ONLY_ME"), MY_CONNECTIONS("MY_CONNECTIONS"), FURTHER_CONNECTIONS("FURTHER_CONNECTIONS"), EVERYONE("EVERYONE");

        private final String value;

        PrivacyPolitics(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
