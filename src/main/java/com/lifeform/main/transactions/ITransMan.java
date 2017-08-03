package com.lifeform.main.transactions;

import org.mapdb.DB;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Bryan on 5/30/2017.
 */
public interface ITransMan {

    boolean verifyTransaction(MKiTransaction s);

    Map<String,MKiTransaction> getPending();

    Map<String,MKiTransaction> getInputs(String key);

    void close();

    ConcurrentMap<String,String> getUTXOMap();

    DB getUTXODB();

    ConcurrentMap<String,String> getUTXOValueMap();

    DB getUTXOValueDB();

    boolean verifyAndCommitTransaction(MKiTransaction s);

    ConcurrentMap<String,Boolean> getUTXOSpentMap();

    boolean softVerifyTransaction(MKiTransaction s);

}
