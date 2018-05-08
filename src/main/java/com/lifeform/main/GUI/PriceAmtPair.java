package com.lifeform.main.GUI;

import java.math.BigInteger;

public class PriceAmtPair {
    public PriceAmtPair(BigInteger price, BigInteger amt) {
        this.price = price;
        this.amount = amt;
    }

    public BigInteger price;
    public BigInteger amount;
}
