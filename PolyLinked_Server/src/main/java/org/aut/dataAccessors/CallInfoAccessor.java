package org.aut.dataAccessors;

import org.aut.models.CallInfo;
import org.aut.models.Message;
import org.aut.utils.JsonHandler;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.json.JSONObject;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;

public class CallInfoAccessor {
    private static final Connection connection = DataBaseAccessor.getConnection();

    private CallInfoAccessor() {
    }

    static void createUserTable() throws IOException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS callInfo (" +
                    "userId TEXT PRIMARY KEY NOT NULL REFERENCES users(userId) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ", emailAddress TEXT NOT NULL REFERENCES users(email) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ", mobileNumber TEXT" +
                    ", homeNumber TEXT" +
                    ", workNumber TEXT" +
                    ", address TEXT" +
                    ", birthDay BIGINT" +
                    ", privacyPolitics TEXT" +
                    ", socialMedia TEXT" +
                    ");");
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    public synchronized static void addCallInfo(CallInfo callInfo) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO callInfo(userId, emailAddress, mobileNumber, homeNumber, workNumber, Address, birthDay, privacyPolitics, socialMedia) " +
                "VALUES (?,?,?,?,?,?,?,?,?);");

        statement.setString(1, callInfo.getUserId());
        statement.setString(2, callInfo.getEmail());
        statement.setString(3, callInfo.getMobileNumber());
        statement.setString(4, callInfo.getHomeNumber());
        statement.setString(5, callInfo.getWorkNumber());
        statement.setString(6, callInfo.getAddress());
        statement.setLong(7, callInfo.getBirthDay());
        statement.setString(8, callInfo.getPrivacyPolitics());
        statement.setString(9, callInfo.getSocialMedia());

        statement.executeUpdate();
        statement.close();
    }

    public synchronized static void updateCallInfo(CallInfo callInfo) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE callInfo SET emailAddress=?, mobileNumber=?, homeNumber=?, workNumber=?, address=?, birthDay=?, privacyPolitics=?, socialMedia=? WHERE userId=?;");

        statement.setString(1, callInfo.getEmail());
        statement.setString(2, callInfo.getMobileNumber());
        statement.setString(3, callInfo.getHomeNumber());
        statement.setString(4, callInfo.getWorkNumber());
        statement.setString(5, callInfo.getAddress());
        statement.setLong(6, callInfo.getBirthDay());
        statement.setString(7, callInfo.getPrivacyPolitics());
        statement.setString(8, callInfo.getSocialMedia());
        statement.setString(9, callInfo.getUserId());

        statement.executeUpdate();
        statement.close();
    }

    public synchronized static CallInfo getCallInfoByUserId(String userId) throws SQLException, NotFoundException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM callInfo WHERE userId = ?;");
        statement.setString(1, userId);
        ResultSet resultSet = statement.executeQuery();
        return getCallInfoFromResultSet(resultSet);
    }

    public synchronized static void deleteCallInfo(String userId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM callInfo WHERE userId = ?;");
        statement.setString(1, userId);
        statement.executeUpdate();
        statement.close();
    }

    public synchronized static boolean callInfoExists(String userId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM callInfo WHERE userId = ?;");
        statement.setString(1, userId);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }


    private static CallInfo getCallInfoFromResultSet(ResultSet resultSet) throws SQLException, NotFoundException {
        JSONObject jsonObject = JsonHandler.getFromResultSet(resultSet);
        resultSet.close();
        if (jsonObject == null) {
            throw new NotFoundException("CallInfo not Found");
        } else {
            CallInfo callInfo;
            try {
                callInfo = new CallInfo(jsonObject);
            } catch (NotAcceptableException e) {
                throw new NotFoundException("CallInfo not Found");
            }
            return callInfo;
        }
    }
}
