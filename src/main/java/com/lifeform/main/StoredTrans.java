package com.lifeform.main;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.util.List;


public class StoredTrans extends RecursiveTreeObject<StoredTrans> {

    StringProperty address;
    DoubleProperty amount;
    StringProperty sent;
    StringProperty message;
    StringProperty otherAddress;
    StringProperty timestamp;
    StringProperty height;
    ListProperty<String> outputs;
    DoubleProperty fee;
    ListProperty<String> inputs;

    public StoredTrans(String address, Double amount, String sent, String message, String otherAdd, String timestamp, String height, ObservableList<String> outputs, ObservableList<String> inputs, Double fee) {
        this.address = new SimpleStringProperty(address);
        this.amount = new SimpleDoubleProperty(amount);
        this.sent = new SimpleStringProperty(sent);
        this.message = new SimpleStringProperty(message);
        this.otherAddress = new SimpleStringProperty(otherAdd);
        this.timestamp = new SimpleStringProperty(timestamp);
        this.height = new SimpleStringProperty(height);
        this.outputs = new SimpleListProperty<>(outputs);
        this.inputs = new SimpleListProperty<>(inputs);
        this.fee = new SimpleDoubleProperty(fee);


    }
}
