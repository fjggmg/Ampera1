package com.lifeform.main;

import com.lifeform.main.adx.Order;
import com.sun.javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.ListView;

public class RefreshableListViewSkin extends ListViewSkin<Order> {
    public RefreshableListViewSkin(ListView<Order> listView) {
        super(listView);
    }

    public void refresh() {
        super.flow.recreateCells();
    }
}
