package com.ampex.main.GUI;

import com.ampex.main.Ki;
import com.jfoenix.controls.JFXDecorator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
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
    private Parent root = new Pane();
    private Parent root2 = new Pane();

    @Override
    public void start(final Stage pStage) throws Exception {
        Ki.getInstance().setInnerGUIRef(this);
        this.pStage = pStage;
        pStage.setTitle("Origin");
        pStage.getIcons().add(new Image(FXGUI.class.getResourceAsStream("/origin.png")));
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

        pStage.setMinWidth(1156);
        pStage.setMinHeight(650);
        pStage.setWidth(1156);
        pStage.setHeight(650);
        decorator.setStyle("-fx-border-width:0");

        if (sync)
            loadSync();
        else
            loadMain();
        //Ki.getInstance().getGUIHook().postInit(this, pStage);
    }


    public void loadMain() {
        try {
            Parent root = FXMLLoader.load(FXGUI.class.getResource("/NewGUI.fxml"));
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    decorator.setContent(root);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSync() {
        try {
            Parent root = FXMLLoader.load(FXGUI.class.getResource("/SyncPage.fxml"));
            decorator.setContent(root);
            Scene scene = new Scene(decorator, 800, 650);
            String css = FXGUI.class.getResource("/text-style.css").toExternalForm();
            scene.getStylesheets().add(css);
            pStage.setMinHeight(650);
            pStage.setMinWidth(800);
            pStage.hide();
            pStage.setScene(scene);
            pStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
