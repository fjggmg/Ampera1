package com.ampex.main.GUI.data;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.*;
import javafx.collections.ObservableList;


public class StoredTrans extends RecursiveTreeObject<StoredTrans> {

    public StringProperty address;
    public DoubleProperty amount;
    public StringProperty sent;
    public StringProperty message;
    public StringProperty otherAddress;
    public StringProperty timestamp;
    public StringProperty height;
    public ListProperty<String> outputs;
    public DoubleProperty fee;
    public ListProperty<String> inputs;

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
