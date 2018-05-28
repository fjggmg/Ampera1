package com.ampex.main.network.packets;

import amp.HeadlessPrefixedAmplet;
import com.ampex.amperabase.IAddress;
import com.ampex.amperabase.IOutput;
import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;
import com.ampex.main.transactions.addresses.Address;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UTXOStartAck implements Packet {

    byte[] addresses;


    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (addresses == null) return;
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(addresses);
        while (hpa.hasNextElement()) {
            List<IOutput> utxos = new ArrayList<>();
            IAddress a = Address.fromByteArray(hpa.getNextElement());
            if (ki.getTransMan().getUTXOs(a, true) != null)
                utxos.addAll(ki.getTransMan().getUTXOs(a, true));
            UTXOData ud = new UTXOData();
            HeadlessPrefixedAmplet hpaU = HeadlessPrefixedAmplet.create();
            for (IOutput o : utxos) {
                hpaU.addBytes(o.serializeToBytes());
            }
            ud.utxos = hpaU.serializeToBytes();
            connMan.sendPacket(ud);
        }
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        addresses = Arrays.copyOf(serialized, serialized.length);
    }

    @Override
    public byte[] serializeToBytes() {
        return Arrays.copyOf(addresses, addresses.length);
    }
}
