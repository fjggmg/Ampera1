package com.lifeform.main.network;

import com.lifeform.main.transactions.ITrans;

import java.math.BigInteger;

public class BlockHeader {
    public String solver;
    public String merkleRoot;
    public String ID;
    public BigInteger height;
    public long timestamp;
    public String prevID;
    public String payload;
    public ITrans coinbase;

}
