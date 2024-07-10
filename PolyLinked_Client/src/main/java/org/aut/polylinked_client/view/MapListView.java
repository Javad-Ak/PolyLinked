package org.aut.polylinked_client.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import org.aut.polylinked_client.model.MediaLinked;
import org.aut.polylinked_client.model.User;
import java.util.List;
import java.util.TreeMap;

public class MapListView<T extends MediaLinked> implements LazyLoaderList {
    ListView<ContentCell<T>> listView;

    private final TreeMap<T, User> dataTree;

    private final ObservableList<ContentCell<T>> observableCells = FXCollections.observableArrayList();

    private final List<T> sortedKeys;

    private int index = 0;

    private int bufferCount = 0;

    public MapListView(ListView<ContentCell<T>> listView, TreeMap<T, User> dataTree, List<T> sortedKeys) {
        this.listView = listView;
        this.dataTree = dataTree;
        this.sortedKeys = sortedKeys;
    }

    public void activate(int bufferCount) {
        this.bufferCount = bufferCount;
        listView.setItems(observableCells);
        listView.setCellFactory(listView -> new ContentCell<>());
        loadBuffer();
        activateLazyLoading(listView);
    }

    public void addFirst(ContentCell<T> cell) {
        observableCells.addFirst(cell);
    }

    @Override
    public void loadBuffer() {
        if (sortedKeys.isEmpty() || sortedKeys.size() - 1 < index || bufferCount == 0) return;

        for (int i = index; i < index + bufferCount && i < sortedKeys.size(); i++)
            observableCells.add(new ContentCell<>(sortedKeys.get(i), dataTree.get(sortedKeys.get(i))));

        index += bufferCount;
    }
}
