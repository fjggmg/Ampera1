package com.lifeform.main.transactions;

import java.math.BigInteger;

/**
 * Created by Bryan on 8/8/2017.
 */
public interface TXIO {
    Address getAddress();
    BigInteger getAmount();
    Token getToken();
    int getIndex();
    String toJSON();
    String getID();
    long getTimestamp();
}
