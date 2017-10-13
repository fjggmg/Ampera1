package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.network.logic.INetworkEndpoint;
import java.math.BigInteger;

public class ConnMan extends IConnectionManager {

    private IPacketProcessor pp;
    private boolean isRelay;
    private IKi ki;
    private String ID;
    private INetworkEndpoint endpoint;
    public ConnMan(IKi ki, boolean isRelay, INetworkEndpoint endpoint, IPacketProcessor pp)
    {
        this(ki,isRelay,endpoint);
        this.pp = pp;

    }
    public ConnMan(IKi ki, boolean isRelay, INetworkEndpoint endpoint)
    {
        this.isRelay = isRelay;
        this.ki = ki;
        pp = new PacketProcessor(ki,this);
        this.endpoint = endpoint;
    }
    @Override
    public boolean isRelay() {
        return isRelay;
    }

    @Override
    public String getID() {
        return ID;
    }
    @Override
    public void setID(String ID)
    {
        this.ID = ID;
    }
    @Override
    public void sendPacket(Object o) {

        ki.debug("Sending packet: " + o.toString());
        //connection.sendTCP(new FrameworkMessage.KeepAlive());
        endpoint.sendPacket(o);
    }

    private boolean process = true;
    @Override
    public void disconnect() {

        process = false;
        endpoint.disconnect();
        ki.getNetMan().getConnections().remove(this);

    }

    @Override
    public void connected()
    {
        ki.debug("Connection established, forming and sending Handshake");
        //sendPacket("This is a test 5");
        Handshake hs = new Handshake();
        hs.isRelay = isRelay;
        hs.currentHeight = ki.getChainMan().currentHeight();
        hs.ID = ki.getEncryptMan().getPublicKeyString();
        if(ki.getChainMan().currentHeight().compareTo(BigInteger.ZERO) > 0)
            hs.mostRecentBlock = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID;
        else
            hs.mostRecentBlock = "";
        hs.version = Handshake.VERSION;
        hs.chainVer = Handshake.CHAIN_VER;
        sendPacket(hs);

    }

    @Override
    public String getAddress() {
        return endpoint.getAddress();
    }

    @Override
    public void received(Object o) {
       pp.enqueue(o);
    }
}
