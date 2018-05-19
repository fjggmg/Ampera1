package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.network.IConnectionManager;
import com.ampex.main.transactions.ITrans;

import java.io.Serializable;
import java.math.BigInteger;

public class BlockAck implements Serializable,Packet {
    private static final long serialVersionUID = 184L;
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

}
