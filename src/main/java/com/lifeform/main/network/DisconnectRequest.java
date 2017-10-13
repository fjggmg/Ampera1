package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;

public class DisconnectRequest implements Serializable, Packet {
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (pg.relays != null) {
            //assuming that since our relay list is not empty we've gone ahead and started connections with other relays
            //TODO we need to either A: (stupid way) time this so that we wait to see if the relays we received are up or B: (smart way) reconnect to this relay if we cannot connect and/or wait until we're connected to other relays to disconnect
            connMan.disconnect();
        }
    }

    //TODO not filled out because we're not using it, maybe we will in the future
    @Override
    public int packetType() {
        return 0;
    }
}
