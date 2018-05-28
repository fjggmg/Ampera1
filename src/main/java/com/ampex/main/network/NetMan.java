package com.ampex.main.network;

import com.ampex.main.IKi;
import com.ampex.main.data.utils.AmpBuildable;
import com.ampex.main.data.utils.JSONManager;
import com.ampex.main.data.xodus.XodusStringMap;
import com.ampex.main.network.logic.Client;
import com.ampex.main.network.logic.Server;
import com.ampex.main.network.packets.BlockSyncRequest;
import com.ampex.main.network.packets.Ping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetMan extends Thread implements INetworkManager {

    private static final String[] testBoot = {"73.108.51.16"};
    private static final String[] bootstrap = {"mimpve.host"};
    public static final String NET_VER = "2.1.2";
    private IKi ki;
    private boolean isRelay;
    private static final int PORT = 29555;
    private static final int TESTNET_PORT = 29111;
    private boolean live = false;
    private volatile List<IConnectionManager> connections = new CopyOnWriteArrayList<>();
    private volatile Map<String, IConnectionManager> connMap = new ConcurrentHashMap<>();
    private volatile List<String> relays = new ArrayList<>();
    private XodusStringMap rList;
    private boolean DIFF_SET = false;
    private GlobalPacketQueuer gpq = new GlobalPacketQueuer();
    public NetMan(IKi ki,boolean isRelay)
    {
        this.ki = ki;
        this.isRelay = isRelay;
        rList = new XodusStringMap("relays");
        if (rList.get("relays") != null) {
            relays = JSONManager.parseJSONToList(rList.get("relays"));
        }
        ConnMan.init(ki);
    }

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
        for (String relay : relays) {
            if (!this.relays.contains(relay))
                this.relays.add(relay);
        }
        rList.put("relays", JSONManager.parseListToJSON(this.relays).toJSONString());
    }

    @Override
    public List<String> getRelays() {
        return relays;
    }

    @Override
    public void close() {
        interrupt();
        gpq.interrupt();
        rList.close();
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
        DIFF_SET = true;
    }

    @Override
    public boolean isDiffSet() {
        return DIFF_SET;
    }

    @Override
    public boolean isRelay()
    {
        return isRelay;
    }

    @Override
    public void attemptConnect(final String IP) {
        Thread t = new Thread() {

            public void run() {
                setName("client:" + IP);
                ki.debug("Attempting connection to: " + IP);
                Client client = new Client(ki, IP, (ki.getOptions().testNet) ? TESTNET_PORT : PORT);
                IConnectionManager connMan = new ConnMan(ki, isRelay, client);
                //connections.add(connMan);
                try {
                    client.start(connMan);
                } catch (Exception e)
                {
                    if(ki.getOptions().pDebug)
                    ki.debug("Client stopped, error follows: ");
                    e.printStackTrace();
                }
            }
        };
        threads.add(t);
        t.start();
    }

    private List<Thread> threads = new CopyOnWriteArrayList<>();
    @Override
    public void run()
    {
        gpq.start();
        //These are anonymous because java is fucking retarded and won't let you name lambda'd threads
        //they're also anonymous because they do fuck all and aren't worth tracking
        //may possibly track in the future
        Thread nc = new Thread() {
            public void run() {
                setName("Network Cleanup");
                while (true) {
                    List<IConnectionManager> toRemove = new ArrayList<>();
                    for (IConnectionManager connMan : connections) {
                        if (connMan == null || !connMan.isConnected()) {
                            toRemove.add(connMan);
                            if (connMan != null) {
                                connMan.getPacketProcessor().getPacketGlobal().cancelAllResends();
                                //connMan.getPacketProcessor().getThread().interrupt();
                                ki.debug("Cleaning up PacketProcessor: " + connMan.getID());
                            }
                        }
                    }
                    List<Thread> tToRemove = new ArrayList<>();
                    for (Thread t : threads) {
                        if (!t.isAlive()) {
                            tToRemove.add(t);
                        }
                    }
                    if (!toRemove.isEmpty()) {
                        connections.removeAll(toRemove);
                    }
                    if (!tToRemove.isEmpty()) {
                        threads.removeAll(tToRemove);
                    }
                    try {
                        sleep(300000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
        nc.setDaemon(true);
        threads.add(nc);
        nc.start();
        if (!isRelay) {
            Thread t = new Thread() {
                public void run() {
                    setName("BlockSync");
                    while (true) {
                        try {
                            sleep(300000);
                        } catch (InterruptedException e) {
                            return;
                        }

                        BlockSyncRequest bsr = new BlockSyncRequest();
                        bsr.height = ki.getChainMan().currentHeight();
                        broadcast(bsr);
                    }
                }
            };
            t.setDaemon(true);
            t.start();
            threads.add(t);


            Thread t2 = new Thread() {

                public void run() {
                    setName("Ping Thread");
                    while (true) {
                        if (ki.getNetMan().isInterrupted()) return;
                        Ping ping = new Ping();
                        ping.currentTime = System.currentTimeMillis();
                        broadcast(ping);
                        try {
                            sleep(60000);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }
            };
            t2.setDaemon(true);
            threads.add(t2);
            t2.start();
        }
        setName("Networking-Main");
        if (isRelay) {
            Thread t = new Thread() {

                public void run() {
                    setName("server:" + ((ki.getOptions().testNet) ? TESTNET_PORT : PORT));
                    Server server = new Server(ki, (ki.getOptions().testNet) ? TESTNET_PORT : PORT);
                    try {
                        server.start();
                    } catch (Exception e)
                    {
                        ki.debug("Server stopped, error follows: ");
                        e.printStackTrace();
                    }
                }
            };
            threads.add(t);
            t.start();

        }
        List<String> alreadyAttempted = new ArrayList<>();
        for (String ip : (ki.getOptions().testNet) ? testBoot:bootstrap) {
            if (ip == null || ip.isEmpty()) continue;
            if (alreadyAttempted.contains(ip.replace("/", "").split(":")[0])) continue;
            attemptConnect(ip);
            alreadyAttempted.add(ip.replace("/", "").split(":")[0]);
        }

    }

    @Override
    public void interrupt() {
        for (Thread t : threads) {
            t.interrupt();
        }
        super.interrupt();
    }
    @Override
    public List<IConnectionManager> getConnections() {
        return connections;
    }

    private List<Integer> nullConns = new ArrayList<>();
    @Override
    public void broadcast(AmpBuildable o) {
        nullConns.clear();
        if (ki.getOptions().pDebug)
            ki.debug("Beginning broadcast");
        int p = 0;
        while (connections.isEmpty()) {
            if(p > 5)
            {
                ki.debug("We're unable to reconnect at the moment, the relay may be overloaded");
                return;
            }
            if (ki.getOptions().pDebug)
                ki.debug("Connections empty, attempting reconnect");
            for (String ip : bootstrap) {
                attemptConnect(ip);
            }
            try {
                sleep(20000);
            } catch (InterruptedException e) {
                return;
            }
            p++;
        }
        int i = 0;
        for(IConnectionManager connMan:connections)
        {
            if (connMan != null) {
                if (connMan.getPacketProcessor().getPacketGlobal().doneDownloading) {
                    connMan.sendPacket(o);
                    if (ki.getOptions().pDebug)
                        ki.debug("Broadcasting packet: " + o.toString() + " to " + connMan.getAddress());
                } else
                    connMan.queueUntilDone(o);
            } else {
                nullConns.add(i);
            }
            i++;
        }
        if (!nullConns.isEmpty()) {
            for (int index : nullConns) {
                if (ki.getOptions().pDebug)
                    ki.debug("Removing connection: " + index + " because it is null");
                connections.remove(index);
            }
        }
    }

    @Override
    public void broadcastAllBut(String ID, AmpBuildable o) {
        for(IConnectionManager connMan:connections)
        {
            if (connMan != null && ID != null && connMan.getID() != null) {
                if (!connMan.getID().equals(ID)) {
                    if(connMan.getPacketProcessor().getPacketGlobal().doneDownloading)
                        connMan.sendPacket(o);
                    else
                        connMan.queueUntilDone(o);
                }
            }
        }
    }

    @Override
    public IConnectionManager getConnection(String ID) {
        return connMap.get(ID);
    }

    @Override
    public synchronized boolean connectionInit(String ID, IConnectionManager connMan) {

        if (!isRelay) {
            for (IConnectionManager cm : connections) {
                if (cm.getAddress().replace("/", "").split(":")[0].equals(connMan.getAddress().replace("/", "").split(":")[0])) {
                    return false;
                }
            }
        }

        for (IConnectionManager cm : connections) {
            if (cm.getID().equals(ID)) {
                return false;
            }
        }

        connMap.put(ID,connMan);
        connections.add(connMan);
        ki.debug("Connection init for: " + ID + " is complete");
        return true;

    }
}
