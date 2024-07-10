module org.aut.polylinked_client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.jfoenix;
    requires org.json;
    requires org.jetbrains.annotations;
    requires java.net.http;
    requires GNAvatarView;
    requires org.kordamp.ikonli.javafx;
    requires org.controlsfx.controls;
    requires javafx.media;


    opens org.aut.polylinked_client.control to javafx.fxml;
    exports org.aut.polylinked_client;
    exports org.aut.polylinked_client.utils;
    exports org.aut.polylinked_client.utils.exceptions;
    exports org.aut.polylinked_client.model;
    exports org.aut.polylinked_client.control;
    exports org.aut.polylinked_client.view;
}