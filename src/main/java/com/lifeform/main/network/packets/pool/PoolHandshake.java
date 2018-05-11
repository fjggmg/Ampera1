package com.lifeform.main.network.packets.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.Settings;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.pool.PoolNetMan;
import com.lifeform.main.transactions.Address;

import java.io.Serializable;

public class PoolHandshake implements Serializable, PoolPacket {
    private static final long serialVersionUID = 184L;
    public String address;
    public String ID;
    public String version;

    @Override
    public void process(IKi ki, IConnectionManager connMan) {

        ki.debug("Received pool handshake: ");
        if (version == null) return;
        if (!version.equals(PoolNetMan.POOL_NET_VERSION)) {
            ki.debug("Wrong pool net version");
            connMan.disconnect();
            return;
        }
        if (ki.getOptions().pool) {
            ki.getNetMan().connectionInit(ID, connMan);
            if (ki.getSetting(Settings.AUTO_MINE)) {
                if (ki.getPoolData().currentWork != null) {
                    ki.getMinerMan().restartMiners();
                }
            }
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
