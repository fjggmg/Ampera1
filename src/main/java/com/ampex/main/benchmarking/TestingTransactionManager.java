package com.ampex.main.benchmarking;

import amp.Amplet;
import com.ampex.amperabase.*;
import com.ampex.main.IKi;
import com.ampex.main.blockchain.ChainManager;
import com.ampex.main.data.buckets.KeyKeyTypePair;
import com.ampex.main.data.utils.Utils;
import com.ampex.main.transactions.*;
import database.XodusAmpMap;
import engine.binary.IBinary;
import engine.data.writable_memory.on_ice.WritableMemory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestingTransactionManager extends Thread implements ITransMan{
    private IKi ki;
    private boolean nodisk;

    private XodusAmpMap utxoVerMap;
    public TestingTransactionManager(IKi ki, boolean nodisk, Map<String,TXIOData> prePop) {
        this.ki = ki;
        this.nodisk = nodisk;
        if(!nodisk) {
            utxoVerMap = new XodusAmpMap("benchTrans/utxoVer.dat");
            utxoVerMap.clear();
            for (Map.Entry<String, TXIOData> data : prePop.entrySet()) {
                utxoVerMap.putBytes(data.getKey(), data.getValue().serializeToBytes());
            }
        }
    }


    @Override
    public void run() {

    }
    @Override
    public void interrupt() {
        super.interrupt();
    }

    @Override
    public boolean hasUTXOsOnDisk(IAddress address) {
        return false;
    }

    @Override
    public boolean verifyTransaction(ITransAPI transaction) {

            //ki.debug("Verifying transaction: " + transaction.getID());


        for (IInput i : transaction.getInputs()) {

            if(!nodisk)
            {
                TXIOData data;
                try {
                    data = TXIOData.fromByteArray(utxoVerMap.getBytes(i.getID()));
                    if (data == null) {
                        if (ki.getOptions().tDebug)
                            ki.getMainLog().warn("Input already spent, bad transaction");
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

                if (ki.getOptions().tDebug)
                    ki.debug("input not spent");

                if (data.getAmount().compareTo(i.getAmount()) != 0) {
                    if (ki.getOptions().tDebug)
                        ki.getMainLog().warn("input is incorrect amount");
                    return false;
                }
                if (ki.getOptions().tDebug)
                    ki.debug("input correct amount");
                if (!data.getAddress().encodeForChain().equals(i.getAddress().encodeForChain())) {
                    if (ki.getOptions().tDebug)
                        ki.getMainLog().warn("Input not for this address");
                    return false;
                }
                if (data.getIndex() != i.getIndex()) {
                    if (ki.getOptions().tDebug)
                        ki.getMainLog().warn("Wrong input index");
                    return false;
                }
                if (!data.getToken().equals(i.getToken())) {
                    if (ki.getOptions().tDebug)
                        ki.getMainLog().warn("Wrong token");
                    return false;
                }
            }

            if (i == null) {
                    //ki.getMainLog().warn("Input is null, malformed transaction.");
                return false;
            }
        }
            //ki.debug("all inputs verified");
        if (!transaction.verifyInputToOutput()) {
                //ki.debug("Input values are not equal to output values");
            return false;
        }
        //ki.debug("input to output verifies");
        if (!transaction.verifyCanSpend()) {
                //ki.debug("this address cannot spend this input");
            return false;
        }
            //ki.debug("verified can spend");
        if (!transaction.verifySigs()) {
                //ki.debug("the signature on this transaction does not match");
            return false;
        }
        if (!transaction.verifySpecial(ki)) {
                //ki.debug("Contract requirements for this transaction have not been met");
            return false;
        }
            //ki.debug("verified signature");
            //ki.debug("Transaction verified");

        return true;
    }

    @Override
    public boolean addTransaction(ITransAPI transaction) {
        return verifyTransaction(transaction) && addTransactionNoVerify(transaction);
    }

    /**
     * very dangerous method, only use when you are certain the transaction you are adding is valid
     *
     * @param transaction transaction to add to DB
     * @return true if successful
     */
    @Override
    public boolean addTransactionNoVerify(ITransAPI transaction) {
        return true;
    }

    @Override
    public List<IOutput> getUTXOs(IAddress address, boolean safe) {
       return null;
    }

    @Override
    public boolean verifyCoinbase(ITransAPI transaction, BigInteger blockHeight, BigInteger fees) {
        if (ki.getOptions().tDebug) {
            ki.debug("Verifying coinbase transaction");
            ki.debug("It has: " + transaction.getOutputs().size() + " outputs");
        }
        if (blockHeight.compareTo(BigInteger.ZERO) != 0) {
            if (transaction.getOutputs().size() > 1) return false;

            if (!transaction.getOutputs().get(0).getToken().equals(Token.ORIGIN)) return false;

            if (transaction.getOutputs().get(0).getAmount().compareTo(ChainManager.blockRewardForHeight(blockHeight).add(fees)) != 0)
                return false;
        }
        return true;
    }

    @Override
    public boolean addCoinbase(ITransAPI transaction, BigInteger blockHeight, BigInteger fees) {


        return true;
    }

    @Override
    public void resetLite() {
        //not implemented
    }

    @Override
    public ITransAPI deserializeTransaction(Amplet amplet) throws InvalidTransactionException {
        return Transaction.fromAmplet(amplet);
    }

    @Override
    public List<ITransAPI> getPending() {
        return null;
    }

    @Override
    public void addUTXOs(List<IOutput> list) {
        //not implemented
    }

    @Override
    public List<String> getUsedUTXOs() {
        return null;
    }

    @Override
    public void undoTransaction(ITransAPI transaction) {

    }

    @Override
    public boolean utxosChanged(IAddress address) {

        return true;
    }

    @Override
    public void close() {
        interrupt();
    }

    @Override
    public void clear() {

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

            List<IInput> inputs = new ArrayList<>();

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
            //magic value but works may update later
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
            return trans;

        }
        throw new InvalidTransactionException("Public key null");
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
            BigInteger calcFee = TransactionFeeCalculator.calculateMinFee(outs, ins, pOuts, pIns, eIns,bIns);
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

    //private Map<BigInteger, IBlockAPI> processMap = new HashMap<>();

    @Override
    public boolean postBlockProcessing(BigInteger height) {

        return true;
    }

    @Override
    public List<IInput> getInputsForAmountAndToken(IAddress address, BigInteger amount, Token token, boolean used) {
        List<IInput> inputs = new ArrayList<>();
        List<IOutput> outs = getUTXOs(address, true);
        BigInteger totalIn = BigInteger.ZERO;
        if (outs == null) return null;
        for (IOutput o : outs) {
            if (o.getToken().equals(token)) {
                inputs.add(Input.fromOutput(o));
                totalIn = totalIn.add(o.getAmount());
                if (totalIn.compareTo(amount) >= 0) {
                    break;
                }
            }
        }
        if (totalIn.compareTo(amount) < 0)
            return null;

        return inputs;
    }

    @Override
    public List<IInput> getInputsForToken(IAddress address, Token token) {
        List<IInput> inputs = new ArrayList<>();
        List<IOutput> outs = getUTXOs(address, true);
        if (outs == null) return null;
        for (IOutput o : outs) {
            if (o.getToken().equals(token)) {
                inputs.add(Input.fromOutput(o));
            }
        }
        return inputs;
    }

    @Override
    public BigInteger getAmountInWallet(IAddress address, Token token) {
        BigInteger amount = BigInteger.ZERO;
        List<IOutput> outs = getUTXOs(address, true);
        if (outs == null) return BigInteger.ZERO;
        for (IOutput o : outs) {
            if (o.getToken().equals(token)) {
                amount = amount.add(o.getAmount());
            }
        }
        return amount;
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
    public IKSEP createKSEP(String sig, String entropy, List<String> inputs, String prefix, boolean p2sh, KeyType keyType) {
        return new KeySigEntropyPair(sig, entropy, inputs, prefix, p2sh, keyType);
    }

    @Override
    public IOutput createOutput(BigInteger amount, IAddress receiver, Token token, int index, long timestamp) {
        return new Output(amount, receiver, token, index, timestamp, Output.VERSION);
    }

    @Override
    public IOutput deserializeOutput(byte[] bytes) {
        return Output.fromBytes(bytes);
    }

    @Override
    public void unUseUTXOs(List<IInput> inputs) {
    }

}
