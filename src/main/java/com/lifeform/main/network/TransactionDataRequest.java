package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.transactions.Address;
import com.lifeform.main.transactions.Output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TransactionDataRequest implements Serializable, Packet {


    public List<Address> addresses = new ArrayList<>();

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        for (Address address : addresses) {
            List<Output> utxos = ki.getTransMan().getUTXOs(address);
            if (utxos == null) continue;
            UTXOData ud = new UTXOData();
            ud.utxos = utxos;
            connMan.sendPacket(ud);
        }
    }

    @Override
    public int packetType() {
        return 0;
    }
}
