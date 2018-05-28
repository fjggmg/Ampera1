package com.ampex.main.network.packets;

import amp.HeadlessAmplet;
import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;

public class Pong implements Packet {

    long latency;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {

        connMan.setCurrentLatency(System.currentTimeMillis() - latency);
        ki.getNetMan().setLive(true);

    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            HeadlessAmplet ha = HeadlessAmplet.create(serialized);
            latency = ha.getNextLong();
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create Pong from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessAmplet ha = HeadlessAmplet.create();
        ha.addElement(latency);
        return ha.serializeToBytes();
    }
}
