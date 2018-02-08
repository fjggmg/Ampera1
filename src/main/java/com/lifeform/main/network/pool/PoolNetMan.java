package com.lifeform.main.network.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.ChainManager;
import com.lifeform.main.blockchain.GPUMiner;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.INetworkManager;
import com.lifeform.main.network.logic.Client;
import com.lifeform.main.network.logic.Server;
import com.lifeform.main.transactions.Address;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class PoolNetMan extends Thread implements INetworkManager {

    private IKi ki;
    private static final int PORT = 29999;
    private Set<IConnectionManager> connections = new HashSet<>();
    private Map<String, IConnectionManager> connMap = new HashMap<>();
    public static final String POOL_NET_VERSION = "1.0.0";
    public PoolNetMan(IKi ki) {
        this.ki = ki;
    }

    @Override
    public Set<IConnectionManager> getConnections() {
        return connections;
    }

    @Override
    public void broadcast(Object o) {
        List<IConnectionManager> toRemove = new ArrayList<>();
        for (IConnectionManager c : connections) {
            if (c.isConnected())
                c.sendPacket(o);
            else
                toRemove.add(c);
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

    @Override
    public Client getClient(String ID) {
        return null;
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

            Thread t2 = new Thread() {

                public void run() {
                    setName("StatUpdate");
                    while (true) {
                        for (IConnectionManager conn : connections) {
                            StatUpdate su = new StatUpdate();
                            su.shares = ki.getPoolManager().getTotalSharesOfMiner(Address.decodeFromChain(ki.getPoolData().addMap.get(conn.getID())));
                            BigDecimal sd = new BigDecimal(GPUMiner.shareDiff);
                            BigDecimal cd = new BigDecimal(ki.getChainMan().getCurrentDifficulty());
                            su.currentPPS = (double) ((((cd.divide(sd, 9, RoundingMode.HALF_DOWN).doubleValue() * ChainManager.blockRewardForHeight(ki.getChainMan().currentHeight()).longValueExact()) * 0.99)));

                            conn.sendPacket(su);
                        }
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

    }
}
