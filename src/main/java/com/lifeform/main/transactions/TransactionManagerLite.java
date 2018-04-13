package com.lifeform.main.transactions;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.data.Utils;

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

    //always safe
    @Override
    public List<Output> getUTXOs(IAddress address, boolean safe) {
        List<Output> utxos = new ArrayList<>();
        for (String ID : utxoMap.keySet()) {
            if (!usedUTXO.contains(ID) && utxoMap.get(ID).getAddress().encodeForChain().equals(address.encodeForChain()))
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

        return createSimple(receiver, amount, fee, token, message, 1);
    }

    @Override
    public ITrans createSimple(IAddress receiver, BigInteger amount, BigInteger fee, Token token, String message, int multipleOuts) throws InvalidTransactionException {
        if (ki.getEncryptMan().getPublicKey(ki.getAddMan().getMainAdd().getKeyType()) != null) {
            if (multipleOuts < 1)
                throw new InvalidTransactionException("Cannot create transaction with less than 1 output");
            if (multipleOuts % 10 != 0 && multipleOuts != 1)
                throw new InvalidTransactionException("To create a simple transaction with this method multiple outs must be divisible by 10 or equal to 1");

            List<Output> outputs = new ArrayList<>();
            for (int index = 0; index < multipleOuts; index++) {
                Output output = new Output(amount.divide(BigInteger.valueOf(multipleOuts)), receiver, token, index, System.currentTimeMillis(), (byte) 2);
                outputs.add(output);
            }

            java.util.List<Input> inputs = new ArrayList<>();

            //ki.getMainLog().info("Fee is: " + fee.toString());

            BigInteger totalInput = BigInteger.ZERO;
            IAddress a = ki.getAddMan().getMainAdd();
            if (ki.getTransMan().getUTXOs(a, true) == null)
                throw new InvalidTransactionException("No UTXOs on this address");
            for (Output o : ki.getTransMan().getUTXOs(a, true)) {
                if (o.getToken().equals(token)) {
                    if (inputs.contains(Input.fromOutput(o))) continue;
                    inputs.add(Input.fromOutput(o));
                    totalInput = totalInput.add(o.getAmount());
                    if (totalInput.compareTo(amount) >= 0) break;

                }
            }


            if (totalInput.compareTo(amount) < 0) {
                throw new InvalidTransactionException("Not enough " + token.name() + " to do this transaction");
            }
            int outs = outputs.size();
            int ins = inputs.size() + 5;//arbitrary
            int pOuts = 0;
            int pIns = 0;
            for (Output o : outputs) {
                if (o.getAddress().isP2SH()) {
                    pOuts++;
                }
            }
            for (Input i : inputs) {
                if (i.getAddress().isP2SH()) {
                    pIns++;
                }
            }
            pIns = pIns + 2;//arbitrary
            BigInteger calcFee = TransactionFeeCalculator.calculateMinFee(outs, ins, pOuts, pIns);
            if (fee.compareTo(calcFee) < 0) {
                fee = calcFee;
            }
            BigInteger feeInput = (token.equals(Token.ORIGIN)) ? totalInput : BigInteger.ZERO;

            //get inputs

            for (Output o : ki.getTransMan().getUTXOs(a, true)) {
                if (o.getToken().equals(Token.ORIGIN)) {
                    inputs.add(Input.fromOutput(o));
                    feeInput = feeInput.add(o.getAmount());
                    if (feeInput.compareTo(fee) >= 0) break;

                }
            }


            if (feeInput.compareTo(fee) < 0) {
                throw new InvalidTransactionException("Not enough origin to pay for this fee");
            }


            List<String> sIns = new ArrayList<>();
            for (Input i : inputs) {
                sIns.add(i.getID());
            }
            Map<String, KeySigEntropyPair> keySigMap = new HashMap<>();
            KeySigEntropyPair ksep = new KeySigEntropyPair(null, ki.getAddMan().getEntropyForAdd(ki.getAddMan().getMainAdd()), sIns, ki.getAddMan().getMainAdd().getPrefix(), false, a.getKeyType());
            keySigMap.put(ki.getEncryptMan().getPublicKeyString(a.getKeyType()), ksep);
            ITrans trans = new NewTrans(message, outputs, inputs, keySigMap, TransactionType.NEW_TRANS);
            ki.debug("Transaction has: " + trans.getOutputs().size() + " Outputs before finalization");
            trans.makeChange(fee, ki.getAddMan().getMainAdd()); // TODO this just sends change back to the main address......will need to give option later
            trans.addSig(ki.getEncryptMan().getPublicKeyString(a.getKeyType()), Utils.toBase64(ki.getEncryptMan().sign(trans.toSignBytes(), a.getKeyType())));
            ki.debug("Transaction has: " + trans.getOutputs().size() + "Outputs after finalization");
            usedUTXO.addAll(sIns);
            return trans;

        }

        throw new InvalidTransactionException("Public key null");
    }

    //no pbp because lite doesn't verify transactions, it just accepts them
    @Override
    public boolean postBlockProcessing(Block block) {
        return true;
    }

    @Override
    public List<Input> getInputsForAmountAndToken(IAddress address, BigInteger amount, Token token, boolean used) {
        List<Output> utxos = getUTXOs(address, true);
        BigInteger total = BigInteger.ZERO;
        List<Input> inputs = new ArrayList<>();
        List<String> usedIns = new ArrayList<>();
        for (Output o : utxos) {
            if (total.compareTo(amount) >= 0) break;
            if (o.getToken().equals(token)) {
                inputs.add(Input.fromOutput(o));
                total = total.add(o.getAmount());
                if (used) usedIns.add(o.getID());
            }
        }
        if (total.compareTo(amount) < 0) return null;
        usedUTXO.addAll(usedIns);
        return inputs;
    }

    @Override
    public void unUseUTXOs(List<Input> inputs) {
        for (Input i : inputs) {
            usedUTXO.remove(i.getID());
        }
    }
}
