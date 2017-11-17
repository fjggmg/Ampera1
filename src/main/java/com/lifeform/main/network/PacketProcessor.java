package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.blockchain.CPUMiner;
import com.lifeform.main.blockchain.ChainManager;
import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.Input;
import com.lifeform.main.transactions.Transaction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketProcessor implements IPacketProcessor{

    private IKi ki;
    public PacketProcessor(IKi ki,IConnectionManager connMan)
    {
        this.connMan = connMan;
        this.ki = ki;
        new Thread() {
            public void run() {
                setName("PacketProcessor");
                heartbeat();
            }

        }.start();
        pg = new PacketGlobal(ki, connMan);
    }

    private void heartbeat()
    {
        while(run)
        {
            //ki.debug("Heartbeat of packet processor, current queue size: " + packets.size());
            if(packets.size() > 0)
            {
                if (packets.get(0) == null) {
                    packets.remove(0);
                    continue;
                }
                if(ki.getOptions().pDebug)
                ki.debug("Processing packet: " + packets.get(0).toString());
                process(packets.get(0));
                packets.remove(0);
            }
            if (packets.size() == 0) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private volatile List<Object> packets = new ArrayList<>();
    private boolean run = true;
    private IConnectionManager connMan;
    private PacketGlobal pg;
    @Override
    public void process(Object packet) {
        //TODO: investigate if we even need this typecheck, afaik netty should break if it receives something that it doesn't recognize
        if (packet instanceof Packet)
        {
            //TODO we may not even need the packet types? testing will be done here
            ((Packet) packet).process(ki, connMan, pg);
        } else {
            if(ki.getOptions().pDebug)
            ki.debug("Received unknown packet from " + connMan.getAddress());
        }
    }

    @Override
    public void enqueue(Object packet) {
        if(ki.getOptions().pDebug)
        ki.debug("Enqueued new packet for processing: " + packet.toString());
        packets.add(packet);
    }

    @Override
    public PacketGlobal getPacketGlobal() {
        return pg;
    }
}