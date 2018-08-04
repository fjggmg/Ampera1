package com.ampex.main.GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class SyncGUI extends Application {

    public static void subLaunch() {
        launch();
    }


    @Override
    public void start(Stage pStage) throws Exception {
        pStage.setTitle("Ampera");

        Parent root = FXMLLoader.load(FXGUI.class.getResource("/SyncPage.fxml"));

        String css = FXGUI.class.getResource("/text-style.css").toExternalForm();

        Scene scene = new Scene(root, 1156, 650);
        scene.getStylesheets().add(css);
        pStage.setMinWidth(1156);
        pStage.setMinHeight(650);
        pStage.setWidth(1156);
        pStage.setHeight(650);
        pStage.getIcons().add(new Image(FXGUI.class.getResourceAsStream("/origin.png")));
        pStage.setScene(scene);
        pStage.show();
    }
}
