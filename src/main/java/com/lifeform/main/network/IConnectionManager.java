package com.lifeform.main.network;

public abstract class IConnectionManager{

    public abstract boolean isRelay();
    public abstract String getID();
    public abstract void sendPacket(Object o);
    public abstract void disconnect();
    public abstract void received(Object o);
    public abstract void setID(String ID);
    public abstract void connected();

}
