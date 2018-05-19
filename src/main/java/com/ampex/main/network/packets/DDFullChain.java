package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.network.IConnectionManager;

import java.io.Serializable;

public class DDFullChain implements Packet, Serializable {
    private static final long serialVersionUID = 184L;
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        PendingTransactionRequest ptr = new PendingTransactionRequest();
        connMan.sendPacket(ptr);
        if (ki.getOptions().poolRelay) {
            ki.getPoolManager().updateCurrentHeight(ki.getChainMan().currentHeight());
        }
    }

}
