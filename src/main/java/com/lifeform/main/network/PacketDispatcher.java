package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.network.logic.INetworkEndpoint;

import java.util.ArrayList;
import java.util.List;

public class PacketDispatcher {
    private IKi ki;
    private IConnectionManager connMan;
    private INetworkManager netMan;

    public PacketDispatcher(IKi ki, IConnectionManager connMan, INetworkManager netMan) {
        this.ki = ki;
        this.connMan = connMan;
        this.netMan = netMan;
        new Thread() {
            public void run() {
                setName("PacketDispatcher");
                heartbeat();
            }
        }.start();
    }

    private boolean run = true;
    private volatile List<BABPair> babPackets = new ArrayList<>();
    private volatile List<Object> bPackets = new ArrayList<>();
    private volatile List<Object> packets = new ArrayList<>();

    public void enqueue(Object packet) {
        packets.add(packet);
    }

    public void enqueuBroadcast(Object packet) {
        bPackets.add(packet);
    }

    public void enqueueBroadcastAllBut(String ID, Object packet) {
        babPackets.add(new BABPair(ID, packet));
    }

    public void heartbeat() {
        while (run) {
            if (packets.size() > 0) {
                connMan.sendPacket(packets.get(0));
                packets.remove(0);
            }
            if (bPackets.size() > 0) {
                netMan.broadcast(bPackets.get(0));
                bPackets.remove(0);
            }
            if (babPackets.size() > 0) {
                netMan.broadcastAllBut(babPackets.get(0).ID, babPackets.get(0).packet);
                babPackets.remove(0);
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
