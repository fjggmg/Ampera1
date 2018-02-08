package com.lifeform.main;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class StoredTrans extends RecursiveTreeObject<StoredTrans> {

    StringProperty address;
    StringProperty amount;
    StringProperty sent;
    StringProperty message;
    StringProperty otherAddress;
    StringProperty timestamp;
    StringProperty height;

    public StoredTrans(String address, String amount, String sent, String message, String otherAdd, String timestamp, String height) {
        this.address = new SimpleStringProperty(address);
        this.amount = new SimpleStringProperty(amount);
        this.sent = new SimpleStringProperty(sent);
        this.message = new SimpleStringProperty(message);
        this.otherAddress = new SimpleStringProperty(otherAdd);
        this.timestamp = new SimpleStringProperty(timestamp);
        this.height = new SimpleStringProperty(height);
    }
}
