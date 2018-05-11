package com.lifeform.main.network.packets;

import amp.HeadlessPrefixedAmplet;
import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.transactions.Address;
import com.lifeform.main.transactions.IAddress;
import com.lifeform.main.transactions.Output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UTXOStartAck implements Packet, Serializable {
    private static final long serialVersionUID = 184L;
    List<String> addresses;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        for (String address : addresses) {
            List<Output> utxos = new ArrayList<>();
            IAddress a = Address.decodeFromChain(address);
            if (ki.getTransMan().getUTXOs(a, true) != null)
                utxos.addAll(ki.getTransMan().getUTXOs(a, true));
            UTXOData ud = new UTXOData();
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
            for (Output o : utxos) {
                hpa.addBytes(o.serializeToBytes());
            }
            ud.utxos = hpa.serializeToBytes();
            connMan.sendPacket(ud);
        }
    }
}
