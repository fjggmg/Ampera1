package com.lifeform.main.network;

import java.io.Serializable;
import java.math.BigInteger;

public class BlockHeader implements Serializable {
    public String solver;
    public String merkleRoot;
    public String ID;
    public BigInteger height;
    public long timestamp;
    public String prevID;
    public byte[] payload;
    public String coinbase;
    public boolean laFlag = false;

}
