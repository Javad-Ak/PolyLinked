package org.aut.models;

import org.aut.utils.exceptions.NotAcceptableException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class Connect implements JsonSerializable {
    private final String applicant_id;
    private final String acceptor_id;
    private final String note;
    private final AcceptState accept_state;
    private final Date create_date;

    public Connect(String applicant_id, String acceptor_id, String note, AcceptState accept_state) throws NotAcceptableException {
        validateFields(applicant_id, acceptor_id, note);
        this.applicant_id = applicant_id;
        this.acceptor_id = acceptor_id;
        this.note = note;
        this.accept_state = accept_state;
        this.create_date = new Date(System.currentTimeMillis());
    }

    public Connect(String applicant_id, String acceptor_id, String note) throws NotAcceptableException {
        validateFields(applicant_id, acceptor_id, note);
        this.applicant_id = applicant_id;
        this.acceptor_id = acceptor_id;
        this.note = note;
        this.accept_state = AcceptState.WAITING;
        this.create_date = new Date(System.currentTimeMillis());
    }

    public Connect(JSONObject json) throws NotAcceptableException {
        try {
            this.applicant_id = json.getString("applicant_id");
            this.acceptor_id = json.getString("acceptor_id");
            this.accept_state = AcceptState.valueOf(json.getString("accept_state"));
            this.note = json.getString("note");
            this.create_date = new Date(json.getLong("create_date"));
        } catch (JSONException e) {
            throw new NotAcceptableException("Wrong jsonObject");
        }
        validateFields(applicant_id, acceptor_id, note);
    }

    public String getAcceptor_id() {
        return acceptor_id;
    }

    public String getApplicant_id() {
        return applicant_id;
    }

    public String getNote() {
        return note;
    }

    public String getAccept_state() {
        return accept_state.value;
    }

    public Date getCreate_date() {
        return create_date;
    }

    @Override
    public String toString() {
        return "{" +
                "applicant_id:" + applicant_id +
                ", acceptor_id:" + acceptor_id +
                ", accept_state:" + accept_state.toString() +
                ", note:" + note +
                ", create_date:" + create_date.getTime() +
                "}";
    }



    public static void validateFields(String applicantId, String acceptorId, String note) throws NotAcceptableException {
        if (applicantId == null || acceptorId == null || note == null || note.length() >= 500) {
            throw new NotAcceptableException("invalid argument");
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("applicant_id", applicant_id);
        jsonObject.put("acceptor_id", acceptor_id);
        jsonObject.put("accept_state", accept_state.value);
        jsonObject.put("note", note);
        jsonObject.put("create_date", create_date.getTime());
        return jsonObject;
    }

    public enum AcceptState {
        ACCEPTED("ACCEPTED"),
        REJECTED("REJECTED"),
        WAITING("WAITING");


        private final String value;

        AcceptState(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
