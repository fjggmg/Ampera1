package com.ampex.main.network;

import io.netty.channel.Channel;

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

    public abstract boolean isConnected();
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

    public abstract void gotHS();
    public abstract Channel getChannel();
    public abstract long currentLatency();

    public abstract void setCurrentLatency(long latency);

    public abstract long uptime();

    public abstract void setStartTime(long startTime);

}
