package com.ampex.main.GUI;

import com.ampex.main.Ki;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;

import java.math.BigInteger;

public class SyncPage {

    public Pane syncPane;
    public Label mainLabel;
    public JFXProgressBar downloadProgress;
    public JFXProgressBar verifyProgress;
    public Label heightLabel;
    private Font mFont = Font.loadFont(SyncPage.class.getResourceAsStream("/ADAM.CG PRO.otf"), 24);
    private Thread updateThread;

    @FXML
    public void initialize() {
        Stop[] stops = new Stop[]{new Stop(0, Color.valueOf("18BC9C")), new Stop(1, Color.valueOf("00A685"))};
        syncPane.setBackground(new Background(new BackgroundFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops), CornerRadii.EMPTY, Insets.EMPTY)));
        //syncPane.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)))
        mainLabel.setFont(mFont);

        BigInteger offset = Ki.getInstance().getLoadHeight();
        BigInteger total = Ki.getInstance().getStartHeight().subtract(Ki.getInstance().getLoadHeight());
        updateThread = new Thread() {
            public void run() {
                while (true) {

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            heightLabel.setText("Height - " + Ki.getInstance().getChainMan().currentHeight());
                            downloadProgress.setProgress(Ki.getInstance().getDownloadedTo().subtract(offset).doubleValue() / total.doubleValue());
                            verifyProgress.setProgress(Ki.getInstance().getChainMan().currentHeight().subtract(offset).doubleValue() / total.doubleValue());

                        }
                    });
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                    if (Ki.getInstance().getChainMan().currentHeight().compareTo(Ki.getInstance().getStartHeight()) >= 0) {
                        break;
                    }
                }

            }

        };
        updateThread.setDaemon(true);
        updateThread.setName("Update Thread");
        updateThread.start();
    }

    public static void close() {

    }


}
