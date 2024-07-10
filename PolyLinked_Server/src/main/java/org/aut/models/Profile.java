package org.aut.models;

import org.aut.utils.JsonHandler;
import org.aut.utils.exceptions.NotAcceptableException;
import org.json.JSONException;
import org.json.JSONObject;

public class Profile implements MediaLinked {
    private final String userId; // same as user id -> foreign key
    private final String bio; // 220 ch
    private final String country; // 60 ch
    private final String city;  // 60 ch
    private final Status status;
    private final Profession profession;
    private final int notify;
    // + Skills(relatively), Educations, CallInfo

    public Profile(String userID, String bio, String country, String city, Status status, Profession profession, int notify) throws NotAcceptableException {
        validateFields(bio, country, city);

        this.userId = userID;
        this.bio = bio;
        this.country = country.toUpperCase();
        this.city = city.toUpperCase();
        this.status = status;
        this.profession = profession;
        this.notify = notify;
    }

    public Profile(JSONObject profile) throws NotAcceptableException {
        try {
            userId = profile.getString("userId");
            bio = profile.getString("bio");
            country = profile.getString("country");
            city = profile.getString("city");
            status = Status.valueOf(profile.getString("status"));
            profession = Profession.valueOf(profile.getString("profession"));
            notify = profile.getInt("notify");
        } catch (JSONException e) {
            throw new NotAcceptableException("JSON could not be parsed");
        }
        validateFields(bio, country, city);
    }

    @Override
    public String toString() {
        return '{' +
                "userId: " + userId +
                ", bio: " + bio +
                ", country: " + country.toUpperCase() +
                ", city: " + city.toUpperCase() +
                ", status: " + status.toString() +
                ", profession: " + profession.toString() +
                ", notify: " + notify +
                '}';
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", userId);
        jsonObject.put("bio", bio);
        jsonObject.put("country", country);
        jsonObject.put("city", city);
        jsonObject.put("status", status.toString());
        jsonObject.put("profession", profession.toString());
        jsonObject.put("notify", notify);
        return jsonObject;
    }

    private void validateFields(String bio, String country, String city) throws NotAcceptableException {
        if ((bio != null && bio.length() > 220) ||
                (country != null && !country.matches("(?i)^[a-z]{0,60}$")) ||
                (city != null && !city.matches("(?i)^[a-z]{0,60}$")))

            throw new NotAcceptableException("invalid arguments");
    }

    public String getUserId() {
        return userId;
    }

    public String getBio() {
        return bio;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getStatus() {
        return status.toString();
    }

    public String getProfession() {
        return profession.toString();
    }

    public int getNotify() {
        return notify;
    }


    @Override
    public String getMediaURL() {
        return MediaLinked.SERVER_PREFIX + "backgrounds/bg" + userId;
    }

    public enum Status {
        RECRUITER("RECRUITER"), SERVICE_PROVIDER("SERVICE_PROVIDER"), JOB_SEARCHER("JOB_SEARCHER");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum Profession {
        DOCTOR("DOCTOR"),
        NURSE("NURSE"),
        TEACHER("TEACHER"),
        ENGINEER("ENGINEER"),
        LAWYER("LAWYER"),
        ACCOUNTANT("ACCOUNTANT"),
        ARCHITECT("ARCHITECT"),
        SCIENTIST("SCIENTIST"),
        SOFTWARE_DEVELOPER("SOFTWARE_DEVELOPER"),
        DENTIST("DENTIST"),
        PHARMACIST("PHARMACIST"),
        PILOT("PILOT"),
        VETERINARIAN("VETERINARIAN"),
        CHEF("CHEF"),
        JOURNALIST("JOURNALIST"),
        POLICE_OFFICER("POLICE_OFFICER"),
        FIREFIGHTER("FIREFIGHTER"),
        ELECTRICIAN("ELECTRICIAN"),
        PLUMBER("PLUMBER"),
        MECHANIC("MECHANIC"),
        GRAPHIC_DESIGNER("GRAPHIC_DESIGNER"),
        MARKETING_MANAGER("MARKETING_MANAGER"),
        HR_MANAGER("HR_MANAGER"),
        FINANCIAL_ANALYST("FINANCIAL_ANALYST"),
        SOCIAL_WORKER("SOCIAL_WORKER"),
        PSYCHOLOGIST("PSYCHOLOGIST"),
        THERAPIST("THERAPIST"),
        MUSICIAN("MUSICIAN"),
        ACTOR("ACTOR"),
        WRITER("WRITER"),
        LIBRARIAN("LIBRARIAN"),
        ECONOMIST("ECONOMIST"),
        PHYSICAL_THERAPIST("PHYSICAL_THERAPIST"),
        OCCUPATIONAL_THERAPIST("OCCUPATIONAL_THERAPIST"),
        RADIOLOGIST("RADIOLOGIST"),
        SURGEON("SURGEON"),
        ANESTHESIOLOGIST("ANESTHESIOLOGIST"),
        CONSULTANT("CONSULTANT"),
        ENTREPRENEUR("ENTREPRENEUR"),
        PROJECT_MANAGER("PROJECT_MANAGER"),
        REAL_ESTATE_AGENT("REAL_ESTATE_AGENT"),
        SALES_MANAGER("SALES_MANAGER"),
        INTERIOR_DESIGNER("INTERIOR_DESIGNER"),
        CIVIL_ENGINEER("CIVIL_ENGINEER"),
        MECHANICAL_ENGINEER("MECHANICAL_ENGINEER"),
        DATA_SCIENTIST("DATA_SCIENTIST"),
        BIOLOGIST("BIOLOGIST"),
        CHEMIST("CHEMIST"),
        URBAN_PLANNER("URBAN_PLANNER"),
        ENVIRONMENTAL_SCIENTIST("ENVIRONMENTAL_SCIENTIST"),
        COMPUTER_ENGINEER("COMPUTER_ENGINEER"),
        PROGRAMMER("PROGRAMMER"),
        WEB_DEVELOPER("WEB_DEVELOPER"),
        NETWORK_ENGINEER("NETWORK_ENGINEER"),
        SYSTEMS_ANALYST("SYSTEMS_ANALYST"),
        CYBERSECURITY_ANALYST("CYBERSECURITY_ANALYST"),
        DATABASE_ADMINISTRATOR("DATABASE_ADMINISTRATOR"),
        UX_UI_DESIGNER("UX_UI_DESIGNER"),
        BUSINESS_ANALYST("BUSINESS_ANALYST"),
        IT_MANAGER("IT_MANAGER");

        private final String value;

        Profession(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
