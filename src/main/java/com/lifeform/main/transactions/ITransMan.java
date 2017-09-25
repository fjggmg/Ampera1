package com.lifeform.main.transactions;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by Bryan on 8/11/2017.
 */
public interface ITransMan {

    boolean verifyTransaction(ITrans transaction);
    boolean addTransaction(ITrans transaction);

    boolean addTransactionNoVerify(ITrans transaction);
    List<Output> getUTXOs(Address address);
    boolean verifyCoinbase(ITrans transaction,BigInteger blockHeight, BigInteger fees);
    boolean addCoinbase(ITrans transaction,BigInteger blockHeight,BigInteger fees);
    List<ITrans> getPending();
    List<String> getUsedUTXOs();

    void commit();
    void close();

    void clear();


}
