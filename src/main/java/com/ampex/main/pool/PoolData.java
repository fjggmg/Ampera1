package com.ampex.main.pool;

import com.ampex.amperabase.IAddress;
import com.ampex.main.blockchain.Block;
import com.ampex.main.network.packets.pool.PoolBlockHeader;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PoolData {
    public IAddress payTo;
    //byte[] blockData;
    public String ID;
    public PoolBlockHeader currentWork;
    public Map<BigInteger, List<String>> tracking = new HashMap<>();
    public Map<String, Block> workMap = new HashMap<>();
    public Map<String, String> addMap = new HashMap<>();
    public Map<String, Long> hrMap = new HashMap<>();
    public String poolConn;
    //public List<Block> pplnsBlocks;
    public BigInteger lowestHeight = BigInteger.ZERO;
}
