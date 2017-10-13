package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.transactions.ITrans;

import java.io.Serializable;

public class PendingTransactionRequest implements Serializable, Packet {
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        for (ITrans trans : ki.getTransMan().getPending()) {
            TransactionPacket tp = new TransactionPacket();
            tp.trans = trans.toJSON();
            connMan.sendPacket(tp);
        }
    }

    @Override
    public int packetType() {
        return PacketType.PTR.getIndex();
    }
}
