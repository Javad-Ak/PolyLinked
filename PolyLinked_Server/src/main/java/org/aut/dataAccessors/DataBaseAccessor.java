package org.aut.dataAccessors;


import org.aut.httpHandlers.LikeHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.nio.file.*;
import java.sql.*;

public class DataBaseAccessor extends Thread {
    private static final Path RESOURCES = Path.of("./src/main/resources");
    private static final Path DATABASE = Path.of("./src/main/resources/dataBase.db");
    private static final Path URL = Path.of("jdbc:sqlite:./src/main/resources/dataBase.db");
    private static final ArrayList<Connection> connections = new ArrayList<>();

    public DataBaseAccessor(String name) {
        super(name);
    }

    @Override
    public void run() {
        closeConnections();
    }

    public static void create() throws IOException {
        if (!Files.isDirectory(RESOURCES)) Files.createDirectories(RESOURCES);
        if (!Files.isRegularFile(DATABASE)) Files.createFile(DATABASE);

        MediaAccessor.createDirectories();
        UserAccessor.createUserTable();
        FollowAccessor.createFollowsTable();
        ConnectAccessor.createConnectTable();
        ProfileAccessor.createTable();
        EducationAccessor.createTable();
        SkillsAccessor.createTable();
        CallInfoAccessor.createUserTable();
        MessageAccessor.createMessageTable();
        PostAccessor.createTable();
        LikeAccessor.createTable();
        CommentAccessor.createMessageTable();
    }

    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(URL.toString());
            connections.add(connection);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeConnections() {
        for (Connection connection : connections) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
