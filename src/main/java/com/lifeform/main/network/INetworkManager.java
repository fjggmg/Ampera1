package com.lifeform.main.network;



import com.lifeform.main.network.logic.Client;

import java.util.List;
import java.util.Set;

public interface INetworkManager {

    Set<IConnectionManager> getConnections();
    void broadcast(Object o);
    void broadcastAllBut(String ID,Object o);
    IConnectionManager getConnection(String ID);
    void connectionInit(String ID, IConnectionManager connMan);
    void start();
    boolean isRelay();
    Client getClient(String ID);

    void attemptConnect(String IP);

    void interrupt();


}
