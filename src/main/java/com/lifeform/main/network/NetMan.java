package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.network.logic.Client;
import com.lifeform.main.network.logic.Server;
import java.util.*;

public class NetMan extends Thread implements INetworkManager {

    public static final String[] bootstrap = {"73.108.51.16","221.0.236.161","75.74.67.19"};
    public static final String NET_VER = "2.0.4";
    private IKi ki;
    private boolean isRelay;
    public static final int PORT = 29555;
    Set<IConnectionManager> connections = new HashSet<>();
    Map<String,IConnectionManager> connMap = new HashMap<>();
    Map<String,Client> clientMap = new HashMap<>();
    List<Client> clients = new ArrayList<>();
    public NetMan(IKi ki,boolean isRelay)
    {
        this.ki = ki;
        this.isRelay = isRelay;
        //Log.set(Log.LEVEL_TRACE);
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
        new Thread() {

            public void run() {
                setName("client:" + IP);
                Client client = new Client(ki, IP, PORT);
                IConnectionManager connMan = new ConnMan(ki, isRelay, client);
                connections.add(connMan);
                try
                {
                    client.start(connMan);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.start();
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
        for(String ip:bootstrap) {
            Thread t = new Thread() {

                public void run() {
                    setName("client:" + ip);
                    Client client = new Client(ki, ip, PORT);
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

    @Override
    public void broadcast(Object o) {
        if (ki.getOptions().pDebug)
            ki.debug("Beginning broadcast");
        for(IConnectionManager connMan:connections)
        {
            if(!(connMan == null)) {
                if (ki.getOptions().pDebug)
                    ki.debug("Connection Manager not null");
                if (connMan.getPacketProcessor().getPacketGlobal().doneDownloading) {
                    connMan.sendPacket(o);
                    if (ki.getOptions().pDebug)
                        ki.debug("Broadcasting packet: " + o.toString() + " to " + connMan.getAddress());
                }
                else
                    connMan.queueUntilDone(o);
            }
        }
        connections.remove(null);
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
        connections.remove(null);
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
