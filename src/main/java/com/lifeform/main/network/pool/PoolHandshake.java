package com.lifeform.main.network.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.transactions.Address;

import java.io.Serializable;

public class PoolHandshake implements Serializable, PoolPacket {
    public String address;
    public String ID;
    public String version;

    @Override
    public void process(IKi ki, IConnectionManager connMan) {

        ki.debug("Received pool handshake: ");
        if (!version.equals(PoolNetMan.POOL_NET_VERSION)) {
            ki.debug("Wrong pool net version");
            connMan.disconnect();
            return;
        }
        if (ki.getOptions().pool) {
            ki.getNetMan().connectionInit(ID, connMan);
            return;
        }
        if (Address.decodeFromChain(address).isValid()) {

            ki.debug("ID: " + ID);
            ki.debug("Address: " + address);
            connMan.setID(ID);
            connMan.sendPacket(ki.getPoolData().currentWork);
            ki.getPoolNet().connectionInit(ID, connMan);
            ki.getPoolData().addMap.put(ID, address);

        }
    }
}
