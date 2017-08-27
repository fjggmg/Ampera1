package com.lifeform.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


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

        pStage.initStyle(StageStyle.UNDECORATED);
        pStage.setResizable(false);
        pStage.setTitle("Origin");
        Parent root = FXMLLoader.load(getClass().getResource("/FXGUI.fxml"));
        Scene scene = new Scene(root, 640, 480);
        pStage.getIcons().add(new Image(getClass().getResourceAsStream("/origin.png")));
        pStage.setScene(scene);
        pStage.show();
        FXMLController.primaryStage = pStage;
        FXMLController.app = this;


    }



}
