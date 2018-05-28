package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;

public class DoneDownloading implements Packet {

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        pg.doneDownloading = true;
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {

    }

    @Override
    public byte[] serializeToBytes() {
        return new byte[0];
    }
}
