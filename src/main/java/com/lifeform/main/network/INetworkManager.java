package com.lifeform.main.network;



import com.lifeform.main.network.logic.Client;

import java.util.List;
import java.util.Set;

public interface INetworkManager {

    Set<IConnectionManager> getConnections();
    void broadcast(Object o);
    void broadcastAllBut(String ID,Object o);
    IConnectionManager getConnection(String ID);

    boolean connectionInit(String ID, IConnectionManager connMan);
    void start();
    boolean isRelay();
    Client getClient(String ID);

    void attemptConnect(String IP);

    void interrupt();

    boolean live();

    void setLive(boolean live);

    void addRelays(List<String> relays);

    List<String> getRelays();

    void close();

    GlobalPacketQueuer getGPQ();


}
