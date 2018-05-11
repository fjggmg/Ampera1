package com.lifeform.main.network.packets;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.transactions.IAddress;
import com.lifeform.main.transactions.TransactionManagerLite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class UTXODataStart implements Packet,Serializable {
    private static final long serialVersionUID = 184L;
    static Set<String> connIDs = new HashSet<>();
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {

            if(connIDs.contains(connMan.getID())) return;
            connIDs.add(connMan.getID());

        ((TransactionManagerLite)ki.getTransMan()).resetLite();
        UTXOStartAck usa = new UTXOStartAck();
        usa.addresses = new ArrayList<>();
        for (IAddress a : ki.getAddMan().getAll())
        {
            usa.addresses.add(a.encodeForChain());
        }
        connMan.sendPacket(usa);
    }

}
