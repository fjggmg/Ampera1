package com.lifeform.main.network;


import com.lifeform.main.IKi;
import com.lifeform.main.network.logic.Client;
import com.lifeform.main.network.logic.Server;

import java.util.*;

public class NetMan extends Thread implements INetworkManager {

    public static final String[] bootstrap = {"73.108.51.16","221.0.236.161","75.74.67.19"};
    public static final String NET_VER = "2.0.0";
    private IKi ki;
    private boolean isRelay;
    public static final int PORT = 29555;
    public static final int WRITE_BUFFER = 150000000;
    public static final int OBJECT_BUFFER = 60000000;
    Set<IConnectionManager> connections = new HashSet<>();
    Map<String,IConnectionManager> connMap = new HashMap<>();
    Map<Integer,IConnectionManager> kryoMap = new HashMap<>();
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
    public void run()
    {
        setName("Networking-Main");
        if (isRelay) {
            new Thread() {

                public void run() {
                    setName("server:" + PORT);
                    Server server = new Server(ki, PORT);
                    try

                    {
                        server.start();
                    } catch (
                            Exception e)

                    {
                        e.printStackTrace();
                    }
                }
            }.start();

        }
        for(String ip:bootstrap) {
            new Thread() {

                public void run() {
                    setName("client:" + ip);
                    Client client = new Client(ki, ip, PORT);
                    IConnectionManager connMan = new ConnMan(ki, isRelay, client);
                    connections.add(connMan);
                    try

                    {
                        client.start(connMan);
                    } catch (
                            Exception e)

                    {
                        e.printStackTrace();
                    }
                }
            }.start();

        }
    }

    @Override
    public Set<IConnectionManager> getConnections() {
        return connections;
    }

    @Override
    public void broadcast(Object o) {
        for(IConnectionManager connMan:connections)
        {
            if(!(connMan == null)) {
                connMan.sendPacket(o);
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
                    connMan.sendPacket(o);
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
