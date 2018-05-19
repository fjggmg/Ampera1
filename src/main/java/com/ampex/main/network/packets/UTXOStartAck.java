package com.ampex.main.network.packets;

import amp.HeadlessPrefixedAmplet;
import com.ampex.amperabase.IAddress;
import com.ampex.amperabase.IOutput;
import com.ampex.main.IKi;
import com.ampex.main.network.IConnectionManager;
import com.ampex.main.transactions.addresses.Address;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UTXOStartAck implements Packet, Serializable {
    private static final long serialVersionUID = 184L;
    List<String> addresses;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (addresses == null) return;
        for (String address : addresses) {
            List<IOutput> utxos = new ArrayList<>();
            IAddress a = Address.decodeFromChain(address);
            if (ki.getTransMan().getUTXOs(a, true) != null)
                utxos.addAll(ki.getTransMan().getUTXOs(a, true));
            UTXOData ud = new UTXOData();
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
            for (IOutput o : utxos) {
                hpa.addBytes(o.serializeToBytes());
            }
            ud.utxos = hpa.serializeToBytes();
            connMan.sendPacket(ud);
        }
    }
}
