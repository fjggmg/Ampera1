package com.lifeform.main.network.packets;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;

import java.io.Serializable;

public class TransactionDataRequest implements Serializable, Packet {




    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        connMan.sendPacket(new UTXODataStart());

    }

}
