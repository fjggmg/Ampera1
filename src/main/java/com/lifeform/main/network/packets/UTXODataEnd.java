package com.lifeform.main.network.packets;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;

import java.io.Serializable;

public class UTXODataEnd implements Packet,Serializable {
    private static final long serialVersionUID = 184L;
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {

            UTXODataStart.connIDs.remove(connMan.getID());

    }

}
