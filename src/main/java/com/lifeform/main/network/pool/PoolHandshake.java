package com.lifeform.main.network.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.transactions.Address;

import java.io.Serializable;

public class PoolHandshake implements Serializable, PoolPacket {
    public String address;
    public String ID;

    @Override
    public void process(IKi ki, IConnectionManager connMan) {
        if (Address.decodeFromChain(address).isValid()) {
            ki.debug("Received pool handshake: ");
            ki.debug("ID: " + ID);
            ki.debug("Address: " + address);
            connMan.setID(ID);
            connMan.sendPacket(ki.getPoolData().currentWork);
        }
    }
}
