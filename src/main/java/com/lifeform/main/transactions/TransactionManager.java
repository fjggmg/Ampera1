package com.lifeform.main.transactions;

import amp.HeadlessPrefixedAmplet;
import amp.database.XodusAmpMap;
import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.blockchain.ChainManager;
import com.lifeform.main.data.*;
import com.lifeform.main.network.Packet;
import engine.binary.Binary;
import engine.data.WritableMemory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Bryan on 8/11/2017.
 */
public class TransactionManager implements ITransMan {


    private XodusAmpMap utxoAmp;
    private XodusAmpMap utxoVerMap;
    private IKi ki;
    private List<ITrans> pending = new CopyOnWriteArrayList<>();
    private List<String> usedUTXOs = new ArrayList<>();
    private BigInteger currentHeight = BigInteger.valueOf(-1);
    private final Object processLock = new Object();
    public TransactionManager(IKi ki, boolean dump) {
        this.ki = ki;
        if (!new File("transactions" + ((ki.getOptions().testNet) ? ChainManager.TEST_NET : ChainManager.POW_CHAIN) + "/").mkdirs()) {
            ki.getMainLog().warn("Unable to create transactions folder");
        }
        utxoAmp = new XodusAmpMap("transactions" + ((ki.getOptions().testNet) ? ChainManager.TEST_NET : ChainManager.POW_CHAIN) + "/utxoAmp.dat");//utxoDB.hashMap("utxoDB", Serializer.STRING, Serializer.STRING).createOrOpen();
        utxoVerMap = new XodusAmpMap("transactions" + ((ki.getOptions().testNet) ? ChainManager.TEST_NET : ChainManager.POW_CHAIN) + "/utxoVer.dat");
        new Thread() {
            public void run() {
                List<ITrans> toRemove = new ArrayList<>();
                setName("Transaction Cleanup");
                while (true) {
                    ki.debug("Running transaction cleanup");
                    for (ITrans t : pending) {
                        if (t.getOutputs().get(0).getTimestamp() < System.currentTimeMillis() - 3_600_000) {
                            toRemove.add(t);
                        }
                    }
                    if (!toRemove.isEmpty()) {
                        for (ITrans t : toRemove) {
                            unUseUTXOs(t.getInputs());
                        }
                        pending.removeAll(toRemove);
                        toRemove.clear();

                    }
                    try {
                        sleep(3_600_0000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        new Thread() {
            public void run() {

                setName("Post Block Processing");
                while (true) {
                    ki.debug("Post block processing on #" + currentHeight);
                    Block b = processMap.remove(currentHeight);
                    currentHeight = currentHeight.add(BigInteger.ONE);
                    if (b != null) {
                        ITrans coinbase = b.getCoinbase();
                        for (Output o : coinbase.getOutputs()) {
                            HeadlessPrefixedAmplet hpa;
                            if (utxoAmp.getBytes(o.getAddress().toByteArray()) != null)
                                hpa = HeadlessPrefixedAmplet.create(utxoAmp.getBytes(o.getAddress().toByteArray()));
                            else
                                hpa = HeadlessPrefixedAmplet.create();

                            //ki.debug("Putting output: " + o.getID() + " with address: " + o.getAddress().encodeForChain() + " of amount + " + o.getAmount());
                            hpa.addElement(o.getID());
                            try {
                                hpa.addBytes(new TXIOData(o.getAddress(), o.getIndex(), o.getAmount(), o.getToken(), o.getTimestamp(), o.getVersion()).serializeToBytes());
                            } catch (InvalidTXIOData invalidTXIOData) {
                                invalidTXIOData.printStackTrace();
                                continue;
                            }
                            utxoAmp.putBytes(o.getAddress().toByteArray(), hpa.serializeToBytes());

                        }
                        for (String trans : b.getTransactionKeys()) {

                            ITrans t = b.getTransaction(trans);
                            for (Input i : t.getInputs()) {
                                getUsedUTXOs().remove(i.getID());
                                HeadlessPrefixedAmplet hpa;
                                if (utxoAmp.getBytes(i.getAddress().toByteArray()) != null)
                                    hpa = HeadlessPrefixedAmplet.create(utxoAmp.getBytes(i.getAddress().toByteArray()));
                                else
                                    hpa = HeadlessPrefixedAmplet.create();
                                while (hpa.peekNextElement() != null) {
                                    try {
                                        String ID = new String(hpa.peekNextElement(), "UTF-8");
                                        if (ID.equals(i.getID())) {
                                            //ki.debug("Removing output: " + i.getID() + " with address: " + i.getAddress().encodeForChain() + " of amount + " + i.getAmount());

                                            hpa.deleteNextElement();
                                            hpa.deleteNextElement();
                                            utxoAmp.putBytes(i.getAddress().toByteArray(), hpa.serializeToBytes());
                                            break;
                                        } else {
                                            hpa.getNextElement();
                                            hpa.getNextElement();
                                        }
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                        continue;
                                    }
                                }

                            }
                            for (Output o : t.getOutputs()) {
                                HeadlessPrefixedAmplet hpa;
                                if (utxoAmp.getBytes(o.getAddress().toByteArray()) != null)
                                    hpa = HeadlessPrefixedAmplet.create(utxoAmp.getBytes(o.getAddress().toByteArray()));
                                else
                                    hpa = HeadlessPrefixedAmplet.create();

                                    hpa.addElement(o.getID());
                                //ki.debug("Putting output: " + o.getID() + " with address: " + o.getAddress().encodeForChain() + " of amount + " + o.getAmount());

                                try {
                                    hpa.addBytes(new TXIOData(o.getAddress(), o.getIndex(), o.getAmount(), o.getToken(), o.getTimestamp(), o.getVersion()).serializeToBytes());
                                } catch (InvalidTXIOData invalidTXIOData) {
                                    invalidTXIOData.printStackTrace();
                                    continue;
                                }
                                utxoAmp.putBytes(o.getAddress().toByteArray(), hpa.serializeToBytes());


                            }

                            ki.getExMan().transactionProccessed(trans);
                        }
                    }

                        synchronized (processLock) {
                            try {
                                if (processMap.isEmpty())
                                    processLock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                }
            }
        }.start();

        if (dump) {
            /*
            StringFileHandler fh = new StringFileHandler(ki,"utxoDump.txt");
            fh.delete();
            for(String key:utxoMap.keySet())
            {

                fh.addLine("Address: " + key);
                List<String> dumpList = JSONManager.parseJSONToList(utxoMap.get(key));
                for(String value:dumpList)
                {
                    fh.addLine(value);
                }

            }
            fh.save();
            fh = new StringFileHandler(ki,"utxoValueDump.txt");
            fh.delete();
            for(String key:utxoValueMap.keySet())
            {
                fh.addLine(key + " " + utxoValueMap.get(key));
            }
            fh.save();

            fh = new StringFileHandler(ki,"utxoSpentDump.txt");
            fh.delete();
            for(String key:utxoSpent.keySet())
            {
                fh.addLine(key + " " + utxoSpent.get(key));
            }
            fh.save();
            */
            //old dump does not work
        }
    }

    @Override
    public boolean verifyTransaction(ITrans transaction) {

        if (ki.getOptions().tDebug)
            ki.debug("Verifying transaction: " + transaction.getID());


        for (Input i : transaction.getInputs()) {
            if (ki.getOptions().tDebug)
                ki.debug("Verifying input: " + i.getID());
            if (utxoVerMap == null) {
                if (ki.getOptions().tDebug)
                    ki.getMainLog().warn("UTXO file uninitialized, installation corrupted or fatal program error");
                return false;
            }
            if (i == null) {
                if (ki.getOptions().tDebug)
                    ki.getMainLog().warn("Input is null, malformed transaction.");
                return false;
            }

            try {
                if (utxoVerMap.getBytes(i.getID()) == null) {
                    if (ki.getOptions().tDebug)
                        ki.getMainLog().warn("Input already spent, bad transaction");
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            TXIOData data = TXIOData.fromByteArray(utxoVerMap.getBytes(i.getID()));
            if (data == null) return false;
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
        if (ki.getOptions().tDebug)
            ki.debug("all inputs verified");
        if (!transaction.verifyInputToOutput()) {
            if (ki.getOptions().tDebug)
                ki.getMainLog().warn("Input values are not equal to output values");
            return false;
        }
        if (ki.getOptions().tDebug)
            ki.debug("input to output verifies");
        if (!transaction.verifyCanSpend()) { //TODO investigate if we still need this as we're checking address congruence above
            if (ki.getOptions().tDebug)
                ki.getMainLog().warn("this address cannot spend this input");
            return false;
        }
        if (ki.getOptions().tDebug)
            ki.debug("verified can spend");
        if (!transaction.verifySigs()) {
            if (ki.getOptions().tDebug)
                ki.getMainLog().warn("the signature on this transaction does not match");
            return false;
        }
        if (!transaction.verifySpecial(ki)) {
            if (ki.getOptions().tDebug)
                ki.getMainLog().warn("Contract requirements for this transaction have not been met");
            return false;
        }
        if (ki.getOptions().tDebug) {
            ki.debug("verified signature");
            ki.debug("Transaction verified");
        }
        return true;
    }

    @Override
    public boolean addTransaction(ITrans transaction) {
        return verifyTransaction(transaction) && addTransactionNoVerify(transaction);
    }

    /**
     * very dangerous method, only use when you are certain the transaction you are adding is valid
     *
     * @param transaction transaction to add to DB
     * @return true if successful
     */
    @Override
    public boolean addTransactionNoVerify(ITrans transaction) {
        if (ki.getOptions().tDebug)
            ki.debug("Saving transaction to disk");
        //region verification saving
        for (Input i : transaction.getInputs()) {
            utxoVerMap.remove(i.getID());
        }

        for (Output o : transaction.getOutputs()) {
            TXIOData data;
            try {
                data = new TXIOData(o.getAddress(), o.getIndex(), o.getAmount(), o.getToken(), o.getTimestamp(), o.getVersion());
            } catch (InvalidTXIOData invalidTXIOData) {
                invalidTXIOData.printStackTrace();
                return false;
            }
            utxoVerMap.putBytes(o.getID(), data.serializeToBytes());
        }
        //endregion

        //TODO what the fuck is going on in the next few lines, we're looking for the same transaction multiple times? Isn't this already prevented from happening?
        List<ITrans> toRemove = new ArrayList<>();
        for (ITrans t : pending) {
            if (t.getID().equals(transaction.getID())) toRemove.add(t);
        }
        pending.removeAll(toRemove);
        if (ki.getOptions().tDebug)
            ki.debug("Transaction removed from pending pool, done adding transaction");
        return true;
    }

    @Override
    public List<Output> getUTXOs(IAddress address, boolean safe) {
        if (utxoAmp.getBytes(address.toByteArray()) == null) {
            return null;
        }

        //ki.debug("Getting UTXOs for address: " + address.encodeForChain());
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(utxoAmp.getBytes(address.toByteArray()));
        //ki.debug("HPA has elements: " + hpa.hasNextElement());
        String utxoID;
        try {
            utxoID = new String(hpa.getNextElement(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        List<Output> utxos = new ArrayList<>();
        if (!utxoID.isEmpty()) {

            while (hpa.peekNextElement() != null) {
                if (usedUTXOs.contains(utxoID)) {
                    try {
                        hpa.getNextElement();
                    } catch (Exception e) {
                        break;
                    }
                } else {
                    TXIOData data = TXIOData.fromByteArray(hpa.getNextElement());
                    //ki.debug("Adding data" + utxoID + " " + data.getAmount());
                    if (data != null)
                        utxos.add(new Output(data.getAmount(), data.getAddress(), data.getToken(), data.getIndex(), data.getTimestamp(), data.getVersion()));
                }
                try {
                    utxoID = new String(hpa.getNextElement(), "UTF-8");
                } catch (NullPointerException | UnsupportedEncodingException e) {
                    //quietly fail, we're done here
                }
            }
        }
        return utxos;
    }

    @Override
    public boolean verifyCoinbase(ITrans transaction, BigInteger blockHeight, BigInteger fees) {
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
    public boolean addCoinbase(ITrans transaction, BigInteger blockHeight, BigInteger fees) {

        if (!verifyCoinbase(transaction, blockHeight, fees)) return false;
        for (Output o : transaction.getOutputs()) {
            if (ki.getOptions().tDebug) {
                ki.debug("Address " + o.getAddress().encodeForChain());
                ki.debug("ID: " + o.getID());
                ki.debug("Token " + o.getToken());
                ki.debug("Amount " + o.getAmount());
            }
            TXIOData data = null;
            try {
                data = new TXIOData(o.getAddress(), o.getIndex(), o.getAmount(), o.getToken(), o.getTimestamp(), o.getVersion());
            } catch (InvalidTXIOData invalidTXIOData) {
                invalidTXIOData.printStackTrace();
                return false;
            }
            utxoVerMap.putBytes(o.getID(), data.serializeToBytes());
            ki.getAddMan().receivedOn(o.getAddress());
        }


        return true;
    }

    @Override
    public List<ITrans> getPending() {
        return pending;
    }

    @Override
    public List<String> getUsedUTXOs() {
        return usedUTXOs;
    }

    @Override
    public void undoTransaction(ITrans transaction) {

    }

    @Override
    public boolean utxosChanged(IAddress address) {

        return true;
    }

    @Override
    public void close() {
        utxoAmp.close();
        utxoVerMap.close();
    }

    @Override
    public void clear() {

        utxoAmp.clear();
        utxoVerMap.clear();
    }


    @Override
    public ITrans createSimpleMultiSig(Binary bin, IAddress receiver, BigInteger amount, BigInteger fee, Token token, String message, int multipleOuts) throws InvalidTransactionException {
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
            List<String> sIns = new ArrayList<>();
            if (ki.getTransMan().getUTXOs(a, true) == null)
                throw new InvalidTransactionException("No UTXOs on this address");
            for (Output o : ki.getTransMan().getUTXOs(a, true)) {
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
            for (Output o : outputs) {
                if (o.getAddress().isP2SH()) {
                    pOuts++;
                }
            }
            for (Input i : inputs) {
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

            for (Output o : ki.getTransMan().getUTXOs(a, true)) {
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

            Map<String, KeySigEntropyPair> keySigMap = new HashMap<>();
            KeySigEntropyPair ksep = new KeySigEntropyPair(null, ki.getAddMan().getEntropyForAdd(ki.getAddMan().getMainAdd()), sIns, ki.getAddMan().getMainAdd().getPrefix(), true, a.getKeyType());
            keySigMap.put(Utils.toBase64(bin.serializeToAmplet().serializeToBytes()), ksep);
            ITrans trans = new NewTrans(message, outputs, inputs, keySigMap, TransactionType.NEW_TRANS);
            ki.debug("Transaction has: " + trans.getOutputs().size() + " Outputs before finalization");
            trans.makeChange(fee, ki.getAddMan().getMainAdd()); // TODO this just sends change back to the main address......will need to give option later
            //trans.addSig(ki.getEncryptMan().getPublicKeyString(a.getKeyType()), Utils.toBase64(ki.getEncryptMan().sign(trans.toSignBytes(), a.getKeyType())));
            WritableMemory wm = new WritableMemory();
            //TODO magic value because we can't get size of constant memory, and, also, we don't know how many keys are here.....
            int i = 0;
            for (; i < 32; i++) {
                try {

                    KeyKeyTypePair kktp = KeyKeyTypePair.fromBytes(bin.getConstantMemory().getElement(i).getData());
                    if (kktp == null) break;
                    if (kktp.getKey() == null) break;
                    if (kktp.getKeyType() == null) break;
                    if (ki.getEncryptMan().getPublicKeyString(kktp.getKeyType()).equals(Utils.toBase64(kktp.getKey()))) {
                        ki.debug("Adding signature to transaction with key: " + kktp.getKeyType());
                        wm.setElement(ki.getEncryptMan().sign(trans.toSignBytes(), kktp.getKeyType()), i);
                    }
                } catch (Exception e) {
                    //fail quietly
                    break;
                }
            }
            trans.addSig(Utils.toBase64(bin.serializeToAmplet().serializeToBytes()), Utils.toBase64(wm.serializeToBytes()));
            ki.debug("Transaction has: " + trans.getOutputs().size() + "Outputs after finalization");
            usedUTXOs.addAll(sIns);
            return trans;

        }
        throw new InvalidTransactionException("Public key null");
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
            List<String> sIns = new ArrayList<>();
            if (ki.getTransMan().getUTXOs(a, true) == null)
                throw new InvalidTransactionException("No UTXOs on this address");
            List<Input> tempIns = getInputsForAmountAndToken(a, amount, token, true);
            if (tempIns != null)
                inputs.addAll(tempIns);
            else
                throw new InvalidTransactionException("Not enough " + token.getName() + " to do this transaction");

            for (Input i : inputs) {
                sIns.add(i.getID());
                totalInput = totalInput.add(i.getAmount());
            }

            int outs = outputs.size();
            int ins = inputs.size() + 5;//arbitrary
            int pOuts = 0;
            int pIns = 0;
            int bIns = 0;
            int eIns = 0;
            for (Output o : outputs) {
                if (o.getAddress().isP2SH()) {
                    pOuts++;
                }
            }
            for (Input i : inputs) {
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
                List<Input> tempFee = getInputsForAmountAndToken(a, fee, Token.ORIGIN, true);
                if (tempFee == null) throw new InvalidTransactionException("Not enough Origin to pay this fee");
                inputs.addAll(tempFee);
                for (Input i : tempFee) {
                    sIns.add(i.getID());
                }
            }

            Map<String, KeySigEntropyPair> keySigMap = new HashMap<>();
            KeySigEntropyPair ksep = new KeySigEntropyPair(null, ki.getAddMan().getEntropyForAdd(ki.getAddMan().getMainAdd()), sIns, ki.getAddMan().getMainAdd().getPrefix(), false, a.getKeyType());
            keySigMap.put(ki.getEncryptMan().getPublicKeyString(a.getKeyType()), ksep);
            ITrans trans = new NewTrans(message, outputs, inputs, keySigMap, TransactionType.NEW_TRANS);
            ki.debug("Transaction has: " + trans.getOutputs().size() + " Outputs before finalization");
            trans.makeChange(fee, a); // TODO this just sends change back to the main address......will need to give option later
            trans.addSig(ki.getEncryptMan().getPublicKeyString(a.getKeyType()), Utils.toBase64(ki.getEncryptMan().sign(trans.toSignBytes(), a.getKeyType())));
            ki.debug("Transaction has: " + trans.getOutputs().size() + " Outputs after finalization");
            //usedUTXOs.addAll(sIns);
            return trans;

        }

        throw new InvalidTransactionException("Public key null");
    }

    Map<BigInteger, Block> processMap = new HashMap<>();

    @Override
    public boolean postBlockProcessing(Block block) {

        processMap.put(block.height, block);
        synchronized (processLock) {
            processLock.notify();
        }
        return true;
    }

    @Override
    public List<Input> getInputsForAmountAndToken(IAddress address, BigInteger amount, Token token, boolean used) {
        List<Input> inputs = new ArrayList<>();
        List<Output> outs = getUTXOs(address, true);
        BigInteger totalIn = BigInteger.ZERO;
        if (outs == null) return null;
        for (Output o : outs) {
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
        if (used) {
            List<String> usedOuts = new ArrayList<>();
            for (Input i : inputs) {
                usedOuts.add(i.getID());
            }
            usedUTXOs.addAll(usedOuts);
        }
        return inputs;
    }

    @Override
    public List<Input> getInputsForToken(IAddress address, Token token) {
        List<Input> inputs = new ArrayList<>();
        List<Output> outs = getUTXOs(address, true);
        if (outs == null) return null;
        for (Output o : outs) {
            if (o.getToken().equals(token)) {
                inputs.add(Input.fromOutput(o));
            }
        }
        return inputs;
    }

    @Override
    public BigInteger getAmountInWallet(IAddress address, Token token) {
        BigInteger amount = BigInteger.ZERO;
        List<Output> outs = getUTXOs(address, true);
        if (outs == null) return BigInteger.ZERO;
        for (Output o : outs) {
            if (o.getToken().equals(token)) {
                amount = amount.add(o.getAmount());
            }
        }
        return amount;
    }
    @Override
    public void unUseUTXOs(List<Input> inputs) {
        for (Input i : inputs) {
            usedUTXOs.remove(i.getID());
        }
    }

    @Override
    public void setCurrentHeight(BigInteger currentHeight) {
        this.currentHeight = currentHeight;
    }
}
