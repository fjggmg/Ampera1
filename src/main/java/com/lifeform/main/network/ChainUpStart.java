package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.ChainManager;

import java.io.Serializable;
import java.math.BigInteger;

public class ChainUpStart implements Serializable, Packet {
    BigInteger startHeight;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        pg.cuFlag = true;

        pg.temp = new ChainManager(ki, ki.getChainMan().getChainVer(), "temp/", "tempcs.temp", "temptrans.temp", "tempextra.temp", "tempcm.temp", ki.getChainMan().getByHeight(startHeight.subtract(BigInteger.ONE)), true);

    }

    @Override
    public int packetType() {
        return PacketType.CUS.getIndex();
    }
}
