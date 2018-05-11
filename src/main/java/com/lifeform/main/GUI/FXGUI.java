package com.lifeform.main.GUI;

import com.jfoenix.controls.JFXDecorator;
import com.lifeform.main.Ki;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


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
    @Override
    public void start(final Stage pStage) throws Exception{

        //pStage.initStyle(StageStyle.UNDECORATED);
        //pStage.setResizable(false);
        pStage.setTitle("Origin");

        //NewGUI.stage = pStage;
        Parent root = FXMLLoader.load(getClass().getResource("/NewGUI.fxml"));
        JFXDecorator decorator = new JFXDecorator(pStage, root);
        //decorator.setCustomMaximize(false);
        String css = getClass().getResource("/text-style.css").toExternalForm();
        decorator.setOnCloseButtonAction(() -> {
            System.out.println("Close requested");
            if (Ki.getInstance().getOptions().pool) {
                Ki.getInstance().close();
                return;
            }
            NewGUI.close = true;
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
        Ki.getInstance().getGUIHook().postInit(this, pStage);


    }



}
