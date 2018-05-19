package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.network.IConnectionManager;

import java.io.Serializable;

public class DisconnectRequest implements Serializable, Packet {
    private static final long serialVersionUID = 184L;
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (pg.relays != null) {
            //assuming that since our relay list is not empty we've gone ahead and started connections with other relays
            //TODO we need to either A: (stupid way) time this so that we wait to see if the relays we received are up or B: (smart way) reconnect to this relay if we cannot connect and/or wait until we're connected to other relays to disconnect
            if (!ki.getNetMan().isRelay())
                connMan.disconnect();
        }
    }

}
