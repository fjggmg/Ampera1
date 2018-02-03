package com.lifeform.main.network;

import com.lifeform.main.IKi;

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

    @Override
    public int packetType() {
        return 0;
    }
}
