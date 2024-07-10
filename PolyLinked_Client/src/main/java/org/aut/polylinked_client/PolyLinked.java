package org.aut.polylinked_client;

import javafx.application.Application;
import javafx.stage.Stage;
import org.aut.polylinked_client.utils.DataAccess;

/**
 * PolyLinked_Client is a LinkedIn clone Application. (in pair with PolyLinked_Server)
 *
 * @author AliReza AthariFard
 * @author MohammadJavd Akbari
 * @version 1.0
 */
public class PolyLinked extends Application {
    private static SceneManager sceneManager;

    @Override
    public void start(Stage stage) {
        DataAccess.initiate();
        sceneManager = new SceneManager(stage);
        sceneManager.setScene();
        stage.setTitle("PolyLinked");

        stage.show();
    }

    public static void launchApp(String[] args) {
        launch(args);
    }
}