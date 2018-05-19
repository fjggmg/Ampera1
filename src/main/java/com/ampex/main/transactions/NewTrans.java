package com.ampex.main.transactions;

import amp.Amplet;
import amp.HeadlessPrefixedAmplet;
import amp.classification.AmpClassCollection;
import amp.classification.classes.AC_ClassInstanceIDIsIndex;
import amp.classification.classes.AC_SingleElement;
import com.ampex.amperabase.*;
import com.ampex.main.IKi;
import com.ampex.main.blockchain.IChainMan;
import com.ampex.main.data.AmpIDs;
import com.ampex.main.data.EncryptionManager;
import com.ampex.main.data.Utils;
import com.ampex.main.transactions.addresses.Address;
import com.ampex.main.transactions.addresses.InvalidAddressException;
import com.ampex.main.transactions.addresses.NewAdd;
import engine.binary.Binary;
import engine.data.DataElement;
import engine.data.WritableMemory;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewTrans implements ITrans {
    private List<IOutput> outputs;
    private List<IInput> inputs;
    private String message;
    private TransactionType type;
    private Map<String, IKSEP> keySigMap;

    public NewTrans(String message, List<IOutput> outputs, List<IInput> inputs, Map<String, IKSEP> keySigMap, TransactionType type) throws InvalidTransactionException {
        if (message.length() > 256) throw new InvalidTransactionException("Message too long");
        if (outputs.size() + inputs.size() > IChainMan.MAX_TXIOS)
            throw new InvalidTransactionException("Too many TXIOs");
        int i = 0;
        for (IOutput o : outputs) {
            if (o.getIndex() != i) throw new InvalidTransactionException("Bad Output index");
            i++;
        }
        for (Map.Entry<String, IKSEP> key : keySigMap.entrySet()) {
            if (key.getValue().getInputs() == null) throw new InvalidTransactionException("Null inputs in KSEP");
            if (key.getValue().getInputs().isEmpty()) throw new InvalidTransactionException("Empty inputs in KSEP");
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
            ID = com.ampex.main.data.Utils.toBase64(EncryptionManager.sha3256(serializeToAmplet().serializeToBytes()));
            //System.out.println("ID is: " + ID);
            return ID;
        }
    }

    @Override
    public List<IOutput> getOutputs() {
        return outputs;
    }

    @Override
    public List<IInput> getInputs() {
        return inputs;
    }

    @Override
    public boolean verifySigs() {
        for (Map.Entry<String, IKSEP> key : keySigMap.entrySet()) {
            if (!key.getValue().isP2sh()) {
                if (!EncryptionManager.verifySig(toSignBytes(), com.ampex.main.data.Utils.fromBase64(key.getValue().getSig()), key.getKey(), key.getValue().getKeyType()))
                    return false;
            }

        }
        return true;
    }

    public String getSig(String key) {
        return keySigMap.get(key).getSig();
    }

    @Override
    public String toSign() {
        return null;
    }

    @Override
    public boolean verifyCanSpend() {
        List<String> inputIDs = new ArrayList<>();
        Map<String, IInput> inputMap = new HashMap<>();
        for (IInput i : inputs) {
            if (inputIDs.contains(i.getID())) {
                System.out.println("dup input");
                return false;
            }
            inputIDs.add(i.getID());
            inputMap.put(i.getID(), i);
        }
        for (Map.Entry<String, IKSEP> key : keySigMap.entrySet()) {
            if (inputMap.get(key.getValue().getInputs().get(0)) == null) {
                System.out.println("input 0 null");
                return false;
            }
            String address = inputMap.get(key.getValue().getInputs().get(0)).getAddress().encodeForChain();
            IKSEP ksep = key.getValue();
            if (!inputMap.get(ksep.getInputs().get(0)).canSpend(key.getKey(), ksep.getEntropy(), ksep.getPrefix(), ksep.isP2sh(), ksep.getKeyType())) {
                System.out.println("address mismatch, Address expecting: ");
                System.out.println(inputMap.get(ksep.getInputs().get(0)).getAddress().encodeForChain());
                System.out.println("Received: ");
                System.out.println("Entropy: " + ksep.getEntropy());
                System.out.println("Key: " + key);
                try {
                    System.out.println(NewAdd.createNew(key.getKey(), ksep.getEntropy(), AddressLength.byIndicator(inputMap.get(ksep.getInputs().get(0)).getAddress().toByteArray()[1]), ksep.isP2sh(), inputMap.get(ksep.getInputs().get(0)).getAddress().getKeyType()).encodeForChain());
                } catch (InvalidAddressException e) {
                    try {
                        System.out.println(Address.createNew(key.getKey(), ksep.getEntropy()).encodeForChain());
                    } catch (Exception e1) {
                        System.out.println("This is not necessarily an error, this is probably why the transaction failed.");
                        e.printStackTrace();
                        e1.printStackTrace();
                    }
                }
                return false;
            }
            for (String input : key.getValue().getInputs()) {
                if (!inputMap.get(input).getAddress().encodeForChain().equals(address)) {
                    System.out.println("address mismatch");
                    return false;
                }
                inputIDs.remove(input);
            }
        }
        if (!inputIDs.isEmpty()) {
            System.out.println("IInputs not empty");
            return false;
        }
        return true;
    }

    @Override
    public boolean verifyInputToOutput() {
        if (inputs.size() < 1 && outputs.size() < 1) return false;
        for (Token t : Token.values()) {
            BigInteger allIInput = BigInteger.ZERO;
            for (IInput i : inputs) {
                if (i.getToken().equals(t))
                    allIInput = allIInput.add(i.getAmount());
                if (i.getAmount().compareTo(BigInteger.ZERO) < 0) {
                    System.out.println("0 or negative input on token: " + i.getToken());
                    return false;
                }
            }

            BigInteger allOutput = BigInteger.ZERO;
            for (IOutput o : outputs) {
                if (o.getToken().equals(t))
                    allOutput = allOutput.add(o.getAmount());
                if (o.getAmount().compareTo(BigInteger.ZERO) < 0) {
                    System.out.println("0 or negative output on token: " + o.getToken());
                    return false;
                }
            }
            //System.out.println("Allin/Allout for: " + t.name());
            //System.out.println("" + allIInput + "/" + allOutput);
            if (t.equals(Token.ORIGIN)) {
                if (allIInput.compareTo(allOutput) < 0) {

                    return false;
                }
            } else {
                if (allIInput.compareTo(allOutput) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean verifySpecial(IKi ki) {
        for (Map.Entry<String, IKSEP> key : keySigMap.entrySet()) {
            IKSEP ksep = key.getValue();
            if (ksep.isP2sh()) {
                IAddress execAdd = null;
                for (IInput i : inputs) {
                    if (ksep.getInputs().contains(i.getID()))
                        execAdd = i.getAddress();
                }
                if (execAdd == null) return false;
                try {
                    ArrayList<DataElement> result = ki.getBCE8().executeProgram(Binary.deserializeFromAmplet(Amplet.create(com.ampex.main.data.Utils.fromBase64(key.getKey()))), WritableMemory.deserializeFromBytes(Utils.fromBase64(ksep.getSig())), this, execAdd.toByteArray(), false);
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
        BigInteger allIInput = BigInteger.ZERO;
        for (IInput i : inputs) {
            if (i.getToken().equals(Token.ORIGIN))
                allIInput = allIInput.add(i.getAmount());

        }
        BigInteger allOutput = BigInteger.ZERO;
        for (IOutput o : outputs) {
            if (o.getToken().equals(Token.ORIGIN))
                allOutput = allOutput.add(o.getAmount());
        }
        return allIInput.subtract(allOutput);
    }

    @Override
    public void makeChange(BigInteger fee, IAddress cAdd) {
        makeChangeSecondary(cAdd);
        BigInteger allIInput = BigInteger.ZERO;
        for (IInput i : inputs) {
            if (i.getToken().equals(Token.ORIGIN))
                allIInput = allIInput.add(i.getAmount());

        }
        BigInteger allOutput = BigInteger.ZERO;
        for (IOutput o : outputs) {
            if (o.getToken().equals(Token.ORIGIN))
                allOutput = allOutput.add(o.getAmount());
        }
        if (allIInput.subtract(allOutput).compareTo(fee) <= 0) {
            //not enough left to make fee
            return;
        }
        Output o = new Output(allIInput.subtract(allOutput).subtract(fee), cAdd, Token.ORIGIN, outputs.size(), System.currentTimeMillis(), Output.VERSION);
        outputs.add(o);

    }

    private void makeChangeSecondary(IAddress cAdd) {
        for (Token t : Token.values()) {
            if (t.equals(Token.ORIGIN)) continue;
            BigInteger allIInput = BigInteger.ZERO;
            boolean hasToken = false;
            for (IInput i : inputs) {
                if (i.getToken().equals(t)) {
                    allIInput = allIInput.add(i.getAmount());
                    hasToken = true;
                }

            }
            if (!hasToken) continue;
            BigInteger allOutput = BigInteger.ZERO;
            for (IOutput o : outputs) {
                if (o.getToken().equals(t))
                    allOutput = allOutput.add(o.getAmount());
            }

            Output o = new Output(allIInput.subtract(allOutput), cAdd, t, outputs.size(), System.currentTimeMillis(), Output.VERSION);
            outputs.add(o);
        }
    }

    @Override
    public byte[] toSignBytes() {
        AC_SingleElement msg = null;
        if (message != null && !message.isEmpty())
            msg = AC_SingleElement.create(AmpIDs.MESSAGE_ID_GID, message);

        AC_ClassInstanceIDIsIndex inputs = AC_ClassInstanceIDIsIndex.create(AmpIDs.INPUTS_CID, "IInputs");
        for (IInput i : getInputs()) {
            inputs.addElement(i.serializeToAmplet());
        }
        AC_ClassInstanceIDIsIndex outputs = AC_ClassInstanceIDIsIndex.create(AmpIDs.OUTPUTS_CID, "Outputs");
        for (IOutput o : getOutputs()) {
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
        keySigMap.get(key).setSig(sig);
    }

    @Override
    public Amplet serializeToAmplet() {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();

        hpa.addElement(message);
        hpa.addElement(type.toString());

        HeadlessPrefixedAmplet ins = HeadlessPrefixedAmplet.create();
        for (IInput i : inputs) {
            ins.addBytes(i.serializeToBytes());
        }
        HeadlessPrefixedAmplet outs = HeadlessPrefixedAmplet.create();
        for (IOutput o : outputs) {
            outs.addBytes(o.serializeToBytes());
        }
        hpa.addElement(ins);
        hpa.addElement(outs);
        HeadlessPrefixedAmplet map = HeadlessPrefixedAmplet.create();
        for (Map.Entry<String, IKSEP> key : keySigMap.entrySet()) {

            map.addElement(key.getKey());
            map.addElement(key.getValue().toAmplet());

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
        List<IInput> inputs = new ArrayList<>();
        while (ins.hasNextElement()) {
            inputs.add(Input.fromBytes(ins.getNextElement()));
        }
        HeadlessPrefixedAmplet outs = HeadlessPrefixedAmplet.create(hpa.getNextElement());
        List<IOutput> outputs = new ArrayList<>();
        while (outs.hasNextElement()) {
            outputs.add(Output.fromBytes(outs.getNextElement()));
        }
        HeadlessPrefixedAmplet map = HeadlessPrefixedAmplet.create(hpa.getNextElement());
        Map<String, IKSEP> keySigMap = new HashMap<>();
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
