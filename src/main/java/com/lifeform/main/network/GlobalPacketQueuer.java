package com.lifeform.main.network;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GlobalPacketQueuer extends Thread {

    private List<ConnManPacketPair> cmppList = new CopyOnWriteArrayList<>();

    public void enqueue(ConnManPacketPair cmpp) {
        synchronized (cmppList) {
            cmppList.add(cmpp);
            cmppList.notifyAll();
        }
    }

    public void run() {
        synchronized (cmppList) {
            setName("GlobalPacketQueuer");
            while (true) {
                while (!cmppList.isEmpty()) {
                    if (cmppList.get(0).connMan != null && cmppList.get(0).packet != null) {
                        cmppList.get(0).connMan.getPacketProcessor().process(cmppList.get(0).packet);
                    }
                    cmppList.remove(0);
                }
                try {
                    cmppList.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
