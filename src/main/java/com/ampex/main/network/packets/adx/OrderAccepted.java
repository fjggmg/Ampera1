package com.ampex.main.network.packets.adx;

import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;
import com.ampex.main.network.packets.Packet;
import com.ampex.main.network.packets.PacketGlobal;

import java.nio.charset.Charset;

public class OrderAccepted implements Packet {
    public String ID;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.getExMan().orderAccepted(ID);
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            ID = new String(serialized, Charset.forName("UTF-8"));
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create OrderAccepted from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        if (ID != null)
            return ID.getBytes(Charset.forName("UTF-8"));
        else
            return new byte[0];
    }
}
