package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;

public class DoneDownloading implements Serializable, Packet {
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        pg.doneDownloading = true;
    }

    @Override
    public int packetType() {
        return 0;
    }
}
