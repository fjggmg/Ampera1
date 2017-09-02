package com.lifeform.main.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
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
    public static final int IN_BUFFER = 20000000;
    public static final int OUT_BUFFER = 20000000;
    List<IConnectionManager> connections = new ArrayList<>();
    Map<String,IConnectionManager> connMap = new HashMap<>();
    public NetMan(IKi ki,boolean isRelay)
    {
        this.ki = ki;
        this.isRelay = isRelay;




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
                    server.addListener(new ConnMan(ki,true,conn));
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
            client.addListener(new ConnMan(ki,false));
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
            connMan.sendPacket(o);
        }
    }

    @Override
    public void broadcastAllBut(String ID, Object o) {
        for(IConnectionManager connMan:connections)
        {
            if(!connMan.getID().equals(ID))
            {
                connMan.sendPacket(o);
            }
        }
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
