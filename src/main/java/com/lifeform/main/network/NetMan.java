package com.lifeform.main.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.lifeform.main.IKi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetMan extends Thread implements INetworkManager {

    public static final String[] bootstrap = {"73.108.51.16","221.0.236.161","75.74.67.19"};
    public static final String NET_VER = "1.0.0";
    private IKi ki;
    private boolean isRelay;
    public static final int PORT = 29555;
    public static final int IN_BUFFER = 100000000;
    public static final int OUT_BUFFER = 100000000;
    List<IConnectionManager> connections = new ArrayList<>();
    Map<String,IConnectionManager> connMap = new HashMap<>();
    Map<Integer,IConnectionManager> kryoMap = new HashMap<>();
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
    public void run()
    {
        if(isRelay)
        {
            Server server = new Server(IN_BUFFER,OUT_BUFFER);
            NetworkSetup.setup(server);
            server.addListener(new Listener(){
                @Override
                public void connected(Connection conn)
                {
                    IConnectionManager connMan = new ConnMan(ki,true,conn);
                    kryoMap.put(conn.getID(),connMan);
                    connMan.connected(conn);

                }

                @Override
                public void received(Connection conn, Object o)
                {
                    kryoMap.get(conn.getID()).received(o);
                }
            });
            server.start();

            try {
                server.bind(PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }



        }
        int successes = 0;
        for(String ip:bootstrap) {
            Client client = new Client(IN_BUFFER, OUT_BUFFER);
            NetworkSetup.setup(client);
            client.addListener(new Listener(){
                IConnectionManager connMan = new ConnMan(ki,false);
                @Override
                public void connected(Connection conn)
                {
                    connMan.connected(conn);
                }

                @Override
                public void received(Connection conn,Object o)
                {
                    connMan.received(o);
                }
            });
            client.start();
            successes++;
            try {
                client.connect(5000,ip,PORT);
            } catch (IOException e) {
                ki.getMainLog().info("Could not connect to: " + ip);
                successes--;
            }

            if(successes > 4)
            {
                break;
            }
        }
    }

    @Override
    public List<IConnectionManager> getConnections() {
        return null;
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
            if(connMan != null && ID != null) {
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
