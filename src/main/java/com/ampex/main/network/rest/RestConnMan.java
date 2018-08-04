package com.ampex.main.network.rest;

import com.ampex.amperabase.AmpBuildable;
import com.ampex.amperabase.IConnectionManager;
import com.ampex.amperabase.IKiAPI;
import com.ampex.amperabase.IPacketProcessor;
import com.ampex.amperanet.packets.rest.IRestPacket;
import com.ampex.main.network.logic.INetworkEndpoint;

public class RestConnMan extends IConnectionManager {

    private IKiAPI ki;
    private INetworkEndpoint endpoint;
    RestConnMan(IKiAPI ki, INetworkEndpoint endpoint)
    {
        this.ki = ki;
        this.endpoint = endpoint;
    }
    @Override
    public boolean isRelay() {
        return false;
    }

    @Override
    public String getID() {
        return null;
    }

    @Override
    public void sendPacket(AmpBuildable ampBuildable) {
        endpoint.sendPacket(ampBuildable);
    }

    @Override
    public void disconnect() {
        endpoint.disconnect();
    }

    @Override
    public void received(Object o) {
        if(o instanceof IRestPacket)
        {
            ((IRestPacket)o).process(ki,this,null);
        }

    }

    @Override
    public void setID(String s) {

    }

    @Override
    public void connected() {

    }

    @Override
    public IPacketProcessor getPacketProcessor() {
        return null;
    }

    @Override
    public String getAddress() {
        return null;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void gotHS() {

    }

    @Override
    public long currentLatency() {
        return 0;
    }

    @Override
    public void setCurrentLatency(long l) {

    }

    @Override
    public long uptime() {
        return 0;
    }

    @Override
    public void setStartTime(long l) {

    }
}
