package com.ampex.main.network.packets;

import amp.ByteTools;
import amp.HeadlessPrefixedAmplet;
import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;
import com.ampex.main.transactions.ITrans;

import java.math.BigInteger;

public class BlockAck implements Packet {

    public BigInteger height;
    public boolean verified;
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (height == null) return;
        pg.cancelResend(height);
        if(verified)
        {
            if(ki.getChainMan().currentHeight().compareTo(height) > 0)
            {
                pg.sendBlock(height.add(BigInteger.ONE));
            }else if(ki.getChainMan().currentHeight().compareTo(height) == 0 && !pg.doneDownloading) {
                pg.doneDownloading = true;
                connMan.doneDownloading();
                connMan.sendPacket(new DoneDownloading());
                connMan.sendPacket(new DDFullChain());
                if(ki.getNetMan().isRelay())
                for(ITrans t:ki.getTransMan().getPending())
                {
                    TransactionPacket tp = new TransactionPacket();
                    tp.trans = t.serializeToAmplet().serializeToBytes();
                    connMan.sendPacket(tp);
                }
            } else if (ki.getChainMan().currentHeight().compareTo(height) == 0) {
                connMan.sendPacket(new DoneDownloading());
            }
        }
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(serialized);
            height = new BigInteger(hpa.getNextElement());
            verified = ByteTools.buildBoolean(hpa.getNextElement()[0]);
        } catch (Exception e) {
            throw new InvalidAmpBuildException("unable to build BlockAck packet from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.addElement(height);
        hpa.addElement(verified);
        return hpa.serializeToBytes();
    }
}
