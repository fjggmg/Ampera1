package com.ampex.main.network.packets;

import amp.HeadlessAmplet;
import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;

public class Ping implements Packet {

    public long currentTime;
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        Pong pong = new Pong();
        pong.latency = currentTime;
        connMan.sendPacket(pong);
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            HeadlessAmplet ha = HeadlessAmplet.create(serialized);
            currentTime = ha.getNextLong();
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create Ping from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessAmplet ha = HeadlessAmplet.create();
        ha.addElement(currentTime);
        return ha.serializeToBytes();
    }
}
