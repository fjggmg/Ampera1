package com.lifeform.main.network;

import java.util.List;

public interface INetworkManager {

    List<IConnectionManager> getConnections();
    void broadcast(Object o);
    void broadcastAllBut(String ID,Object o);
    IConnectionManager getConnection(String ID);
    void connectionInit(String ID, IConnectionManager connMan);
    void start();


}
