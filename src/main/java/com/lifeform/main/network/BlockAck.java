package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;
import java.math.BigInteger;

public class BlockAck implements Serializable,Packet {

    public BigInteger height;
    public boolean verified;
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
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
            } else if (ki.getChainMan().currentHeight().compareTo(height) == 0) {
                connMan.sendPacket(new DoneDownloading());
            }
        }
    }


    //TODO: not putting a type here since we've not made one yet but we're not using these either, possibly deprecate this feature soon
    @Override
    public int packetType() {
        return 0;
    }
}
