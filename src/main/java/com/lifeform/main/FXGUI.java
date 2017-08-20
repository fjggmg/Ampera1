package com.lifeform.main;

import com.lifeform.main.transactions.Token;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;

public class FXGUI extends Application {



    public static void subLaunch(String... args)
    {
        launch(args);
    }
    public static void main(String[] args) {
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

        pStage.setScene(scene);
        pStage.show();
        FXMLController.primaryStage = pStage;
        FXMLController.app = this;


    }



}
