package com.ampex.main.blockchain;

import com.ampex.amperabase.IBlockAPI;
import com.ampex.amperabase.IStateManager;
import com.ampex.main.IKi;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class StateManagerLite extends Thread implements IStateManager {
    private IKi ki;

    public StateManagerLite(IKi ki) {
        this.ki = ki;
    }

    private BigInteger addHeight = BigInteger.valueOf(-1L);
    Map<String, Map<BigInteger, IBlockAPI>> blockMap = new HashMap<>();
    @Override
    public void addBlock(IBlockAPI block, String connID) {
        blockMap.computeIfAbsent(connID, k -> new HashMap<>());

        blockMap.get(connID).put(block.getHeight(), block);
        if (block.getHeight().compareTo(addHeight) > 0) {
            addHeight = block.getHeight();
        }
    }


    @Override
    public void run() {
        while (true) {
            if (addHeight.compareTo(ki.getChainMan().currentHeight()) > 0) {
                for (Map.Entry<String, Map<BigInteger, IBlockAPI>> connID : blockMap.entrySet()) {
                    if (connID.getValue().get(addHeight) != null) {
                        if (addHeight.compareTo(ki.getChainMan().currentHeight()) > 0) {
                            ki.getChainMan().addBlock(connID.getValue().get(addHeight));
                        }
                    }
                }
            }
            try {
                sleep(10);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

}
