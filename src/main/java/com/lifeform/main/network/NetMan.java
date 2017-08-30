package com.lifeform.main.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.lifeform.main.IKi;
import com.lifeform.main.Ki;
import org.bitbucket.backspace119.generallib.io.network.*;
import org.bitbucket.backspace119.generallib.io.network.Packet;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static com.esotericsoftware.minlog.Log.LEVEL_TRACE;

/**
 * Created by Bryan on 5/8/2017.
 */
public class NetMan extends Thread implements QueuedNetworkManager {


    private final String[] bootstrap = {"73.108.51.16","66.87.153.130"};
    private Map<String,ConnectionManager> connectionMap = new HashMap<>();
    private Map<Integer,String> connToID = new HashMap<>();
    public static int PORT = 29555;
    private final boolean relay;
    private Server server;
    private Client client;
    private IKi ki;
    public NetMan(IKi ki, int tickTime, boolean relay)
    {
        this.ki = ki;
        this.tickTime = tickTime;
        this.relay = relay;
        //Log.set(LEVEL_TRACE);
        if(relay)
        {
            server = new Server(20000000,20000000);
            NetworkSetup.setup(server);

            server.start();
            try {
                server.bind(PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            server.addListener(new Listener(){
                @Override
                public void connected(Connection connection)
                {
                    Handshake hs = new Handshake();
                    hs.ID = ki.getEncryptMan().getPublicKeyString();
                    hs.version = Ki.VERSION;
                    hs.currentHeight = ki.getChainMan().currentHeight();
                    hs.isRelay = true;
                    if(ki.getChainMan().currentHeight().compareTo(BigInteger.valueOf(-1L)) == 0)
                    {
                        hs.mostRecentBlock = "";
                    }else {
                        hs.mostRecentBlock = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID;
                    }
                    connection.sendTCP(hs);
                }

                @Override
                public void received(Connection connection,Object o)
                {
                    if(o instanceof Handshake)
                    {
                        ConnectionManager cm = new RelayConnMan(ki,connection,((Handshake)o).ID);
                        connectionMap.put(cm.getID(),cm);
                        server.addListener((RelayConnMan)cm);

                        connection.sendTCP(new GoAhead());
                    }
                }
            });

            for(String ip:bootstrap)
            {
                Client client = new Client(20000000,20000000);

                NetworkSetup.setup(client);
                client.addListener(new Listener() {

                    @Override
                    public void received(Connection connection, Object o) {
                        if (o instanceof Handshake) {
                            Handshake rhs = ((Handshake) o);
                            if(rhs.ID.equals(ki.getEncryptMan().getPublicKeyString())) {
                                connection.close();
                                return;
                            }
                            ConnectionManager cm = new ConnMan(ki, client, rhs.ID);
                            client.addListener((ConnMan) cm);
                            Handshake hs = new Handshake();
                            hs.ID = ki.getEncryptMan().getPublicKeyString();
                            hs.version = Ki.VERSION;
                            if (ki.getChainMan().currentHeight().compareTo(BigInteger.valueOf(-1L)) == 0) {
                                hs.mostRecentBlock = "";
                            } else {
                                hs.mostRecentBlock = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID;
                            }
                            hs.currentHeight = ki.getChainMan().currentHeight();
                            connectionMap.put(cm.getID(), cm);
                            connToID.put(connection.getID(), cm.getID());
                            ki.setRelayer(cm.getID());
                            connection.sendTCP(hs);
                        } else if (o instanceof GoAhead) {
                            ki.getMainLog().info("Received go ahead from relay");
                            if (!init) {
                                BlockRequestPacket brp = new BlockRequestPacket(ki);
                                Map<String, String> dat = new HashMap<>();
                                dat.put("height", "" + ki.getChainMan().currentHeight());
                                brp.setData(dat);
                                connectionMap.get(connToID.get(connection.getID())).sendPacket(brp);
                                client.removeListener(this);
                                init = true;
                            }
                        }
                    }
                });
                client.start();

                try {
                    client.connect(5000,ip,PORT);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }



        }else {
            client = new Client(20000000, 20000000);
            NetworkSetup.setup(client);
            client.addListener(new Listener() {

                @Override
                public void received(Connection connection, Object o) {
                    if (o instanceof Handshake) {
                        ConnectionManager cm = new ConnMan(ki, client, ((Handshake) o).ID);
                        client.addListener((ConnMan) cm);
                        Handshake hs = new Handshake();
                        hs.ID = ki.getEncryptMan().getPublicKeyString();
                        hs.version = Ki.VERSION;
                        if (ki.getChainMan().currentHeight().compareTo(BigInteger.valueOf(-1L)) == 0) {
                            hs.mostRecentBlock = "";
                        } else {
                            hs.mostRecentBlock = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID;
                        }
                        hs.currentHeight = ki.getChainMan().currentHeight();
                        connectionMap.put(cm.getID(), cm);
                        connToID.put(connection.getID(), cm.getID());
                        ki.setRelayer(cm.getID());
                        connection.sendTCP(hs);
                    } else if (o instanceof GoAhead) {
                        ki.getMainLog().info("Received go ahead from relay");
                        if (!init) {
                            BlockRequestPacket brp = new BlockRequestPacket(ki);
                            Map<String, String> dat = new HashMap<>();
                            dat.put("height", "" + ki.getChainMan().currentHeight());
                            brp.setData(dat);
                            connectionMap.get(connToID.get(connection.getID())).sendPacket(brp);
                            client.removeListener(this);
                            init = true;
                        }
                    }
                }
            });

            client.start();

                    try {
                        client.connect(5000, bootstrap[0], PORT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

        }

    }

    private boolean init = false;
    private int tickTime;
    private FIFOQueue netQ = new FIFOQueue();
    private boolean run = true;
    private Socket connection;
    @Override
    public void stopNet()
    {
        run = false;
        for(String s: connectionMap.keySet())
        {
            //stop connections

        }
    }

    @Override
    public ConnectionManager getConnection(String s) {
        return connectionMap.get(s);
    }

    @Override
    public boolean broadcastAllBut(Packet packet, String s) {
        for(String ID:connectionMap.keySet())
        {
            if(!ID.equals(s))
            {
                connectionMap.get(ID).sendPacket(packet);
            }
        }

        return true;
    }

    @Override
    public boolean broadcastPacket(Object o) {
        if(relay)
        {
            for(String ID:connectionMap.keySet())
            {
                connectionMap.get(ID).sendPacket(o);
            }
        }else{
            client.sendTCP(o);
        }
        return false;
    }

    @Override
    public boolean broadcastAllBut(Object o, String s) {
        for(String ID:connectionMap.keySet())
        {
            if(!ID.equals(s))
            {
                connectionMap.get(ID).sendPacket(o);
            }
        }
        return true;
    }

    @Override
    public NetworkQueue getQueue() {
        return netQ;
    }

    private long stamp;
    @Override
    public void tick() {

    }


    @Override
    public void run()
    {
        //nothing to do with new library here
    }


    @Override
    public ServerSocket startServer(int i) {
        return null;
    }

    @Override
    public Socket connect(String s, int i) throws IOException{

       return null;
    }

    //always returns true?
    //only for server?
    @Override
    public boolean broadcastPacket(Packet packet) {

        if(relay)
        {
            for(String ID:connectionMap.keySet())
            {
                connectionMap.get(ID).sendPacket(packet);
            }
        }else{
            client.sendTCP(packet.toJSON());
        }
        return true;

    }

    @Override
    public boolean connectionStatus(String ID) {
        return connectionMap.get(ID).isConnected();
    }
}

/*
 ServerSocket ss;
        if(relay) {
            ss = startServer(PORT);


            new Thread() {
                public void run() {
                    while (run) {
                        try {

                            Socket s = ss.accept();

                            ConnectionManager cm = new ConnMan(ki,s,EncryptionManager.sha512(s.getInetAddress().getHostAddress()));
                            cm.listen();
                            connectionMap.put(EncryptionManager.sha512(s.getInetAddress().getHostAddress()),cm);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }.start();


        }else{

           for(String address:bootstrap)
           {
               try {
                   Socket s = connect(address, PORT);

                   if (s != null) {
                       ConnectionManager cm = new ConnMan(ki, s,EncryptionManager.sha512(s.getInetAddress().getHostAddress()));
                       cm.listen();
                       connectionMap.put(EncryptionManager.sha512(address), cm);
                       ki.getMainLog().info("Connected to: " + address);
                       BlockRequestPacket brp = new BlockRequestPacket(ki);
                       Map<String,String> dat = new HashMap<>();
                       dat.put("height","" + ki.getChainMan().currentHeight());
                       brp.setData(dat);
                       cm.sendPacket(brp);
                   }
               }catch(Exception e)
               {
                   ki.getMainLog().warn("Unable to connect to: " + address);
               }
           }
        }


        while(run)
        {


            stamp = System.currentTimeMillis();
            /*
            if(!netQ.getAll().isEmpty())
            {
                //this essentially defeats the purpose of having access to the previous packet
                //TODO should possibly go over logic here to make it more accessible in the future.
                netQ.getNext().process(this);

            }


            try {
                    long sleepTime = tickTime - (System.currentTimeMillis() - stamp);
                    if(sleepTime < 0) sleepTime = 1;
        sleep(sleepTime);
        } catch (InterruptedException e) {
        e.printStackTrace();
        }
        }
 */
