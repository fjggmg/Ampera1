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
            List<String> sUtxos = new ArrayList<>();
            for (Output o : utxos) {
                sUtxos.add(o.toJSON());
            }
            ud.utxos = sUtxos;
            connMan.sendPacket(ud);
        }
    }

    @Override
    public int packetType() {
        return 0;
    }
}
