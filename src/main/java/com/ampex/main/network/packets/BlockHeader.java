package com.ampex.main.network.packets;

import amp.HeadlessAmplet;
import amp.HeadlessPrefixedAmplet;
import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.concurrent.CopyOnWriteArrayList;

public class BlockHeader implements Packet {

    public String solver;
    public String merkleRoot;
    public String ID;
    public BigInteger height;
    public long timestamp;
    public String prevID;
    public byte[] payload;
    public byte[] coinbase;
    public boolean laFlag = false;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if(ki.getOptions().pDebug)
        ki.debug("Received block header");
        if(ki.getOptions().pDebug)
        ki.debug("Height: " + height);

        pg.headerMap.put(ID, this);

        pg.bMap.put(this, new CopyOnWriteArrayList<>());


    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(serialized);
            HeadlessAmplet ha = hpa.getNextElementAsHeadlessAmplet();
            timestamp = ha.getNextLong();
            laFlag = ha.getNextBoolean();
            solver = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
            merkleRoot = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
            ID = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
            height = new BigInteger(hpa.getNextElement());
            prevID = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
            payload = hpa.getNextElement();
            coinbase = hpa.getNextElement();
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create BlockHeader from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessAmplet ha = HeadlessAmplet.create();
        ha.addElement(timestamp);
        ha.addElement(laFlag);
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.addElement(ha);
        hpa.addElement(solver);
        hpa.addElement(merkleRoot);
        hpa.addElement(ID);
        hpa.addElement(height);
        hpa.addElement(prevID);
        hpa.addBytes(payload);
        hpa.addBytes(coinbase);
        return hpa.serializeToBytes();
    }
}
