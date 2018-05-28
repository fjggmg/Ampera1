package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;
import com.ampex.main.transactions.ITrans;

public class PendingTransactionRequest implements Packet {

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        for (ITrans trans : ki.getTransMan().getPending()) {
            TransactionPacket tp = new TransactionPacket();
            tp.trans = trans.serializeToAmplet().serializeToBytes();
            connMan.sendPacket(tp);
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
