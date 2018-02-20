package com.lifeform.main.network.pool;

import com.lifeform.main.blockchain.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PoolData {
    public String payTo;
    public byte[] blockData;
    public String ID;
    public PoolBlockHeader currentWork;
    public Map<String, Block> workMap = new HashMap<>();
    public Map<String, String> addMap = new HashMap<>();
    public Map<String, Long> hrMap = new HashMap<>();
    public String poolConn;
    public List<Block> pplnsBlocks;
}
