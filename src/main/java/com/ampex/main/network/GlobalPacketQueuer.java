package com.ampex.main.network;

import com.ampex.main.Ki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalPacketQueuer extends Thread {

    private final List<ConnManPacketPair> cmppList = new ArrayList<>();

    public void enqueue(ConnManPacketPair cmpp) {
        synchronized (cmppList) {
            cmppList.add(cmpp);
            cmppList.notifyAll();
        }
    }

    private Map<String, Thread> connThreads = new HashMap<>();
    public void run() {
        synchronized (cmppList) {
            setName("GlobalPacketQueuer");
            while (true) {

                while (!cmppList.isEmpty()) {

                    if (cmppList.get(0).connMan != null && cmppList.get(0).packet != null) {
                        IConnectionManager connMan = cmppList.get(0).connMan;
                        if (connMan != null) {
                            if (connMan.getID() != null && connThreads.get(connMan.getID()) != null && connThreads.get(connMan.getID()).isAlive())
                                continue;
                            Object packet = cmppList.get(0).packet;
                            Thread t = new Thread(() -> {
                                setName("PacketProcessingThread");
                                try {
                                    connMan.getPacketProcessor().process(packet);
                                } catch (Exception e) {
                                    //get ki in here for debug
                                    Ki.getInstance().getMainLog().error("Failed to process packet", e);
                                }
                            });
                            connThreads.put(connMan.getID(), t);
                            t.start();
                        }
                    }
                    cmppList.remove(0);
                }
                try {
                    cmppList.wait();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

    }
}
