package com.ampex.main.network.packets;

import amp.ByteTools;
import amp.HeadlessPrefixedAmplet;
import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;

import java.math.BigInteger;

public class BlockRequest implements Packet {

    public BigInteger fromHeight;
    public boolean lite = false;
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if(ki.getOptions().pDebug)
        ki.debug("Received block request");
        if (fromHeight == null) return;
        if (!lite) {
            if (fromHeight.compareTo(ki.getChainMan().currentHeight()) < 0)
                pg.sendBlock(fromHeight.add(BigInteger.ONE));
        } else {
            pg.sendBlock(ki.getChainMan().currentHeight());
        }

    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(serialized);
            fromHeight = new BigInteger(hpa.getNextElement());
            lite = ByteTools.buildBoolean(hpa.getNextElement()[0]);
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to build BlockRequest from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.addElement(fromHeight);
        hpa.addElement(lite);
        return hpa.serializeToBytes();

    }
}
