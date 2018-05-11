package com.lifeform.main.network.packets;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;

import java.io.Serializable;

public class DDFullChain implements Packet, Serializable {
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        PendingTransactionRequest ptr = new PendingTransactionRequest();
        connMan.sendPacket(ptr);
        if (ki.getOptions().poolRelay) {
            ki.getPoolManager().updateCurrentHeight(ki.getChainMan().currentHeight());
        }
    }

}
