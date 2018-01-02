package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.transactions.Address;
import com.lifeform.main.transactions.Output;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TransactionDataRequest implements Serializable, Packet {




    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        connMan.sendPacket(new UTXODataStart());

    }

    @Override
    public int packetType() {
        return 0;
    }
}
