package com.lifeform.main.transactions;

import com.lifeform.main.blockchain.Block;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by Bryan on 8/11/2017.
 */
public interface ITransMan {

    boolean verifyTransaction(ITrans transaction);
    boolean addTransaction(ITrans transaction);

    boolean addTransactionNoVerify(ITrans transaction);

    List<Output> getUTXOs(IAddress address, boolean safe);
    boolean verifyCoinbase(ITrans transaction,BigInteger blockHeight, BigInteger fees);
    boolean addCoinbase(ITrans transaction,BigInteger blockHeight,BigInteger fees);
    List<ITrans> getPending();
    List<String> getUsedUTXOs();

    void undoTransaction(ITrans trans);

    boolean utxosChanged(IAddress address);

    void close();

    void clear();

    ITrans createSimple(IAddress receiver, BigInteger amount, BigInteger fee, Token token, String message) throws InvalidTransactionException;

    ITrans createSimple(IAddress receiver, BigInteger amount, BigInteger fee, Token token, String message, int multipleOuts) throws InvalidTransactionException;
    boolean postBlockProcessing(Block block);

    List<Input> getInputsForAmountAndToken(IAddress address, BigInteger amount, Token token, boolean used);

    void unUseUTXOs(List<Input> inputs);
}
