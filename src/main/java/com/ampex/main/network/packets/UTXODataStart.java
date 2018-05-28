package com.ampex.main.network.packets;

import amp.HeadlessPrefixedAmplet;
import com.ampex.amperabase.IAddress;
import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;
import com.ampex.main.transactions.TransactionManagerLite;

import java.util.HashSet;
import java.util.Set;

public class UTXODataStart implements Packet {

    static Set<String> connIDs = new HashSet<>();
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {

            if(connIDs.contains(connMan.getID())) return;
            connIDs.add(connMan.getID());

        ((TransactionManagerLite)ki.getTransMan()).resetLite();
        UTXOStartAck usa = new UTXOStartAck();
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        for (IAddress a : ki.getAddMan().getAll())
        {
            hpa.addBytes(a.toByteArray());
        }
        usa.addresses = hpa.serializeToBytes();
        connMan.sendPacket(usa);
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {

    }

    @Override
    public byte[] serializeToBytes() {
        return new byte[0];
    }
}
