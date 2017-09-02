package com.lifeform.main.network;

import com.esotericsoftware.kryonet.Listener;

public abstract class IConnectionManager extends Listener{

    abstract boolean isRelay();
    abstract String getID();
    abstract void sendPacket(Object o);
    abstract void disconnect();


}
