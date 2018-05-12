package com.lifeform.main.network.packets;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;

import java.io.Serializable;

public class ResetRequest implements Serializable, Packet {
    private static final long serialVersionUID = 184L;
    BlockHeader proof;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.debug("Received a reset request");

    }

}
