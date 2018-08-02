package com.ampex.main.network.pool;

import com.ampex.amperabase.AmpBuildable;
import com.ampex.amperabase.ConnManPacketPair;
import com.ampex.amperabase.IConnectionManager;
import com.ampex.amperabase.IPacketProcessor;
import com.ampex.main.IKi;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.network.ChannelHandler;
import com.ampex.main.network.logic.INetworkEndpoint;
import com.ampex.main.network.packets.pool.PoolHandshake;
import io.netty.channel.Channel;

public class PoolConnMan extends IConnectionManager implements ChannelHandler {


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
    public void sendPacket(AmpBuildable o) {
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
        if (!ki.getOptions().poolRelay) {
            if (ki.getPoolData().payTo == null) {
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
