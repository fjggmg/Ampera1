package com.ampex.main.GUI;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class ADXDialog {


    @FXML
    public JFXButton okButton;

    @FXML
    public void initialize()
    {

        okButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                JFXDialog dialog = getDialog(okButton);
                dialog.close();
            }
        });
    }

    public JFXDialog getDialog(Node n)
    {
        if(n.getParent() == null) return null;
        if(n.getParent() instanceof JFXDialog)
        {
            //System.out.println("Found dialog");
            return (JFXDialog)n.getParent();
        }else{
            //System.out.println("Type of parent: " + n.getParent().getClass().getName());
            return getDialog(n.getParent());
        }
    }

}
