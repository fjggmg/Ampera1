package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ResetRequest implements Serializable, Packet {
    BlockHeader proof;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.debug("Received a reset request");
        if (proof.height.compareTo(ki.getChainMan().currentHeight()) == 0 && pg.laFlag) {
            if (proof.ID.equals(ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID)) {
                //this should be sufficient check but really we need to do a full fucking check of the block, will implement ease of use method for this later
                if (proof.prevID.equals(ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).prevID)) {
                    ki.debug("Reset request is legitimate, reseting block chain and transactions. This may take some time");
                    List<Block> blocks = new ArrayList<>();
                    BigInteger height = BigInteger.ZERO;
                    for (; height.compareTo(ki.getChainMan().currentHeight()) <= 0; height = height.add(BigInteger.ONE)) {
                        blocks.add(ki.getChainMan().getByHeight(height));
                    }
                    ki.getChainMan().clearFile();
                    ki.getTransMan().clear();
                    for (Block b : blocks) {
                        if (!ki.getChainMan().addBlock(b)) {
                            ki.debug("The block chain is corrupted beyond repair, you will need to manually delete the chain and transaction folders AFTER closing the program. After restarting the program will redownload the chain and should work correctly");
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public int packetType() {
        return PacketType.RR.getIndex();
    }
}
