package com.lifeform.main.transactions;

import amp.Amplet;
import amp.HeadlessPrefixedAmplet;
import amp.classification.AmpClassCollection;
import amp.classification.classes.AC_ClassInstanceIDIsIndex;
import amp.classification.classes.AC_SingleElement;
import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.IChainMan;
import com.lifeform.main.data.AmpIDs;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.Utils;
import engine.binary.Binary;
import engine.data.DataElement;
import engine.data.WritableMemory;

import java.io.UnsupportedEncodingException;
import java.io.WriteAbortedException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewTrans implements ITrans {
    private List<Output> outputs;
    private List<Input> inputs;
    private String message;
    private TransactionType type;
    private Map<String, KeySigEntropyPair> keySigMap;

    public NewTrans(String message, List<Output> outputs, List<Input> inputs, Map<String, KeySigEntropyPair> keySigMap, TransactionType type) throws InvalidTransactionException {
        if (message.length() > 256) throw new InvalidTransactionException("Message too long");
        if (outputs.size() + inputs.size() > IChainMan.MAX_TXIOS)
            throw new InvalidTransactionException("Too many TXIOs");
        int i = 0;
        for (Output o : outputs) {
            if (o.getIndex() != i) throw new InvalidTransactionException("Bad Output index");
            i++;
        }
        for (String key : keySigMap.keySet()) {
            if (keySigMap.get(key).inputs == null) throw new InvalidTransactionException("Null inputs in KSEP");
            if (keySigMap.get(key).inputs.isEmpty()) throw new InvalidTransactionException("Empty inputs in KSEP");
        }
        this.outputs = outputs;
        this.inputs = inputs;
        this.keySigMap = keySigMap;
        this.type = type;
        this.message = message;
    }

    private String ID = null;

    @Override
    public String getID() {
        //System.out.println("Call to get ID from trans");
        if (ID != null) {
            return ID;
        } else {
            //System.out.println("ID was null, creating");
            ID = Utils.toBase64(EncryptionManager.sha3256(serializeToAmplet().serializeToBytes()));
            //System.out.println("ID is: " + ID);
            return ID;
        }
    }

    @Override
    public List<Output> getOutputs() {
        return outputs;
    }

    @Override
    public List<Input> getInputs() {
        return inputs;
    }

    @Override
    public boolean verifySigs() {
        for (String key : keySigMap.keySet()) {
            if (!keySigMap.get(key).p2sh) {
                if (!EncryptionManager.verifySig(toSignBytes(), Utils.fromBase64(keySigMap.get(key).sig), key, keySigMap.get(key).keyType))
                    return false;
            }

        }
        return true;
    }

    @Override
    public String toSign() {
        return null;
    }

    @Override
    public boolean verifyCanSpend() {
        List<String> inputIDs = new ArrayList<>();
        Map<String, Input> inputMap = new HashMap<>();
        for (Input i : inputs) {
            if (inputIDs.contains(i.getID())) {
                System.out.println("dup input");
                return false;
            }
            inputIDs.add(i.getID());
            inputMap.put(i.getID(), i);
        }
        for (String key : keySigMap.keySet()) {
            if (inputMap.get(keySigMap.get(key).inputs.get(0)) == null) {
                System.out.println("input 0 null");
                return false;
            }
            String address = inputMap.get(keySigMap.get(key).inputs.get(0)).getAddress().encodeForChain();
            KeySigEntropyPair ksep = keySigMap.get(key);
            if (!inputMap.get(ksep.inputs.get(0)).canSpend(key, ksep.entropy, ksep.prefix, ksep.p2sh, ksep.keyType)) {
                System.out.println("address mismatch, Address expecting: ");
                System.out.println(inputMap.get(ksep.inputs.get(0)).getAddress().encodeForChain());
                System.out.println("Received: ");
                try {
                    System.out.println(NewAdd.createNew(key, ksep.entropy, AddressLength.byIndicator(inputMap.get(ksep.inputs.get(0)).getAddress().toByteArray()[1]), ksep.p2sh, inputMap.get(ksep.inputs.get(0)).getAddress().getKeyType()).encodeForChain());
                } catch (InvalidAddressException e) {
                    e.printStackTrace();
                }
                return false;
            }
            for (String input : keySigMap.get(key).inputs) {
                if (!inputMap.get(input).getAddress().encodeForChain().equals(address)) {
                    System.out.println("address mismatch");
                    return false;
                }
                inputIDs.remove(input);
            }
        }
        if (!inputIDs.isEmpty()) {
            System.out.println("Inputs not empty");
            return false;
        }
        return true;
    }

    @Override
    public boolean verifyInputToOutput() {
        if (inputs.size() < 1 && outputs.size() < 1) return false;
        for (Token t : Token.values()) {
            BigInteger allInput = BigInteger.ZERO;
            for (Input i : inputs) {
                if (i.getToken().equals(t))
                    allInput = allInput.add(i.getAmount());
                if (i.getAmount().compareTo(BigInteger.ZERO) < 0) {
                    System.out.println("0 or negative input on token: " + i.getToken());
                    return false;
                }
            }

            BigInteger allOutput = BigInteger.ZERO;
            for (Output o : outputs) {
                if (o.getToken().equals(t))
                    allOutput = allOutput.add(o.getAmount());
                if (o.getAmount().compareTo(BigInteger.ZERO) < 0) {
                    System.out.println("0 or negative output on token: " + o.getToken());
                    return false;
                }
            }
            //System.out.println("Allin/Allout for: " + t.name());
            //System.out.println("" + allInput + "/" + allOutput);
            if (t.equals(Token.ORIGIN)) {
                if (allInput.compareTo(allOutput) < 0) {

                    return false;
                }
            } else {
                if (allInput.compareTo(allOutput) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean verifySpecial(IKi ki) {
        for (String key : keySigMap.keySet()) {
            KeySigEntropyPair ksep = keySigMap.get(key);
            if (ksep.p2sh) {
                IAddress execAdd = null;
                for (Input i : inputs) {
                    if (ksep.inputs.contains(i.getID()))
                        execAdd = i.getAddress();
                }
                if (execAdd == null) return false;
                try {
                    ArrayList<DataElement> result = ki.getBCE8().executeProgram(Binary.deserializeFromAmplet(Amplet.create(Utils.fromBase64(key))), WritableMemory.deserializeFromBytes(Utils.fromBase64(ksep.sig)), this, execAdd.toByteArray(), false);
                    if (result.get(0).getDataAsInt() != 0) return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public BigInteger getFee() {
        BigInteger allInput = BigInteger.ZERO;
        for (Input i : inputs) {
            if (i.getToken().equals(Token.ORIGIN))
                allInput = allInput.add(i.getAmount());

        }
        BigInteger allOutput = BigInteger.ZERO;
        for (Output o : outputs) {
            if (o.getToken().equals(Token.ORIGIN))
                allOutput = allOutput.add(o.getAmount());
        }
        return allInput.subtract(allOutput);
    }

    @Override
    public void makeChange(BigInteger fee, IAddress cAdd) {
        makeChangeSecondary(cAdd);
        BigInteger allInput = BigInteger.ZERO;
        for (Input i : inputs) {
            if (i.getToken().equals(Token.ORIGIN))
                allInput = allInput.add(i.getAmount());

        }
        BigInteger allOutput = BigInteger.ZERO;
        for (Output o : outputs) {
            if (o.getToken().equals(Token.ORIGIN))
                allOutput = allOutput.add(o.getAmount());
        }
        if (allInput.subtract(allOutput).compareTo(fee) <= 0) {
            //not enough left to make fee
            return;
        }
        Output o = new Output(allInput.subtract(allOutput).subtract(fee), cAdd, Token.ORIGIN, outputs.size(), System.currentTimeMillis(), (byte) 2);
        outputs.add(o);

    }

    private void makeChangeSecondary(IAddress cAdd) {
        for (Token t : Token.values()) {
            if (t.equals(Token.ORIGIN)) continue;
            BigInteger allInput = BigInteger.ZERO;
            boolean hasToken = false;
            for (Input i : inputs) {
                if (i.getToken().equals(t)) {
                    allInput = allInput.add(i.getAmount());
                    hasToken = true;
                }

            }
            if (!hasToken) continue;
            BigInteger allOutput = BigInteger.ZERO;
            for (Output o : outputs) {
                if (o.getToken().equals(t))
                    allOutput = allOutput.add(o.getAmount());
            }

            Output o = new Output(allInput.subtract(allOutput), cAdd, t, outputs.size(), System.currentTimeMillis(), (byte) 2);
            outputs.add(o);
        }
    }

    @Override
    public byte[] toSignBytes() {
        AC_SingleElement msg = null;
        if (message != null && !message.isEmpty())
            msg = AC_SingleElement.create(AmpIDs.MESSAGE_ID_GID, message);

        AC_ClassInstanceIDIsIndex inputs = AC_ClassInstanceIDIsIndex.create(AmpIDs.INPUTS_CID, "Inputs");
        for (Input i : getInputs()) {
            inputs.addElement(i.serializeToAmplet());
        }
        AC_ClassInstanceIDIsIndex outputs = AC_ClassInstanceIDIsIndex.create(AmpIDs.OUTPUTS_CID, "Outputs");
        for (Output o : getOutputs()) {
            outputs.addElement(o);
        }
        AC_SingleElement type = null;
        type = AC_SingleElement.create(AmpIDs.TYPE_GID, this.type.toString());

        AmpClassCollection acc = new AmpClassCollection();
        acc.addClass(type);
        acc.addClass(msg);
        acc.addClass(inputs);
        acc.addClass(outputs);
        return acc.serializeToAmplet().serializeToBytes();
    }

    @Override
    public String toJSON() {
        return null;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void addSig(String key, String sig) {
        keySigMap.get(key).sig = sig;
    }

    @Override
    public Amplet serializeToAmplet() {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();

        hpa.addElement(message);
        hpa.addElement(type.toString());

        HeadlessPrefixedAmplet ins = HeadlessPrefixedAmplet.create();
        for (Input i : inputs) {
            ins.addBytes(i.serializeToBytes());
        }
        HeadlessPrefixedAmplet outs = HeadlessPrefixedAmplet.create();
        for (Output o : outputs) {
            outs.addBytes(o.serializeToBytes());
        }
        hpa.addElement(ins);
        hpa.addElement(outs);
        HeadlessPrefixedAmplet map = HeadlessPrefixedAmplet.create();
        for (String key : keySigMap.keySet()) {

            map.addElement(key);
            map.addElement(keySigMap.get(key).toAmplet());

        }
        hpa.addElement(map);
        AC_SingleElement trans = AC_SingleElement.create(AmpIDs.ID_GID, hpa.serializeToBytes());
        return trans.serializeToAmplet();
    }

    public static NewTrans fromAmplet(Amplet amp) {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(amp.unpackGroup(AmpIDs.ID_GID).getElement(0));
        String message;
        String type;
        try {
            message = new String(hpa.getNextElement(), "UTF-8");
            type = new String(hpa.getNextElement(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        HeadlessPrefixedAmplet ins = HeadlessPrefixedAmplet.create(hpa.getNextElement());
        List<Input> inputs = new ArrayList<>();
        while (ins.hasNextElement()) {
            inputs.add(Input.fromBytes(ins.getNextElement()));
        }
        HeadlessPrefixedAmplet outs = HeadlessPrefixedAmplet.create(hpa.getNextElement());
        List<Output> outputs = new ArrayList<>();
        while (outs.hasNextElement()) {
            outputs.add(Output.fromBytes(outs.getNextElement()));
        }
        HeadlessPrefixedAmplet map = HeadlessPrefixedAmplet.create(hpa.getNextElement());
        Map<String, KeySigEntropyPair> keySigMap = new HashMap<>();
        while (map.hasNextElement()) {
            try {
                String key = new String(map.getNextElement(), "UTF-8");
                KeySigEntropyPair kesp = KeySigEntropyPair.fromAmplet(HeadlessPrefixedAmplet.create(map.getNextElement()));
                keySigMap.put(key, kesp);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        try {
            return new NewTrans(message, outputs, inputs, keySigMap, TransactionType.valueOf(type));
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
