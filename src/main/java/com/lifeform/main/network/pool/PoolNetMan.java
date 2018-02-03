package com.lifeform.main.network.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.network.ConnMan;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.INetworkManager;
import com.lifeform.main.network.logic.Client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PoolNetMan extends Thread implements INetworkManager {

    private IKi ki;
    private static final int PORT = 29999;
    private Set<IConnectionManager> connections = new HashSet<>();

    public PoolNetMan(IKi ki) {
        this.ki = ki;
    }

    @Override
    public Set<IConnectionManager> getConnections() {
        return connections;
    }

    @Override
    public void broadcast(Object o) {
        for (IConnectionManager c : connections) {
            c.sendPacket(o);
        }
    }

    @Override
    public void broadcastAllBut(String ID, Object o) {
        for (IConnectionManager c : connections) {
            if (!c.getID().equals(ID)) c.sendPacket(o);
        }
    }

    @Override
    public IConnectionManager getConnection(String ID) {
        for (IConnectionManager c : connections) {
            if (c.getID().equals(ID)) return c;
        }
        return null;
    }

    @Override
    public boolean connectionInit(String ID, IConnectionManager connMan) {
        return true;
    }

    @Override
    public boolean isRelay() {
        return false;
    }

    @Override
    public Client getClient(String ID) {
        return null;
    }

    List<Thread> threads = new ArrayList<>();

    @Override
    public void attemptConnect(String IP) {
        Thread t = new Thread() {

            public void run() {
                setName("client:" + IP);
                ki.debug("Attempting connection to: " + IP);
                Client client = new Client(ki, IP, PORT);
                IConnectionManager connMan = new PoolConnMan(ki, client);
                //connections.add(connMan);
                try {
                    client.start(connMan);
                } catch (Exception e) {
                    if (ki.getOptions().pDebug)
                        ki.debug("Client stopped, error follows: ");
                    e.printStackTrace();
                }
            }
        };
        threads.add(t);
        t.start();
    }

    private boolean live = false;

    @Override
    public boolean live() {
        return live;
    }

    @Override
    public void setLive(boolean live) {
        this.live = live;
    }

    @Override
    public void addRelays(List<String> relays) {

    }

    @Override
    public List<String> getRelays() {
        return null;
    }

    @Override
    public void close() {

    }
}
