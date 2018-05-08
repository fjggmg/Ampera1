package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import com.lifeform.main.network.TransactionDataRequest;
import com.lifeform.main.transactions.TransactionManager;
import com.lifeform.main.transactions.TransactionManagerLite;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class StateManagerLite extends Thread implements IStateManager {
    private IKi ki;

    public StateManagerLite(IKi ki) {
        this.ki = ki;
    }

    private BigInteger addHeight = BigInteger.valueOf(-1L);
    Map<String, Map<BigInteger, Block>> blockMap = new HashMap<>();
    @Override
    public void addBlock(Block block, String connID) {
        blockMap.computeIfAbsent(connID, k -> new HashMap<>());

        blockMap.get(connID).put(block.height, block);
        if (block.height.compareTo(addHeight) > 0) {
            addHeight = block.height;
        }
    }


    @Override
    public void run() {
        while (true) {
            if (addHeight.compareTo(ki.getChainMan().currentHeight()) > 0) {
                for (Map.Entry<String, Map<BigInteger, Block>> connID : blockMap.entrySet()) {
                    if (connID.getValue().get(addHeight) != null) {
                        if (addHeight.compareTo(ki.getChainMan().currentHeight()) > 0) {
                            if (ki.getChainMan().addBlock(connID.getValue().get(addHeight)).success()) {

                            }
                        }
                    }
                }
            }
            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
