package com.lifeform.main.network.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.network.ConnManPacketPair;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.IPacketProcessor;
import com.lifeform.main.network.logic.INetworkEndpoint;
import com.lifeform.main.network.packets.pool.PoolHandshake;
import io.netty.channel.Channel;

public class PoolConnMan extends IConnectionManager {


    public PoolConnMan(IKi ki, INetworkEndpoint endpoint) {
        this.ki = ki;
        this.endpoint = endpoint;
        ppp = new PoolPacketProcessor(ki, this);
    }

    private IKi ki;
    private PoolPacketProcessor ppp;
    private INetworkEndpoint endpoint;
    private String ID;

    @Override
    public boolean isRelay() {
        return false;
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public void sendPacket(Object o) {
        endpoint.sendPacket(o);
    }

    @Override
    public void disconnect() {
        endpoint.disconnect();
    }

    @Override
    public void received(Object o) {

        ConnManPacketPair cmpp = new ConnManPacketPair();
        cmpp.connMan = this;
        cmpp.packet = o;
        ki.getPoolNet().getGPQ().enqueue(cmpp);
    }

    @Override
    public void setID(String ID) {
        this.ID = ID;
    }

    @Override
    public void connected() {
        ki.debug("Pool connection established");

        PoolHandshake ph = new PoolHandshake();
        if (ki.getOptions().pool) {
            if (ki.getPoolData().payTo == null || ki.getPoolData().ID == null) {
                disconnect();
                return;
            }
            ki.getPoolData().ID = EncryptionManager.sha224(ki.getPoolData().payTo.encodeForChain() + System.currentTimeMillis());
            ph.ID = ki.getPoolData().ID;
            ph.address = ki.getPoolData().payTo.encodeForChain();
        } else {
            ph.ID = EncryptionManager.sha224(ki.getAddMan().getMainAdd().encodeForChain());
            ph.address = ki.getAddMan().getMainAdd().encodeForChain();
        }
        ph.version = PoolNetMan.POOL_NET_VERSION;
        sendPacket(ph);
    }

    @Override
    public IPacketProcessor getPacketProcessor() {
        return ppp;
    }

    @Override
    public String getAddress() {
        return endpoint.getAddress();
    }

    @Override
    public boolean isConnected() {
        return endpoint.isConnected();
    }

    @Override
    public void gotHS() {

    }

    @Override
    public Channel getChannel() {
        return endpoint.getChannel().channel();
    }

    @Override
    public long currentLatency() {
        return 0;
    }

    @Override
    public void setCurrentLatency(long latency) {

    }

    @Override
    public long uptime() {
        return System.currentTimeMillis() - startTime;
    }

    long startTime = 0;

    @Override
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
