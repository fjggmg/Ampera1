package com.lifeform.main;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class StoredTrans extends RecursiveTreeObject<StoredTrans> {

    StringProperty address;
    StringProperty amount;
    BooleanProperty sent;
    StringProperty message;
    StringProperty otherAddress;
    StringProperty timestamp;

    public StoredTrans(String address, String amount, boolean sent, String message, String otherAdd, String timestamp) {
        this.address = new SimpleStringProperty(address);
        this.amount = new SimpleStringProperty(amount);
        this.sent = new SimpleBooleanProperty(sent);
        this.message = new SimpleStringProperty(message);
        this.otherAddress = new SimpleStringProperty(otherAdd);
        this.timestamp = new SimpleStringProperty(timestamp);
    }
}
