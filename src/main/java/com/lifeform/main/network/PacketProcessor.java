package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.util.ArrayList;
import java.util.List;

public class PacketProcessor implements IPacketProcessor{

    private IKi ki;
    private int ncTimes = 0;
    private Thread heartbeat;
    public PacketProcessor(IKi ki,IConnectionManager connMan)
    {
        this.connMan = connMan;
        this.ki = ki;
        heartbeat = new Thread() {
            public void run() {
                setName("PacketProcessor");
                heartbeat();

            }

        };
        heartbeat.start();
        pg = new PacketGlobal(ki, connMan);
    }
    public Thread getThread()
    {
        return heartbeat;
    }
    private void heartbeat()
    {
        while(run)
        {
            synchronized (packets) {
                if (connMan != null)
                    try {
                        if (connMan.getChannel() != null)
                            if (!connMan.isConnected()) {
                                if (ncTimes > 100000000) {
                                    if (ki.getOptions().pDebug)
                                        ki.debug("Disconnecting: " + connMan.getAddress() + " because the connection appears to already be dead");
                                    connMan.disconnect();
                                    ki.getNetMan().getConnections().remove(connMan);
                                    heartbeat.interrupt();
                                    return;

                                }
                                ncTimes++;
                            }
                    } catch (Exception e) {

                    }

                //ki.debug("Heartbeat of packet processor, current queue size: " + packets.size());
                if (packets.size() > 0) {
                    ncTimes = 0;
                    if (packets.get(0) == null) {
                        packets.remove(0);
                        continue;
                    }
                    //if(ki.getOptions().pDebug)
                    //ki.debug("Processing packet: " + packets.get(0).toString());
                    try {
                        process(packets.get(0));

                    } catch (Exception e) {
                        e.printStackTrace();
                        ki.debug("Error while processing packet on connection to: " + connMan.getAddress());
                    } finally {
                        packets.remove(0);
                    }
                }
                if (packets.size() == 0) {
                    try {
                        packets.wait();
                    } catch (InterruptedException e) {
                        if(ki.getOptions().pDebug)
                       ki.debug("PacketProccessor: " + connMan.getID() + " has been killed");
                        return;
                    }
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
        //if(ki.getOptions().pDebug)
        //ki.debug("Enqueued new packet for processing: " + packet.toString());
        synchronized (packets) {
            packets.add(packet);
            packets.notifyAll();
        }
    }

    @Override
    public PacketGlobal getPacketGlobal() {
        return pg;
    }
}