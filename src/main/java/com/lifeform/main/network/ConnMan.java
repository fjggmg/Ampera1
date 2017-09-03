package com.lifeform.main.network;

import com.esotericsoftware.kryonet.Connection;
import com.lifeform.main.IKi;

import java.math.BigInteger;

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
        //connected(connection);
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
    void sendPacket(Object o) {

        ki.debug("Sending packet: " + o.toString());
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
        if(ki.getChainMan().currentHeight().compareTo(BigInteger.ZERO) > 0)
            hs.mostRecentBlock = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID;
        else
            hs.mostRecentBlock = "";
        hs.version = Handshake.VERSION;
        sendPacket(hs);


    }

    @Override
    public void received(Object o)
    {
        ki.debug("Received packet from: " + connection.getID());
        ki.debug("This connection managers ID is: " + this.connection.getID());
        ki.debug("Raw Packet is: " + o.toString());
        if(process && connection.getID() == this.connection.getID())
        pp.process(o);
    }
}
