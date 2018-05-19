package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.network.IConnectionManager;

import java.io.Serializable;

public class ResetRequest implements Serializable, Packet {
    private static final long serialVersionUID = 184L;
    BlockHeader proof;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.debug("Received a reset request");

    }

}
