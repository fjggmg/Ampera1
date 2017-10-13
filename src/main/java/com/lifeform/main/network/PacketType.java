package com.lifeform.main.network;


public enum PacketType {

    BBE(0),
    BE(1),
    BH(2),
    BR(3),
    CUE(4),
    CUS(5),
    HS(6),
    LAC(7),
    LAE(8),
    LAS(9),
    PTR(10),
    RL(11),
    RR(12),
    TP(13);
    private final int index;
    static PacketType[] indexer = {BBE, BE, BH, BR, CUE, CUS, HS, LAC, LAE, LAS, PTR, RL, RR, TP};

    PacketType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public PacketType fromIndex(int index) {
        if (index > indexer.length - 1) return null;
        return indexer[index];
    }
}
