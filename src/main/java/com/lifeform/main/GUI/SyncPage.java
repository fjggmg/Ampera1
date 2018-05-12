package com.lifeform.main.GUI;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class SyncPage {

    public Pane syncPane;

    @FXML
    public void initialize() {
        Stop[] stops = new Stop[]{new Stop(0, Color.valueOf("18BC9C")), new Stop(1, Color.valueOf("00A685"))};
        syncPane.setBackground(new Background(new BackgroundFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops), CornerRadii.EMPTY, Insets.EMPTY)));
        //syncPane.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)))
    }
}
