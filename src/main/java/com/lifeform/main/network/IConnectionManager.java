package com.lifeform.main.network;

import java.util.ArrayList;
import java.util.List;

public abstract class IConnectionManager{

    public abstract boolean isRelay();
    public abstract String getID();
    public abstract void sendPacket(Object o);
    public abstract void disconnect();
    public abstract void received(Object o);
    public abstract void setID(String ID);
    public abstract void connected();
    public abstract IPacketProcessor getPacketProcessor();
    public abstract String getAddress();
    private List<Object> queue = new ArrayList<>();

    public void queueUntilDone(Object packet)
    {
        queue.add(packet);
    }
    public void doneDownloading()
    {
        for(Object p:queue)
        {
            sendPacket(p);
        }
    }

}
