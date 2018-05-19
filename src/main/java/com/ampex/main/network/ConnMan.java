package com.ampex.main.network;

import com.ampex.main.IKi;
import com.ampex.main.data.EncryptionManager;
import com.ampex.main.network.logic.INetworkEndpoint;
import com.ampex.main.network.packets.Handshake;
import com.ampex.main.transactions.TransactionManagerLite;
import io.netty.channel.Channel;

import java.math.BigInteger;

public class ConnMan extends IConnectionManager {

    private IPacketProcessor pp;
    private boolean isRelay;
    private IKi ki;
    private String ID;
    private INetworkEndpoint endpoint;
    private long currentLatency;
    private long startTime;
    private static long OURSTARTTIME = System.currentTimeMillis();
    private static String OURID;
    private volatile boolean gotHS = false;
    public ConnMan(IKi ki, boolean isRelay, INetworkEndpoint endpoint, IPacketProcessor pp)
    {
        this(ki,isRelay,endpoint);
        this.pp = pp;
    }

    public static void init(IKi ki)
    {
        OURID = EncryptionManager.sha224(ki.getEncryptMan().getPublicKeyString(ki.getAddMan().getMainAdd().getKeyType()) + OURSTARTTIME);
    }

    public ConnMan(IKi ki, boolean isRelay, INetworkEndpoint endpoint) {
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

        endpoint.sendPacket(o);
    }

    @Override
    public void disconnect() {
        endpoint.disconnect();
        ki.getNetMan().getConnections().remove(this);

    }

    @Override
    public void connected()
    {
        if(ki.getOptions().lite)
        {
            while(ki.getTransMan() == null) {}
            ((TransactionManagerLite)ki.getTransMan()).resetLite();
        }
        ki.debug("Connection established");
        Handshake hs = new Handshake();
        hs.isRelay = isRelay;
        hs.startTime = OURSTARTTIME;
        hs.currentHeight = ki.getChainMan().currentHeight();
        hs.ID = OURID;
        if(ki.getChainMan().currentHeight().compareTo(BigInteger.ZERO) > 0)
            hs.mostRecentBlock = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID;
        else
            hs.mostRecentBlock = "";
        hs.version = Handshake.VERSION;
        hs.chainVer = ki.getChainMan().getChainVer();
        sendPacket(hs);

        new Thread() {
            public void run() {
                setName("ConnManCleanup");
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!gotHS) {
                    disconnect();
                }
            }
        }.start();


    }

    @Override
    public boolean isConnected() {
        return endpoint.isConnected();
    }

    @Override
    public void gotHS() {
        gotHS = true;
    }

    @Override
    public Channel getChannel() {
        return endpoint.getChannel().channel();
    }

    @Override
    public long currentLatency() {
        return currentLatency;
    }

    @Override
    public void setCurrentLatency(long latency) {
        currentLatency = latency;
    }

    @Override
    public long uptime() {
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public IPacketProcessor getPacketProcessor() {
        return pp;
    }

    @Override
    public String getAddress() {
        return endpoint.getAddress();
    }

    @Override
    public void received(Object o) {
        ConnManPacketPair cmpp = new ConnManPacketPair();
        cmpp.connMan = this;
        cmpp.packet = o;
        ki.getNetMan().getGPQ().enqueue(cmpp);
    }
}
