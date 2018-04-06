package com.lifeform.main.transactions;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionManagerLite implements ITransMan {

    private IKi ki;

    public TransactionManagerLite(IKi ki) {

        this.ki = ki;

    }

    Map<String, TXIO> utxoMap = new HashMap<>();

    public void resetLite() {
        utxoMap.clear();
    }
    @Override
    public boolean verifyTransaction(ITrans transaction) {


        return true;

    }

    @Override
    public boolean addTransaction(ITrans transaction) {

        pending.remove(transaction);
        for (Output output : transaction.getOutputs()) {
            for (IAddress a : ki.getAddMan().getAll()) {
                if (output.getAddress().encodeForChain().equals(a.encodeForChain())) {
                    utxoMap.put(output.getID(), output);
                }
            }
        }
        for (Input input : transaction.getInputs()) {
            for (IAddress a : ki.getAddMan().getAll()) {
                if (input.getAddress().encodeForChain().equals(a.encodeForChain())) {
                    utxoMap.remove(input.getID());
                }
            }
        }

        return true;
    }

    @Override
    public boolean addTransactionNoVerify(ITrans transaction) {
        for (Output output : transaction.getOutputs()) {
            for (IAddress a : ki.getAddMan().getAll()) {
                if (output.getAddress().encodeForChain().equals(a.encodeForChain())) {
                    utxoMap.put(output.getID(), output);
                }
            }
        }
        for (Input input : transaction.getInputs()) {
            for (IAddress a : ki.getAddMan().getAll()) {
                if (input.getAddress().encodeForChain().equals(a.encodeForChain())) {
                    utxoMap.remove(input.getID());
                }
            }
        }
        return true;
    }

    public void addUTXOs(List<Output> outputs) {
        for (Output o : outputs) {
            utxoMap.put(o.getID(), o);
        }
    }

    @Override
    public List<Output> getUTXOs(IAddress address, boolean safe) {
        List<Output> utxos = new ArrayList<>();
        for (String ID : utxoMap.keySet()) {
            if (!usedUTXO.contains(ID))
                utxos.add((Output) utxoMap.get(ID));
        }
        return utxos;
    }

    @Override
    public boolean verifyCoinbase(ITrans transaction, BigInteger blockHeight, BigInteger fees) {
        return true;
    }

    @Override
    public boolean addCoinbase(ITrans transaction, BigInteger blockHeight, BigInteger fees) {
        return true;
    }

    List<ITrans> pending = new ArrayList<>();

    @Override
    public List<ITrans> getPending() {
        return pending;
    }

    List<String> usedUTXO = new ArrayList<>();
    @Override
    public List<String> getUsedUTXOs() {
        return usedUTXO;
    }

    @Override
    public void undoTransaction(ITrans transaction) {
        for (Output output : transaction.getOutputs()) {
            for (IAddress a : ki.getAddMan().getAll()) {
                if (output.getAddress().encodeForChain().equals(a.encodeForChain())) {
                    utxoMap.remove(output.getID());
                }
            }
        }
        for (Input input : transaction.getInputs()) {
            for (IAddress a : ki.getAddMan().getAll()) {
                if (input.getAddress().encodeForChain().equals(a.encodeForChain())) {
                    utxoMap.put(input.getID(), input);
                }
            }
        }
    }

    @Override
    public boolean utxosChanged(IAddress address) {
        return true;
    }



    @Override
    public void close() {

    }

    @Override
    public void clear() {

    }

    @Override
    public ITrans createSimple(IAddress receiver, BigInteger amount, BigInteger fee, Token token, String message) throws InvalidTransactionException {
        return null;
    }

    @Override
    public ITrans createSimple(IAddress receiver, BigInteger amount, BigInteger fee, Token token, String message, int multipleOuts) throws InvalidTransactionException {
        return null;
    }

    @Override
    public boolean postBlockProcessing(Block block) {
        return false;
    }

    @Override
    public List<Input> getInputsForAmountAndToken(IAddress address, BigInteger amount, Token token, boolean used) {
        return null;
    }

    @Override
    public void unUseUTXOs(List<Input> inputs) {

    }
}
