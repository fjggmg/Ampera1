package com.lifeform.main.network.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.network.GlobalPacketQueuer;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.INetworkManager;
import com.lifeform.main.network.logic.Client;
import com.lifeform.main.network.logic.Server;
import com.lifeform.main.network.packets.pool.StatUpdate;
import com.lifeform.main.transactions.Address;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class PoolNetMan extends Thread implements INetworkManager {


    private IKi ki;
    private static final int PORT = 29999;
    private List<IConnectionManager> connections = new CopyOnWriteArrayList<>();
    private Map<String, IConnectionManager> connMap = new HashMap<>();
    public static final String POOL_NET_VERSION = "1.0.3";
    GlobalPacketQueuer gpq = new GlobalPacketQueuer();
    public PoolNetMan(IKi ki) {

        this.ki = ki;


    }

    @Override
    public List<IConnectionManager> getConnections() {
        return connections;
    }

    @Override
    public void broadcast(Object o) {

        if (ki.getOptions().pool) {
            for (IConnectionManager cm : connections) {
                if (!cm.isConnected()) {
                    attemptConnect(ki.getPoolData().poolConn);
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        List<IConnectionManager> toRemove = new ArrayList<>();
        for (IConnectionManager c : connections) {
            if (c.isConnected())
            c.sendPacket(o);
            else
                toRemove.add(c);

        }
        for (IConnectionManager c : toRemove) {
            if (ki.getOptions().poolRelay) {
                ki.getPoolData().hrMap.remove(c.getID());
                ki.getPoolData().addMap.remove(c.getID());

            }
            c.disconnect();
        }
        connections.removeAll(toRemove);


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
        ki.debug("Registering connection");
        connections.add(connMan);
        connMap.put(ID, connMan);
        return true;
    }

    @Override
    public boolean isRelay() {
        return false;
    }

    List<Thread> threads = new ArrayList<>();

    @Override
    public void attemptConnect(String IP) {
        if (connections.size() > 0) return; //only one pool connection for the client
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

    public void run() {
        gpq.start();
        if (ki.getOptions().poolRelay) {
            ki.debug("Starting pool server");
            Thread t = new Thread() {

                public void run() {
                    setName("server:" + PORT);
                    Server server = new Server(ki, PORT);
                    try {
                        server.start();
                    } catch (Exception e) {
                        ki.debug("Server stopped, error follows: ");
                        e.printStackTrace();
                    }
                }
            };
            threads.add(t);
            t.start();
            if (ki.getOptions().pool) {
                new Thread() {
                    public void run() {
                        while (true) {
                            setName("ReconnectThread");
                            if (connections.size() < 1 && ki.getPoolData().poolConn != null && !ki.getPoolData().poolConn.isEmpty()) {
                                attemptConnect(ki.getPoolData().poolConn);
                            }
                            try {
                                sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }
            Thread t2 = new Thread() {

                public void run() {
                    setName("StatUpdate");
                    while (true) {
                        List<IConnectionManager> toRemove = new ArrayList<>();
                        for (IConnectionManager conn : connections) {
                            StatUpdate su = new StatUpdate();
                            if (conn == null) {
                                ki.getMainLog().warn("Skipping connection in pool stat update since it is null");
                                continue;
                            }
                            if (!conn.isConnected()) {
                                conn.disconnect();
                                connMap.remove(conn.getID());
                                ki.getPoolData().hrMap.remove(conn.getID());
                                toRemove.add(conn);
                            }
                            if (ki.getPoolData().addMap.get(conn.getID()) == null) {
                                ki.getMainLog().warn("Skipping connection: " + conn.getID() + " in stat update since we don't have address for it");
                                continue;
                            }
                            try {
                                su.shares = ki.getPoolManager().getTotalSharesOfMiner(Address.decodeFromChain(ki.getPoolData().addMap.get(conn.getID())));

                            } catch (Exception e) {
                                ki.getMainLog().error("Unable to get shares of miner for stat update", e);
                                continue;
                            }
                            su.currentPPS = ki.getPoolManager().getCurrentPayPerShare();

                            conn.sendPacket(su);
                        }
                        connections.removeAll(toRemove);
                        try {
                            sleep(30000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            threads.add(t2);
            t2.start();
        }
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
        for (IConnectionManager conn : connections) {
            if (conn != null && conn.isConnected())
                conn.disconnect();
        }
    }

    @Override
    public GlobalPacketQueuer getGPQ() {
        return gpq;
    }

    @Override
    public void diffSet() {

    }

    @Override
    public boolean isDiffSet() {
        return false;
    }
}
