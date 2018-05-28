package com.ampex.main.network;

import com.ampex.main.data.utils.AmpBuildable;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

public abstract class IConnectionManager{

    public abstract boolean isRelay();
    public abstract String getID();

    public abstract void sendPacket(AmpBuildable o);
    public abstract void disconnect();
    public abstract void received(Object o);
    public abstract void setID(String ID);
    public abstract void connected();
    public abstract IPacketProcessor getPacketProcessor();
    public abstract String getAddress();

    private List<AmpBuildable> queue = new ArrayList<>();

    public abstract boolean isConnected();

    public void queueUntilDone(AmpBuildable packet)
    {
        queue.add(packet);
    }
    public void doneDownloading()
    {
        for (AmpBuildable p : queue)
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
