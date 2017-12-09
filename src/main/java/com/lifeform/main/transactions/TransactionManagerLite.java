package com.lifeform.main.transactions;

import com.lifeform.main.IKi;

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

    Map<String, Output> utxoMap = new HashMap<>();

    @Override
    public boolean verifyTransaction(ITrans transaction) {


        return true;

    }

    @Override
    public boolean addTransaction(ITrans transaction) {

        for (Output output : transaction.getOutputs()) {
            for (Address a : ki.getAddMan().getAll()) {
                if (output.getAddress().encodeForChain().equals(a.encodeForChain())) {
                    utxoMap.put(output.getID(), output);
                }
            }
        }
        for (Input input : transaction.getInputs()) {
            for (Address a : ki.getAddMan().getAll()) {
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
            for (Address a : ki.getAddMan().getAll()) {
                if (output.getAddress().encodeForChain().equals(a.encodeForChain())) {
                    utxoMap.put(output.getID(), output);
                }
            }
        }
        for (Input input : transaction.getInputs()) {
            for (Address a : ki.getAddMan().getAll()) {
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
    public List<Output> getUTXOs(Address address) {
        List<Output> utxos = new ArrayList<>();
        for (String ID : utxoMap.keySet()) {
            utxos.add(utxoMap.get(ID));
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

    @Override
    public List<String> getUsedUTXOs() {
        return null;
    }

    @Override
    public boolean utxosChanged(Address address) {
        return true;
    }

    @Override
    public void commit() {

    }

    @Override
    public void close() {

    }

    @Override
    public void clear() {

    }
}
