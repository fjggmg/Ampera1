package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.util.ArrayList;
import java.util.List;

public class PacketProcessor implements IPacketProcessor{

    private IKi ki;
    private int ncTimes = 0;
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
            if (connMan != null)
                try {
                    if (connMan.getChannel() != null)
                        if (!connMan.isConnected()) {
                            if (ncTimes > 1000) {
                                if(ki.getOptions().pDebug)
                                ki.debug("Disconnecting: " + connMan.getAddress() + " because the connection appears to already be dead");
                                connMan.disconnect();
                                ki.getNetMan().getConnections().remove(connMan);
                                return;

                            }
                            ncTimes++;
                        }
                } catch (Exception e) {

                }

            //ki.debug("Heartbeat of packet processor, current queue size: " + packets.size());
            if(packets.size() > 0)
            {
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
                    ki.debug("Error while processing packet on connection to: " + connMan.getAddress());
                } finally {
                    packets.remove(0);
                }
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
        //if(ki.getOptions().pDebug)
        //ki.debug("Enqueued new packet for processing: " + packet.toString());
        packets.add(packet);
    }

    @Override
    public PacketGlobal getPacketGlobal() {
        return pg;
    }
}