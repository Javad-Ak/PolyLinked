package org.aut.polylinked_client.view;

import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;

public interface LazyLoaderList {
    void loadBuffer();

    default void activateLazyLoading(ListView<?> listView) {
        listView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar scrollBar = (ScrollBar) listView.lookup(".scroll-bar:vertical");

                if (scrollBar != null && scrollBar.getOrientation() == javafx.geometry.Orientation.VERTICAL) {
                    scrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.doubleValue() > 0.85) {
                            loadBuffer();
                        }
                    });
                }
            }
        });
    }
}
