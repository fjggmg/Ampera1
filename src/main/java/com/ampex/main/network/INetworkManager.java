package com.ampex.main.network;


import com.ampex.main.data.utils.AmpBuildable;

import java.util.List;

public interface INetworkManager {

    List<IConnectionManager> getConnections();

    void broadcast(AmpBuildable o);

    void broadcastAllBut(String ID, AmpBuildable o);
    IConnectionManager getConnection(String ID);

    boolean connectionInit(String ID, IConnectionManager connMan);
    void start();
    boolean isRelay();

    void attemptConnect(String IP);

    void interrupt();

    boolean live();

    void setLive(boolean live);

    void addRelays(List<String> relays);

    List<String> getRelays();

    void close();

    GlobalPacketQueuer getGPQ();

    /**
     * run when we receive difficulty from the relay on a lite node. Added to protect from using a static variable
     */
    void diffSet();

    /**
     * @return true if difficulty has been set on lite node
     * @see INetworkManager#diffSet()
     */
    boolean isDiffSet();

    /**
     * wrapper for thread
     *
     * @return true if interrupted
     */
    boolean isInterrupted();


}
