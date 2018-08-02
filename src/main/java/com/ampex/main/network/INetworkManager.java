package com.ampex.main.network;


import com.ampex.amperabase.AmpBuildable;
import com.ampex.amperabase.IConnectionManager;
import com.ampex.amperabase.INetworkManagerAPI;

import java.util.List;

public interface INetworkManager extends INetworkManagerAPI {

    List<IConnectionManager> getConnections();

    void broadcast(AmpBuildable o);

    IConnectionManager getConnection(String ID);

    boolean connectionInit(String ID, IConnectionManager connMan);

    void start();

    void attemptConnect(String IP);

    void interrupt();

    boolean live();

    List<String> getRelays();

    void close();


}
