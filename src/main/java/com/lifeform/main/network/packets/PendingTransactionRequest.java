package com.lifeform.main.network.packets;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.transactions.ITrans;

import java.io.Serializable;

public class PendingTransactionRequest implements Serializable, Packet {
    private static final long serialVersionUID = 184L;
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        for (ITrans trans : ki.getTransMan().getPending()) {
            TransactionPacket tp = new TransactionPacket();
            tp.trans = trans.serializeToAmplet().serializeToBytes();
            connMan.sendPacket(tp);
        }
    }

}
