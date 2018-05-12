package com.lifeform.main.network.packets;

import amp.Amplet;
import amp.HeadlessPrefixedAmplet;
import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.network.IConnectionManager;

import java.io.Serializable;
import java.math.BigInteger;

public class PackagedBlocks implements Packet, Serializable {
    private static final long serialVersionUID = 184L;

    private byte[] packagedBlocks;
    private BigInteger lastBlock;
    private long crc;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (!EncryptionManager.checkCRCValue(packagedBlocks, crc)) {

            return;
        }
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(packagedBlocks);
        while (hpa.hasNextElement()) {
            ki.getStateManager().addBlock(Block.fromAmplet(Amplet.create(hpa.getNextElement())), connMan.getID());
        }
        if (lastBlock.compareTo(pg.startHeight) < 0) {
            PackagedBlocksRequest pbr = new PackagedBlocksRequest();
            pbr.fromBlock = ki.getChainMan().currentHeight();
            connMan.sendPacket(pbr);
        } else {
            pg.doneDownloading = true;
        }
    }

    public static PackagedBlocks createPackage(IKi ki, BigInteger startHeight) {
        PackagedBlocks pb = new PackagedBlocks();

        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        int totalSize = 0;

        while (totalSize < 1_048_576) {
            startHeight = startHeight.add(BigInteger.ONE);
            byte[] block = ki.getChainMan().getByHeight(startHeight).serializeToAmplet().serializeToBytes();
            hpa.addBytes(block);
            totalSize += block.length;
            totalSize += 4;
            if (startHeight.compareTo(ki.getChainMan().currentHeight()) == 0) break;

        }
        pb.lastBlock = startHeight;
        pb.packagedBlocks = hpa.serializeToBytes();
        pb.crc = EncryptionManager.getCRCValue(pb.packagedBlocks);
        return pb;
    }
}
