package com.lifeform.main.transactions;

import java.util.List;

/**
 * Created by Bryan on 8/8/2017.
 */
public interface IAddMan {

    Address getNewAdd();
    List<Address> getActive();
    void receivedOn(Address address);
    void usedEntirely(Address address);
    void verified(Address address);
    void blockTick();
    Address getMainAdd();
    void load();
    void save();
    String getEntropyForAdd(Address a);
    void setMainAdd(Address a);

    List<Address> getAll();

}
