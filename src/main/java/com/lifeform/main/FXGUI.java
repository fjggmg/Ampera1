package com.lifeform.main;

import com.jfoenix.controls.JFXDecorator;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;


/**
 * Copyright (C) Bryan Sharpe
 *
 * All rights reserved
 */
public class FXGUI extends Application {

    public static void subLaunch(String... args)
    {
        launch(args);
    }
    private static Stage primaryStage;
    @Override
    public void start(final Stage pStage) throws Exception{

        //pStage.initStyle(StageStyle.UNDECORATED);
        //pStage.setResizable(false);
        pStage.setTitle("Origin");

        NewGUI.stage = pStage;
        NewGUI.app = this;
        Parent root = FXMLLoader.load(getClass().getResource("/NewGUI.fxml"));
        JFXDecorator decorator = new JFXDecorator(pStage, root);
        //decorator.setCustomMaximize(false);
        String css = getClass().getResource("/text-style.css").toExternalForm();
        decorator.setOnCloseButtonAction(new Thread() {
            @Override
            public void run() {
                System.out.println("Close requested");
                NewGUI.close = true;

            }
        });
        decorator.setCustomMaximize(true);

        decorator.setStyle("-fx-border-width:0");
        Scene scene = new Scene(decorator, 1156, 650);
        scene.getStylesheets().add(css);
        pStage.setMinWidth(1156);
        pStage.setMinHeight(650);
        pStage.setWidth(1156);
        pStage.setHeight(650);
        pStage.getIcons().add(new Image(getClass().getResourceAsStream("/origin.png")));
        pStage.setScene(scene);
        pStage.show();


    }



}
