package com.ampex.main.network.packets.pool;

import amp.HeadlessPrefixedAmplet;
import com.ampex.amperabase.IConnectionManager;
import com.ampex.amperabase.InvalidAmpBuildException;
import com.ampex.main.IKi;
import com.ampex.main.Settings;
import com.ampex.main.network.pool.PoolNetMan;
import com.ampex.main.transactions.addresses.Address;

import java.nio.charset.Charset;

public class PoolHandshake implements PoolPacket {

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
        if (!ki.getOptions().poolRelay) {

            ki.getPoolNet().connectionInit(ID, connMan);
            ki.getMinerMan().stopMiners();
            ki.getMinerMan().setup();
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

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(serialized);
            address = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
            ID = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
            version = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
        } catch(RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create PoolHandshake from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.addElement(address);
        hpa.addElement(ID);
        hpa.addElement(version);
        return hpa.serializeToBytes();
    }
}
