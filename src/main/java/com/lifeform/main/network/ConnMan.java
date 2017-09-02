package com.lifeform.main.network;

import com.esotericsoftware.kryonet.Connection;
import com.lifeform.main.IKi;

public class ConnMan extends IConnectionManager {

    private IPacketProcessor pp;
    private boolean isRelay;
    private IKi ki;
    private Connection connection;
    private String ID;
    public ConnMan(IKi ki, boolean isRelay, Connection conncetion)
    {
        this(ki,isRelay);
        this.connection = conncetion;
        connected(connection);
    }
    public ConnMan(IKi ki, boolean isRelay, IPacketProcessor pp)
    {
        this(ki,isRelay);
        this.pp = pp;

    }
    public ConnMan(IKi ki, boolean isRelay)
    {
        this.isRelay = isRelay;
        this.ki = ki;
        pp = new PacketProcessor(ki,this);
    }
    @Override
    boolean isRelay() {
        return isRelay;
    }

    @Override
    String getID() {
        return ID;
    }

    @Override
    void sendPacket(Object o) {
        connection.sendTCP(o);
    }

    private boolean process = true;
    @Override
    void disconnect() {
        if(connection != null)
        connection.close();
        process = false;
    }

    @Override
    public void connected(Connection connection)
    {
        ki.debug("Connection established, forming and sending Handshake");
        this.connection = connection;
        Handshake hs = new Handshake();
        hs.isRelay = isRelay;
        hs.currentHeight = ki.getChainMan().currentHeight();
        hs.ID = ki.getEncryptMan().getPublicKeyString();
        hs.mostRecentBlock = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID;
        hs.version = Handshake.VERSION;
        sendPacket(hs);


    }

    @Override
    public void received(Connection connection,Object o)
    {
        ki.debug("Received packet from: " + connection.getID());
        ki.debug("This connection managers ID is: " + this.connection.getID());
        ki.debug("Raw Packet is: " + o.toString());
        if(process && connection.getID() == this.connection.getID())
        pp.process(o);
    }
}
