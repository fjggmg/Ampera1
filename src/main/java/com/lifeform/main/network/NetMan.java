package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.data.JSONManager;
import com.lifeform.main.data.files.StringFileHandler;
import com.lifeform.main.network.logic.Client;
import com.lifeform.main.network.logic.Server;
import java.util.*;

public class NetMan extends Thread implements INetworkManager {

    public static final String[] bootstrap = {"73.108.51.16","221.0.236.161","75.74.67.19"};
    public static final String NET_VER = "2.0.6";
    private IKi ki;
    private boolean isRelay;
    public static final int PORT = 29555;
    public boolean live = false;
    volatile Set<IConnectionManager> connections = new HashSet<>();
    volatile Map<String, IConnectionManager> connMap = new HashMap<>();
    volatile Map<String, Client> clientMap = new HashMap<>();
    volatile List<Client> clients = new ArrayList<>();
    private volatile List<String> relays = new ArrayList<>();
    private StringFileHandler rList;
    public NetMan(IKi ki,boolean isRelay)
    {
        this.ki = ki;
        this.isRelay = isRelay;
        rList = new StringFileHandler(ki, "relays.json");
        if (rList.getLines() != null && rList.getLine(0) != null && !rList.getLine(0).isEmpty()) {
            relays = JSONManager.parseJSONToList(rList.getLine(0));
        }
        //Log.set(Log.LEVEL_TRACE);
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
        rList.replaceLine(0, JSONManager.parseListToJSON(this.relays).toJSONString());
    }

    @Override
    public boolean isRelay()
    {
        return isRelay;
    }

    @Override
    public Client getClient(String ID) {
        return clientMap.get(ID);
    }

    @Override
    public void attemptConnect(final String IP) {
        Thread t = new Thread() {

            public void run() {
                setName("client:" + IP);
                Client client = new Client(ki, IP, PORT);
                IConnectionManager connMan = new ConnMan(ki, isRelay, client);
                connections.add(connMan);
                try {
                    client.start(connMan);
                } catch (Exception e)
                {
                    ki.debug("Client stopped, error follows: ");
                    e.printStackTrace();
                }
            }
        };
        threads.add(t);
        t.start();
    }

    private List<Thread> threads = new ArrayList<>();
    @Override
    public void run()
    {
        setName("Networking-Main");
        if (isRelay) {
            Thread t = new Thread() {

                public void run() {
                    setName("server:" + PORT);
                    Server server = new Server(ki, PORT);
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
        if (!relays.isEmpty()) {
            for (String ip : relays) {
                attemptConnect(ip);
            }
        }
        if (connections.size() < 4) {
            for (String ip : bootstrap) {

                attemptConnect(ip);
            }
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
    public Set<IConnectionManager> getConnections() {
        return connections;
    }

    List<Integer> nullConns = new ArrayList<>();
    @Override
    public void broadcast(Object o) {
        nullConns.clear();
        if (ki.getOptions().pDebug)
            ki.debug("Beginning broadcast");
        if (connections.isEmpty()) {
            if (ki.getOptions().pDebug)
                ki.debug("Connections empty, attempting reconnect");
            for (String ip : bootstrap) {
                attemptConnect(ip);
            }
            try {
                sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            broadcast(o);
        }

        int i = 0;
        for(IConnectionManager connMan:connections)
        {
            ki.debug("Attempting broadcast...");
            if(!(connMan == null)) {
                if (ki.getOptions().pDebug)
                    ki.debug("Connection Manager not null");
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
        //connections.remove(null);
    }

    @Override
    public void broadcastAllBut(String ID, Object o) {
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
        //connections.remove(null);
    }

    @Override
    public IConnectionManager getConnection(String ID) {
        return connMap.get(ID);
    }

    @Override
    public void connectionInit(String ID, IConnectionManager connMan) {

        connMap.put(ID,connMan);
        connections.add(connMan);

    }
}
