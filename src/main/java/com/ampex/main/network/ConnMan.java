package com.ampex.main.network;

import com.ampex.amperabase.AmpBuildable;
import com.ampex.amperabase.ConnManPacketPair;
import com.ampex.amperabase.IConnectionManager;
import com.ampex.amperabase.IPacketProcessor;
import com.ampex.amperanet.packets.*;
import com.ampex.main.IKi;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.network.logic.INetworkEndpoint;
import io.netty.channel.Channel;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class ConnMan extends IConnectionManager implements ChannelHandler {

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
    private static final List<String> PASSIVE_WHITELIST = Arrays.asList(
            UTXOData.class.getName(),
            UTXODataEnd.class.getName(),
            UTXODataStart.class.getName(),
            UTXOStartAck.class.getName(),
            Ping.class.getName(),
            Pong.class.getName(),
            Handshake.class.getName()

    );
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
    public void sendPacket(AmpBuildable o) {

        if (getPacketProcessor().getPacketGlobal().passiveConnection()) {
            if (!PASSIVE_WHITELIST.contains(o.getClass().getName())) return;
        }
        endpoint.sendPacket(o);
    }

    @Override
    public void disconnect() {
        if (endpoint.isConnected())
            endpoint.disconnect();
        ki.getNetMan().getConnections().remove(this);

    }

    @Override
    public void connected()
    {
        if(ki.getOptions().lite)
        {
            while(ki.getTransMan() == null) {}
            ki.getTransMan().resetLite();
        }
        ki.debug("Connection established");
        Handshake hs = new Handshake();
        hs.isRelay = isRelay;
        hs.startTime = OURSTARTTIME;
        hs.currentHeight = ki.getChainMan().currentHeight();
        hs.ID = OURID;
        hs.passive = Handshake.usPassive;
        if(ki.getChainMan().currentHeight().compareTo(BigInteger.ZERO) > 0)
            hs.mostRecentBlock = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).getID();
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
