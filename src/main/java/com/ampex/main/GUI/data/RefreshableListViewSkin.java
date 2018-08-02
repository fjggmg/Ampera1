package com.ampex.main.GUI.data;

import com.ampex.main.adx.Order;
import com.sun.javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.ListView;

public class RefreshableListViewSkin<T> extends ListViewSkin<T> {
    public RefreshableListViewSkin(ListView<T> listView) {
        super(listView);
    }

    public void refresh() {
        super.flow.recreateCells();
    }
}
