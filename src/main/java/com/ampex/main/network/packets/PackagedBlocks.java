package com.ampex.main.network.packets;

import amp.Amplet;
import amp.ByteTools;
import amp.HeadlessPrefixedAmplet;
import com.ampex.main.IKi;
import com.ampex.main.blockchain.Block;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;

import java.math.BigInteger;

public class PackagedBlocks implements Packet {


    private byte[] packagedBlocks;
    private BigInteger lastBlock;
    private long crc;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (lastBlock == null || packagedBlocks == null) return;
        if (!EncryptionManager.checkCRCValue(packagedBlocks, crc)) {

            return;
        }
        ki.debug("====================================Received PackagedBlocks. Size of byte array: " + packagedBlocks.length);
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(packagedBlocks);
        while (hpa.hasNextElement()) {
            Block b = Block.fromAmplet(Amplet.create(hpa.getNextElement()));
            if (b == null) break;
            ki.downloadedTo(b.height);
            ki.getStateManager().addBlock(b, connMan.getID());
        }

        if (lastBlock.compareTo(pg.startHeight) < 0) {
            PackagedBlocksRequest pbr = new PackagedBlocksRequest();
            pbr.fromBlock = ki.getChainMan().currentHeight();
            connMan.sendPacket(pbr);
        } else {
            pg.doneDownloading = true;
        }
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(serialized);
            packagedBlocks = hpa.getNextElement();
            lastBlock = new BigInteger(hpa.getNextElement());
            byte[] crcArray = hpa.getNextElement();
            crc = ByteTools.buildLong(crcArray[0], crcArray[1], crcArray[2], crcArray[3], crcArray[4], crcArray[5], crcArray[6], crcArray[7]);
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create PackagedBlocks from bytes");
        }

    }

    public static PackagedBlocks createPackage(IKi ki, BigInteger startHeight) {
        PackagedBlocks pb = new PackagedBlocks();

        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        int totalSize = 0;

        while (totalSize < 1_048_576_00) {
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

    @Override
    public byte[] serializeToBytes() {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.ensureCapacity(1_048_676_00);
        hpa.addBytes(packagedBlocks);
        hpa.addElement(lastBlock);
        hpa.addElement(crc);
        return hpa.serializeToBytes();
    }
}
