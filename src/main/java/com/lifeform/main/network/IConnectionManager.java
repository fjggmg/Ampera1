package com.lifeform.main.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public abstract class IConnectionManager{

    abstract boolean isRelay();
    abstract String getID();
    abstract void sendPacket(Object o);
    abstract void disconnect();
    abstract void received(Object o);
    abstract void connected(Connection conn);
    abstract void setID(String ID);

}
