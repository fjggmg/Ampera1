package com.lifeform.main.transactions;

import engine.binary.Binary;

import java.util.List;

/**
 * Created by Bryan on 8/8/2017.
 */
public interface IAddMan {

    IAddress getNewAdd(KeyType keyType);

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

    IAddress createNew(String binOrKey, String entropy, String prefix, AddressLength l, boolean p2sh, KeyType keyType);

    List<IAddress> getAll();

    void deleteAddress(IAddress address);

    void associateBinary(IAddress address, Binary bin);

    Binary getBinary(IAddress address);
}
