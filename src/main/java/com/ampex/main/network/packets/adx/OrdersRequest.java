package com.ampex.main.network.packets.adx;

import com.ampex.amperabase.IConnectionManager;
import com.ampex.amperabase.IKiAPI;
import com.ampex.amperabase.InvalidAmpBuildException;
import com.ampex.amperanet.packets.Packet;
import com.ampex.amperanet.packets.PacketGlobal;
import com.ampex.main.IKi;

public class OrdersRequest implements Packet {
    @Override
    public void process(IKiAPI ki, IConnectionManager connMan, PacketGlobal pg) {
        for (String o : ((IKi) ki).getExMan().getOrderIDs()) {
            OrderPacket op = new OrderPacket();
            op.transaction = ((IKi) ki).getExMan().getOrder(o).getTxid();
            op.order = ((IKi) ki).getExMan().getOrder(o).serializeToBytes();
            op.matched = false;
            connMan.sendPacket(op);
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
