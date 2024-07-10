package org.aut.dataAccessors;

import org.aut.models.Connect;
import org.aut.models.User;
import org.aut.utils.JsonHandler;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.json.JSONObject;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConnectAccessor {
    private static final Connection connection = DataBaseAccessor.getConnection();

    private ConnectAccessor() {
    }

    static void createConnectTable() throws IOException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS connects (" +
                    "applicant_id TEXT NOT NULL " +
                    ", acceptor_id TEXT NOT NULL" +
                    ", note TEXT NOT NULL" +
                    ", accept_state TEXT NOT NULL" +
                    ", create_date BIGINT(20)" +
                    ", FOREIGN KEY (applicant_id , acceptor_id)" +
                    " REFERENCES users (userId, userId)" +
                    " ON UPDATE CASCADE" +
                    " ON DELETE CASCADE " +
                    ");");
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    public synchronized static void addConnect(Connect connect) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO connects (applicant_id , acceptor_id , note , accept_state , create_date) " +
                "VALUES (?, ?, ? , ? , ?)");
        statement.setString(1, connect.getApplicant_id());
        statement.setString(2, connect.getAcceptor_id());
        statement.setString(3, connect.getNote());
        statement.setString(4, connect.getAccept_state());
        statement.setLong(5, connect.getCreate_date().getTime());
        statement.executeUpdate();
        statement.close();
    }


    public synchronized static void deleteConnect(Connect connect) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM connects WHERE applicant_id = ? AND acceptor_id = ? AND  accept_state = ?");
        statement.setString(1, connect.getApplicant_id());
        statement.setString(2, connect.getAcceptor_id());
        statement.setString(3, Connect.AcceptState.ACCEPTED.toString());
        statement.executeUpdate();
        statement.close();
    }

    public synchronized static void updateConnect(Connect connect) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE connects SET accept_state = ? WHERE applicant_id = ? AND acceptor_id = ?");
        statement.setString(1, connect.getAccept_state()); //may need change to ACCEPTED if there is only one case for updating
        statement.setString(2, connect.getApplicant_id());
        statement.setString(3, connect.getAcceptor_id());
        statement.executeUpdate();
        statement.close();
    }

    public synchronized static ArrayList<Connect> getRejectedConnectsOf(String userId) throws SQLException, NotAcceptableException {
        ArrayList<Connect> acceptedConnects;
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM connects WHERE (applicant_id = ? OR acceptor_id = ?) AND accept_state = ? ORDER BY create_date DESC ");
        statement.setString(1, userId);
        statement.setString(2, userId);
        statement.setString(3, Connect.AcceptState.REJECTED.toString());
        acceptedConnects = getConnectListFromStatement(statement);
        statement.close();
        return acceptedConnects;

    }

    public synchronized static ArrayList<Connect> getWaitingConnectsOf(String userId) throws SQLException, NotAcceptableException {
        ArrayList<Connect> acceptedConnects;
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM connects WHERE acceptor_id = ? AND accept_state = ? ORDER BY create_date DESC ");
        statement.setString(1, userId);
        statement.setString(2, Connect.AcceptState.WAITING.toString());
        acceptedConnects = getConnectListFromStatement(statement);
        statement.close();
        return acceptedConnects;

    }


    public synchronized static Connect getWaitingConnectOfAcceptor(String applicantId, String acceptorId) throws SQLException, NotAcceptableException {
        Connect connect = null;
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM connects WHERE applicant_id = ? AND acceptor_id = ? AND accept_state = ?");
        statement.setString(1, applicantId);
        statement.setString(2, acceptorId);
        statement.setString(3, Connect.AcceptState.WAITING.toString());
        ResultSet resultSet = statement.executeQuery();
        JSONObject jsonObject = JsonHandler.getFromResultSet(resultSet);
        if (jsonObject != null && !jsonObject.isEmpty()) {
            connect = new Connect(jsonObject);
        }
        statement.close();
        return connect;
    }

    public synchronized static Connect getAcceptedConnect(String user1Id, String user2Id) throws SQLException, NotAcceptableException {
        Connect connect = null;
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM connects WHERE ((acceptor_id = ? AND connects.applicant_id = ?) || (applicant_id = ? AND acceptor_id = ?)) AND accept_state = ?");
        statement.setString(1, user1Id);
        statement.setString(2, user2Id);
        statement.setString(3, user1Id);
        statement.setString(4, user2Id);
        statement.setString(5, Connect.AcceptState.ACCEPTED.toString());
        ResultSet resultSet = statement.executeQuery();
        JSONObject jsonObject = JsonHandler.getFromResultSet(resultSet);
        if (jsonObject != null && !jsonObject.isEmpty()) {
            connect = new Connect(jsonObject);
        }
        statement.close();
        return connect;
    }

    public synchronized static ArrayList<Connect> getConnectListFromStatement(PreparedStatement statement) throws SQLException, NotAcceptableException {
        ArrayList<Connect> connects = new ArrayList<>();
        ResultSet resultSet = statement.executeQuery();
        JSONObject jsonObject;
        while ((jsonObject = JsonHandler.getFromResultSet(resultSet)) != null) {
            connects.add(new Connect(jsonObject));
        }
        resultSet.close();
        return connects;
    }

    public synchronized static List<User> getConnectionsOf(String userId) throws SQLException {
        PreparedStatement statement1 = connection.prepareStatement("SELECT acceptor_id FROM connects WHERE applicant_id = ? AND accept_state = ? ORDER BY create_date DESC ");
        statement1.setString(1, userId);
        statement1.setString(2, Connect.AcceptState.ACCEPTED.toString());
        ArrayList<User> users = new ArrayList<>(getUsersFromIdSet(statement1.executeQuery()));
        statement1.close();

        PreparedStatement statement2 = connection.prepareStatement("SELECT applicant_id FROM connects WHERE acceptor_id = ? AND accept_state = ? ORDER BY create_date DESC ");
        statement2.setString(1, userId);
        statement2.setString(2, Connect.AcceptState.ACCEPTED.toString());
        users.addAll(getUsersFromIdSet(statement2.executeQuery()));
        statement2.close();

        return users.stream().distinct().toList();
    }

    public synchronized static List<User> getNetworkOf(String userId) throws SQLException {
        ArrayList<User> users = new ArrayList<>();
        for (User user : getConnectionsOf(userId)) {
            users.add(user);
            for (User user2 : getConnectionsOf(user.getUserId())) {
                users.add(user2);
                users.addAll(getConnectionsOf(user2.getUserId()));
            }
        }
        return users.stream().distinct().toList();
    }

    public synchronized static boolean userIsInNetworkOf(String requesterId , String userId ) throws SQLException {
        List <User> users = getNetworkOf(userId);
        for (User user : users) {
            if (user.getUserId().equals(requesterId)) {
                return true;
            }
        }
        return false;
    }
    public synchronized static boolean usersIsConnected(String user1Id , String user2Id) throws SQLException, NotAcceptableException {
        return getAcceptedConnect(user1Id, user2Id) != null;
    }

    private static ArrayList<User> getUsersFromIdSet(ResultSet resultSet) throws SQLException {
        ArrayList<User> users = new ArrayList<>();
        while (resultSet.next()) {
            try {
                users.add(UserAccessor.getUserById(resultSet.getString(1)));
            } catch (NotFoundException ignored) {
            }
        }
        resultSet.close();

        return users;
    }
}
