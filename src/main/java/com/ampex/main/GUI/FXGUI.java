package com.ampex.main.GUI;

import com.ampex.main.Ki;
import com.jfoenix.controls.JFXDecorator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;


/**
 * Copyright (C) Bryan Sharpe
 * <p>
 * All rights reserved
 */
public class FXGUI extends Application {

    public static void subLaunch(boolean startSync) {
        sync = startSync;
        launch();
    }

    private static boolean sync = false;
    private JFXDecorator decorator;
    private Stage pStage;
    private StackPane root = new StackPane();

    @Override
    public void start(final Stage pStage) throws Exception {
        Ki.getInstance().setInnerGUIRef(this);
        this.pStage = pStage;
        VBox.setVgrow(root, Priority.ALWAYS);
        pStage.setTitle("Ampera");
        pStage.getIcons().add(new Image(FXGUI.class.getResourceAsStream("/origin.png")));
        pStage.setMinWidth(1156);
        pStage.setMinHeight(650);
        pStage.setWidth(1156);
        pStage.setHeight(650);
        //root.setPrefWidth(1156);
        //root.setPrefHeight(650);

        decorator = new JFXDecorator(pStage, root);

        decorator.setOnCloseButtonAction(() -> {
            System.out.println("Close requested");
            if (Ki.getInstance().getOptions().pool) {
                Ki.getInstance().close();
                return;
            }
            NewGUI.close = true;
        });
        decorator.setCustomMaximize(true);
        Scene scene = new Scene(decorator, 1156, 650);

        String css = FXGUI.class.getResource("/text-style.css").toExternalForm();
        scene.getStylesheets().add(css);

        decorator.setStyle("-fx-border-width:0");
        pStage.setScene(scene);
        pStage.show();
        this.app = this;
        if (sync)
            loadSync();
        else
            loadMain();
    }

    private Application app;
    public void loadMain() {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    AnchorPane parent = FXMLLoader.load(FXGUI.class.getResource("/NewGUI.fxml"));
                    Ki.getInstance().getGUIHook().postInit(app, pStage);
                    root.getChildren().clear();
                    root.getChildren().add(parent);
                    parent.setMinHeight(0);
                    parent.setMinWidth(0);
                    //pStage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void loadSync() {
        try {
            AnchorPane parent = FXMLLoader.load(FXGUI.class.getResource("/SyncPage.fxml"));
            parent.setMinHeight(0);
            parent.setMinWidth(0);
            root.getChildren().clear();
            root.getChildren().add(parent);
            //pStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
