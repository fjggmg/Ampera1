package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;

public class DDFullChain implements Packet {

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        PendingTransactionRequest ptr = new PendingTransactionRequest();
        connMan.sendPacket(ptr);
        if (ki.getOptions().poolRelay) {
            ki.getPoolManager().updateCurrentHeight(ki.getChainMan().currentHeight());
        }
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {

    }

    @Override
    public byte[] serializeToBytes() {
        return new byte[0];
    }
}
