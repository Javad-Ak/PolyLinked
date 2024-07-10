package org.aut.polylinked_client.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.aut.polylinked_client.SceneManager;
import org.aut.polylinked_client.model.User;
import org.aut.polylinked_client.view.UserCell;
import org.kordamp.ikonli.javafx.FontIcon;
import java.util.List;


public class UserListController {
    private final ListView<User> listView;

    public UserListController(List<User> users) {
        listView = new ListView<>();
        ObservableList<User> notifications = FXCollections.observableArrayList();
        notifications.addAll(users);
        listView.setItems(notifications);
        listView.setCellFactory(listView -> new UserCell());
    }

    public ListView<User> getListView() {
        return listView;
    }

    public static void initiatePage(ListView<User> listView) {
        BorderPane borderPane = new BorderPane();

        HBox hBox = new HBox();
        FontIcon icon = new FontIcon();
        icon.setId("actionIcon");
        icon.setIconLiteral("mdi-arrow-left");
        Button button = new Button();
        button.setGraphic(icon);

        hBox.getChildren().add(button);
        hBox.setAlignment(Pos.TOP_LEFT);
        hBox.setPadding(new Insets(0, 0, 15, 0));

        borderPane.setPadding(new Insets(20,20,20,20));
        SceneManager.activateTheme(borderPane, "home");
        borderPane.setTop(hBox);
        borderPane.setCenter(listView);

        BooleanProperty bool = new SimpleBooleanProperty(false);
        button.setOnAction(event -> bool.set(true));

        SceneManager.switchRoot(borderPane, bool);
    }
}
