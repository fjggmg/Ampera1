package com.lifeform.main.transactions;

import amp.HeadlessAmplet;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Bryan on 8/8/2017.
 */
public interface TXIO {
    List<Integer> VALID_ID_SIZES = Collections.unmodifiableList(Arrays.asList(28, 32));

    IAddress getAddress();
    BigInteger getAmount();
    Token getToken();
    int getIndex();
    String toJSON();
    String getID();
    long getTimestamp();
    //HeadlessAmplet serializeToAmplet();
}
