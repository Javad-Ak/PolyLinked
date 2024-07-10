package org.aut.polylinked_client.view;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import org.aut.polylinked_client.PolyLinked;
import org.aut.polylinked_client.control.ContentController;
import org.aut.polylinked_client.model.MediaLinked;
import org.aut.polylinked_client.model.User;
import java.io.IOException;

// this class serves both as model and view
public class ContentCell<T extends MediaLinked> extends ListCell<ContentCell<T>> {
    private T content;
    private User user;

    public ContentCell(T content, User user) {
        this.content = content;
        this.user = user;
    }

    //    ############################################################

    private FXMLLoader fxmlLoader;

    private Parent root;

    public ContentCell() {
        if (fxmlLoader == null) {
            try {
                fxmlLoader = new FXMLLoader(PolyLinked.class.getResource("fxmls/content.fxml"));
                root = fxmlLoader.load();
            } catch (IOException e) {
                System.err.println("failed to load post fxml");
                System.exit(1);
            }
        }
    }

    @Override
    protected void updateItem(ContentCell<T> item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
        } else {
            ContentController controller = fxmlLoader.getController();
            controller.setData(item.content, item.user);
            setGraphic(root);
        }
    }
}
