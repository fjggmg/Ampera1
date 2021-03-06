package com.ampex.main.transactions;

import amp.Amplet;
import com.ampex.amperabase.*;
import com.ampex.main.IKi;
import com.ampex.main.data.buckets.KeyKeyTypePair;
import com.ampex.main.data.utils.Utils;
import engine.binary.IBinary;
import engine.data.writable_memory.on_ice.WritableMemory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionManagerLite extends Thread implements ITransMan {

    private IKi ki;

    public TransactionManagerLite(IKi ki) {

        this.ki = ki;

    }

    Map<String, TXIO> utxoMap = new ConcurrentHashMap<>();

    public void resetLite() {
        utxoMap.clear();
    }

    @Override
    public ITransAPI deserializeTransaction(Amplet amplet) throws InvalidTransactionException {
        return Transaction.fromAmplet(amplet);
    }

    @Override
    public boolean verifyTransaction(ITransAPI transaction) {


        return true;

    }

    @Override
    public boolean addTransaction(ITransAPI transaction) {

        pending.remove(transaction);
        for (IOutput output : transaction.getOutputs()) {
            for (IAddress a : ki.getAddMan().getAll()) {
                if (output.getAddress().encodeForChain().equals(a.encodeForChain())) {
                    utxoMap.put(output.getID(), output);
                }
            }
        }
        for (IInput input : transaction.getInputs()) {
            for (IAddress a : ki.getAddMan().getAll()) {
                if (input.getAddress().encodeForChain().equals(a.encodeForChain())) {
                    utxoMap.remove(input.getID());
                }
            }
        }
        if (!ki.getOptions().nogui && ki.getGUIHook() != null)
            ki.getGUIHook().pbpDone();
        return true;
    }

    @Override
    public boolean addTransactionNoVerify(ITransAPI transaction) {
        for (IOutput output : transaction.getOutputs()) {
            for (IAddress a : ki.getAddMan().getAll()) {
                if (output.getAddress().encodeForChain().equals(a.encodeForChain())) {
                    utxoMap.put(output.getID(), output);
                }
            }
        }
        for (IInput input : transaction.getInputs()) {
            for (IAddress a : ki.getAddMan().getAll()) {
                if (input.getAddress().encodeForChain().equals(a.encodeForChain())) {
                    utxoMap.remove(input.getID());
                }
            }
        }
        return true;
    }

    public void addUTXOs(List<IOutput> outputs) {
        for (IOutput o : outputs) {
            utxoMap.put(o.getID(), o);
        }
    }

    //always safe
    @Override
    public List<IOutput> getUTXOs(IAddress address, boolean safe) {
        List<IOutput> utxos = new ArrayList<>();
        for (Map.Entry<String, TXIO> ID : utxoMap.entrySet()) {
            if (!usedUTXO.contains(ID.getKey()) && ID.getValue().getAddress().encodeForChain().equals(address.encodeForChain()))
                utxos.add((Output) ID.getValue());
        }
        return utxos;
    }

    @Override
    public boolean verifyCoinbase(ITransAPI transaction, BigInteger blockHeight, BigInteger fees) {
        return true;
    }

    @Override
    public boolean addCoinbase(ITransAPI transaction, BigInteger blockHeight, BigInteger fees) {
        return true;
    }

    List<ITransAPI> pending = new ArrayList<>();

    @Override
    public List<ITransAPI> getPending() {
        return pending;
    }

    List<String> usedUTXO = new ArrayList<>();
    @Override
    public List<String> getUsedUTXOs() {
        return usedUTXO;
    }

    @Override
    public void undoTransaction(ITransAPI transaction) {
        for (IOutput output : transaction.getOutputs()) {
            for (IAddress a : ki.getAddMan().getAll()) {
                if (output.getAddress().encodeForChain().equals(a.encodeForChain())) {
                    utxoMap.remove(output.getID());
                }
            }
        }
        for (IInput input : transaction.getInputs()) {
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
    public ITrans createSimple(IAddress receiver, BigInteger amount, BigInteger fee, Token token, String message, IAddress changeAddress) throws InvalidTransactionException {

        return createSimple(receiver, amount, fee, token, message, 1, changeAddress);
    }

    @Override
    public ITrans createSimple(IAddress receiver, BigInteger amount, BigInteger fee, Token token, String message, int multipleOuts, IAddress changeAddress) throws InvalidTransactionException {
        if (ki.getEncryptMan().getPublicKey(ki.getAddMan().getMainAdd().getKeyType()) != null) {
            if (multipleOuts < 1)
                throw new InvalidTransactionException("Cannot create transaction with less than 1 output");
            if (multipleOuts % 10 != 0 && multipleOuts != 1)
                throw new InvalidTransactionException("To create a simple transaction with this method multiple outs must be divisible by 10 or equal to 1");

            List<IOutput> outputs = new ArrayList<>();
            for (int index = 0; index < multipleOuts; index++) {
                Output output = new Output(amount.divide(BigInteger.valueOf(multipleOuts)), receiver, token, index, System.currentTimeMillis(), Output.VERSION);
                outputs.add(output);
            }

            List<IInput> inputs = new ArrayList<>();

            //ki.getMainLog().info("Fee is: " + fee.toString());

            BigInteger totalInput = BigInteger.ZERO;
            IAddress a = ki.getAddMan().getMainAdd();
            List<String> sIns = new ArrayList<>();
            if (ki.getTransMan().getUTXOs(a, true) == null)
                throw new InvalidTransactionException("No UTXOs on this address");
            List<IInput> tempIns = getInputsForAmountAndToken(a, amount, token, true);
            if (tempIns != null)
                inputs.addAll(tempIns);
            else
                throw new InvalidTransactionException("Not enough " + token.getName() + " to do this transaction");

            for (IInput i : inputs) {
                sIns.add(i.getID());
                totalInput = totalInput.add(i.getAmount());
            }

            int outs = outputs.size();
            int ins = inputs.size() + 5;//arbitrary
            int pOuts = 0;
            int pIns = 0;
            int bIns = 0;
            int eIns = 0;
            for (IOutput o : outputs) {
                if (o.getAddress().isP2SH()) {
                    pOuts++;
                }
            }
            for (IInput i : inputs) {
                if (i.getAddress().isP2SH()) {
                    pIns++;
                }
                if (i.getAddress().getKeyType().equals(KeyType.BRAINPOOLP512T1)) {
                    bIns++;
                } else if (i.getAddress().getKeyType().equals(KeyType.ED25519)) {
                    eIns++;
                }
            }
            pIns = pIns + 2;//arbitrary
            BigInteger calcFee = TransactionFeeCalculator.calculateMinFee(outs, ins, pOuts, pIns, eIns, bIns);
            if (fee.compareTo(calcFee) < 0) {
                fee = calcFee;
            }
            if (!token.equals(Token.ORIGIN) || totalInput.subtract(amount).compareTo(fee) < 0) {
                List<IInput> tempFee = getInputsForAmountAndToken(a, fee, Token.ORIGIN, true);
                if (tempFee == null) throw new InvalidTransactionException("Not enough Origin to pay this fee");
                inputs.addAll(tempFee);
                for (IInput i : tempFee) {
                    sIns.add(i.getID());
                }
            }

            Map<String, IKSEP> keySigMap = new HashMap<>();
            IKSEP ksep = new KeySigEntropyPair(null, ki.getAddMan().getEntropyForAdd(ki.getAddMan().getMainAdd()), sIns, ki.getAddMan().getMainAdd().getPrefix(), false, a.getKeyType());
            keySigMap.put(ki.getEncryptMan().getPublicKeyString(a.getKeyType()), ksep);
            ITrans trans = new NewTrans(message, outputs, inputs, keySigMap, TransactionType.NEW_TRANS);
            ki.debug("Transaction has: " + trans.getOutputs().size() + " Outputs before finalization");
            trans.makeChange(fee, changeAddress);
            trans.addSig(ki.getEncryptMan().getPublicKeyString(a.getKeyType()), Utils.toBase64(ki.getEncryptMan().sign(trans.toSignBytes(), a.getKeyType())));
            ki.debug("Transaction has: " + trans.getOutputs().size() + " Outputs after finalization");
            //usedUTXOs.addAll(sIns);
            return trans;

        }

        throw new InvalidTransactionException("Public key null");
    }

    @Override
    public ITrans createSimpleMultiSig(IBinary bin, IAddress receiver, BigInteger amount, BigInteger fee, Token token, String message, int multipleOuts, IAddress changeAddress) throws InvalidTransactionException {
        if (ki.getEncryptMan().getPublicKey(ki.getAddMan().getMainAdd().getKeyType()) != null) {
            if (multipleOuts < 1)
                throw new InvalidTransactionException("Cannot create transaction with less than 1 output");
            if (multipleOuts % 10 != 0 && multipleOuts != 1)
                throw new InvalidTransactionException("To create a simple transaction with this method multiple outs must be divisible by 10 or equal to 1");

            List<IOutput> outputs = new ArrayList<>();
            for (int index = 0; index < multipleOuts; index++) {
                Output output = new Output(amount.divide(BigInteger.valueOf(multipleOuts)), receiver, token, index, System.currentTimeMillis(), Output.VERSION);
                outputs.add(output);
            }

            java.util.List<IInput> inputs = new ArrayList<>();

            //ki.getMainLog().info("Fee is: " + fee.toString());

            BigInteger totalInput = BigInteger.ZERO;
            IAddress a = ki.getAddMan().getMainAdd();
            List<String> sIns = new ArrayList<>();
            if (ki.getTransMan().getUTXOs(a, true) == null)
                throw new InvalidTransactionException("No UTXOs on this address");
            for (IOutput o : ki.getTransMan().getUTXOs(a, true)) {
                if (o.getToken().equals(token)) {
                    if (sIns.contains(Input.fromOutput(o).getID())) continue;
                    inputs.add(Input.fromOutput(o));
                    sIns.add(Input.fromOutput(o).getID());
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
            int bIns = 0;
            int eIns = 0;
            for (IOutput o : outputs) {
                if (o.getAddress().isP2SH()) {
                    pOuts++;
                }
            }
            for (IInput i : inputs) {
                if (i.getAddress().isP2SH()) {
                    pIns++;
                }
                if (i.getAddress().getKeyType().equals(KeyType.BRAINPOOLP512T1)) {
                    bIns++;
                } else if (i.getAddress().getKeyType().equals(KeyType.ED25519)) {
                    eIns++;
                }
            }
            pIns = pIns + 2;//arbitrary
            BigInteger calcFee = TransactionFeeCalculator.calculateMinFee(outs, ins, pOuts, pIns, eIns, bIns);
            if (fee.compareTo(calcFee) < 0) {
                fee = calcFee;
            }
            BigInteger feeInput = (token.equals(Token.ORIGIN)) ? totalInput : BigInteger.ZERO;

            for (IOutput o : ki.getTransMan().getUTXOs(a, true)) {
                if (o.getToken().equals(Token.ORIGIN)) {
                    if (!sIns.contains(Input.fromOutput(o).getID())) {
                        inputs.add(Input.fromOutput(o));
                        sIns.add(Input.fromOutput(o).getID());
                        feeInput = feeInput.add(o.getAmount());
                        if (feeInput.compareTo(fee) >= 0) break;
                    }

                }
            }


            if (feeInput.compareTo(fee) < 0) {
                throw new InvalidTransactionException("Not enough origin to pay for this fee");
            }

            Map<String, IKSEP> keySigMap = new HashMap<>();
            IKSEP ksep = new KeySigEntropyPair(null, ki.getAddMan().getEntropyForAdd(ki.getAddMan().getMainAdd()), sIns, ki.getAddMan().getMainAdd().getPrefix(), true, a.getKeyType());
            keySigMap.put(Utils.toBase64(bin.serializeToAmplet().serializeToBytes()), ksep);
            ITrans trans = new NewTrans(message, outputs, inputs, keySigMap, TransactionType.NEW_TRANS);
            ki.debug("Transaction has: " + trans.getOutputs().size() + " Outputs before finalization");
            trans.makeChange(fee, changeAddress);
            //trans.addSig(ki.getEncryptMan().getPublicKeyString(a.getKeyType()), utils.toBase64(ki.getEncryptMan().sign(trans.toSignBytes(), a.getKeyType())));
            WritableMemory wm = new WritableMemory();
            //magic value but works, may update later
            int i = 0;
            for (; i < 32; i++) {
                try {

                    KeyKeyTypePair kktp = KeyKeyTypePair.fromBytes(bin.getConstantMemory().getElement(i).getData());
                    if (kktp == null) break;
                    //if (kktp.getKey() == null) break;
                    if (kktp.getKeyType() == null) break;
                    if (ki.getEncryptMan().getPublicKeyString(kktp.getKeyType()).equals(Utils.toBase64(kktp.getKey()))) {
                        ki.debug("Adding signature to transaction with key: " + kktp.getKeyType());
                        wm.setElement(ki.getEncryptMan().sign(trans.toSignBytes(), kktp.getKeyType()), i);
                    }
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    //fail quietly
                    break;
                }
            }
            trans.addSig(Utils.toBase64(bin.serializeToAmplet().serializeToBytes()), Utils.toBase64(wm.serializeToBytes()));
            ki.debug("Transaction has: " + trans.getOutputs().size() + "Outputs after finalization");
            usedUTXO.addAll(sIns);
            return trans;

        }
        throw new InvalidTransactionException("Public key null");
    }
    //no pbp because lite doesn't verify transactions, it just accepts them
    @Override
    public boolean postBlockProcessing(BigInteger height) {
        return true;
    }

    @Override
    public List<IInput> getInputsForAmountAndToken(IAddress address, BigInteger amount, Token token, boolean used) {
        List<IOutput> utxos = getUTXOs(address, true);
        BigInteger total = BigInteger.ZERO;
        List<IInput> inputs = new ArrayList<>();
        List<String> usedIns = new ArrayList<>();
        for (IOutput o : utxos) {
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
    public List<IInput> getInputsForToken(IAddress address, Token token) {
        List<IOutput> utxos = getUTXOs(address, true);
        List<IInput> inputs = new ArrayList<>();
        for (IOutput output : utxos) {
            if (output.getToken().equals(token)) {
                inputs.add(Input.fromOutput(output));
            }
        }
        return inputs;
    }

    @Override
    public BigInteger getAmountInWallet(IAddress address, Token token) {
        List<IInput> inputs = getInputsForToken(address, token);
        BigInteger amount = BigInteger.ZERO;
        for (IInput input : inputs) {
            amount = amount.add(input.getAmount());
        }
        return amount;
    }

    @Override
    public void unUseUTXOs(List<IInput> inputs) {
        for (IInput i : inputs) {
            usedUTXO.remove(i.getID());
        }
    }

    @Override
    public boolean hasUTXOsOnDisk(IAddress address) {
        return false;
    }

    @Override
    public IKSEP createKSEP(String sig, String entropy, List<String> inputs, String prefix, boolean p2sh, KeyType keyType) {
        return new KeySigEntropyPair(sig, entropy, inputs, prefix, p2sh, keyType);
    }

    @Override
    public ITransAPI createTrans(String message, List<IOutput> outputs, List<IInput> inputs, Map<String, IKSEP> keySigMap) {
        try {
            return new NewTrans(message, outputs, inputs, keySigMap, TransactionType.NEW_TRANS);
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public IOutput createOutput(BigInteger amount, IAddress receiver, Token token, int index, long timestamp) {
        return new Output(amount, receiver, token, index, timestamp, Output.VERSION);
    }

    @Override
    public IOutput deserializeOutput(byte[] bytes) {
        return Output.fromBytes(bytes);
    }
}
