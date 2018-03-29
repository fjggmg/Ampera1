package com.lifeform.main.transactions;

import java.util.List;

/**
 * Created by Bryan on 8/8/2017.
 */
public interface IAddMan {

    IAddress getNewAdd();

    List<IAddress> getActive();

    void receivedOn(IAddress address);

    void usedEntirely(IAddress address);

    void verified(IAddress address);
    void blockTick();

    IAddress getMainAdd();
    void load();
    void save();

    String getEntropyForAdd(IAddress a);

    void setMainAdd(IAddress a);

    IAddress createNew(String binOrKey, String entropy, String prefix, AddressLength l, boolean p2sh);

    List<IAddress> getAll();

    void deleteAddress(IAddress address);

}
