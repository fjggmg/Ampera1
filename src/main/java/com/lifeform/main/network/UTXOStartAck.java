package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.transactions.Address;
import com.lifeform.main.transactions.Output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class UTXOStartAck implements Packet,Serializable {
    List<String> addresses;
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        for (String address : addresses) {
            //ki.debug("RECEIVED REQUEST FOR UTXOS FOR " + address);

            List<Output> utxos = new ArrayList<>();
            Address a = Address.decodeFromChain(address);
            if (ki.getTransMan().getUTXOs(a) != null)
                utxos.addAll(ki.getTransMan().getUTXOs(a));
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