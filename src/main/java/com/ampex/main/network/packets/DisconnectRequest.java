package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;

public class DisconnectRequest implements Packet {

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (pg.relays != null) {
            //assuming that since our relay list is not empty we've gone ahead and started connections with other relays
            //TODO we need to either A: (stupid way) time this so that we wait to see if the relays we received are up or B: (smart way) reconnect to this relay if we cannot connect and/or wait until we're connected to other relays to disconnect
            if (!ki.getNetMan().isRelay())
                connMan.disconnect();
        }
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {

    }

    @Override
    public byte[] serializeToBytes() {
        return new byte[0];
    }
}
