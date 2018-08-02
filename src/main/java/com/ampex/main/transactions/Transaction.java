package com.ampex.main.transactions;

import amp.Amplet;
import amp.classification.AmpClassCollection;
import amp.classification.classes.AC_ClassInstanceIDIsIndex;
import amp.classification.classes.AC_SingleElement;
import amp.group_primitives.UnpackedGroup;
import com.ampex.amperabase.*;
import com.ampex.main.Ki;
import com.ampex.main.blockchain.IChainMan;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.data.utils.JSONManager;
import com.ampex.main.data.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by Bryan on 8/8/2017.
 * Old transaction system. DO NOT USE! THESE TRANSACTIONS WILL FAIL TO VERIFY AFTER A TBD DATE!
 */
public class Transaction implements ITrans {

    /**
     * OLD TRANSACTION SYSTEM! DO NOT USE
     * @param message message on transaction
     * @param sigsRequired required number of signatures
     * @param keySigMap if this is null it will be initialized so you can add signatures to it
     * @param outputs list of outputs
     * @param inputs list of inputs
     * @param entropyMap map of key to entropy for addresses
     * @param keys this will be reordered automatically, multisig wallets order keys by lowest hash of key first and so on
     * @param type Type of transaction, always use standard if reconstructing old transactions here
     * @throws InvalidTransactionException if parameters provided do not create a valid transaction
     */
    @Deprecated
    public Transaction(String message, int sigsRequired, Map<String, String> keySigMap, List<IOutput> outputs, List<IInput> inputs, Map<String, String> entropyMap, List<String> keys, TransactionType type) throws InvalidTransactionException
    {
        this(message, sigsRequired, outputs, inputs, type);
        Collections.sort(keys);
        if (keySigMap != null) {
            this.keySigMap = keySigMap;

        }
        this.entropyMap = entropyMap;
        this.keys = keys;
    }

    public Transaction(String message, int sigsRequired, List<IOutput> outputs, List<IInput> inputs, TransactionType type) throws InvalidTransactionException
    {
        if (type == TransactionType.NEW_TRANS)
            throw new InvalidTransactionException("NewTrans type used with Transaction object, use NewTrans object instead");
        if (message.length() > 256) throw new InvalidTransactionException("Message too long");
        if (outputs.size() + inputs.size() > IChainMan.MAX_TXIOS)
            throw new InvalidTransactionException("Too many TXIOs");
        //if(sigsRequired < 1) throw new InvalidTransactionException("Sigs required < 1"); this needs to happen but it breaks coinbase right now
        this.outputs = outputs;
        this.inputs = inputs;
        this.sigsRequired = sigsRequired;
        this.message = message;
        this.type = type;
    }

    private TransactionType type;
    private List<String> keys;
    private Map<String, String> entropyMap; //for keys
    private Map<String, String> keySigMap = new HashMap<>();
    private List<IOutput> outputs;
    private List<IInput> inputs;
    private int sigsRequired;
    private String message;

    @Deprecated
    @Override
    public String toJSON() {
        JSONObject jo = new JSONObject();
        jo.put("message",message);
        jo.put("sigsRequired",Integer.toString(sigsRequired));
        List<String> sInputs = new ArrayList<>();
        for (IInput i : inputs) {
            sInputs.add(i.toJSON());
        }
        jo.put("inputs", JSONManager.parseListToJSON(sInputs).toJSONString());
        List<String> sOutputs = new ArrayList<>();
        for (IOutput o : outputs) {
            sOutputs.add(o.toJSON());
        }

        jo.put("outputs", JSONManager.parseListToJSON(sOutputs).toJSONString());

        jo.put("keySigMap",JSONManager.parseMapToJSON(keySigMap).toJSONString());
        jo.put("keys", JSONManager.parseListToJSON(keys).toJSONString());

        jo.put("entropyMap",JSONManager.parseMapToJSON(entropyMap).toJSONString());
        if (!type.equals(TransactionType.STANDARD))
            jo.put("type", type.toString());
        return jo.toJSONString();
    }

    @Override
    public void addSig(String key, String sig) {
        keySigMap.put(key,sig);
    }

    @Override
    public String getSig(String key) {
        return keySigMap.get(key);
    }

    public static Transaction fromJSON(String JSON) throws InvalidTransactionException {
        try {
            JSONObject jo = (JSONObject) new JSONParser().parse(JSON);
            String message = (String) jo.get("message");
            int sigsRequired = Integer.parseInt((String) jo.get("sigsRequired"));
            List<IInput> inputs = new ArrayList<>();
            for(String input:JSONManager.parseJSONToList((String)jo.get("inputs")))
            {
                inputs.add(Input.fromJSONString(input));
            }

            List<IOutput> outputs = new ArrayList<>();
            for(String output:JSONManager.parseJSONToList((String)jo.get("outputs")))
            {
                outputs.add(Output.fromJSON(output));
            }

            Map<String,String> keySigMap = JSONManager.parseJSONtoMap((String)jo.get("keySigMap"));
            //Map<String,String> eMap = JSONManager.parseJSONtoMap((String)jo.get("entropyMap"));
            Map<String,String> entropyMap = JSONManager.parseJSONtoMap((String)jo.get("entropyMap"));

            List<String> keys = JSONManager.parseJSONToList((String)jo.get("keys"));
            TransactionType type = TransactionType.STANDARD;
            if (jo.get("type") != null) {
                type = TransactionType.valueOf((String) jo.get("type"));
            }
            return new Transaction(message, sigsRequired, keySigMap, outputs, inputs, entropyMap, keys, type);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public String getID() {
        return EncryptionManager.sha256(toJSON());
    }

    /**
     * WARNING!!!! THIS METHOD INCLUDES A SECURITY ISSUE!!! THE NEW TRANSACTION SYSTEM DOES NOT USE THIS SORT OF SIGNING
     * ANYMORE AND SHOULD BE USED INSTEAD. THIS SYSTEM WILL BE COMPLETELY LOCKED OUT SOON. IT IS LEFT AS IS TO MAINTAIN
     * COMPATIBILITY WITH OLD TRANSACTIONS
     * same as "toJSON" but does not include key map to avoid chicken-egg
     * @return string to sign
     */
    @Deprecated
    @Override
    public String toSign()
    {
        JSONObject jo = new JSONObject();
        jo.put("message",message);
        jo.put("sigsRequired",sigsRequired);
        List<String> sInputs = new ArrayList<>();
        for (IInput i : inputs)
        {
            sInputs.add(i.toJSON());
        }

        jo.put("inputs", JSONManager.parseListToJSON(sInputs));
        /*
        List<String> sOutputs = new ArrayList<>();
        for(Output o:outputs)
        {
            sOutputs.add(o.toJSON());
        }
         */
        //Next line is the security flaw in this design. Hoping to remove this old code with Project UB, experimental feature possibly implemented in the future
        jo.put("outputs", JSONManager.parseListToJSON(sInputs));
        return jo.toJSONString();
    }

    @Override
    public byte[] toSignBytes() {
        AC_SingleElement msg = null;

        msg = AC_SingleElement.create(AmpIDs.MESSAGE_ID_GID, message);

        AC_SingleElement sRqd = AC_SingleElement.create(AmpIDs.SIGS_REQUIRED_GID, sigsRequired);
        AC_ClassInstanceIDIsIndex inputs = AC_ClassInstanceIDIsIndex.create(AmpIDs.INPUTS_CID, "Inputs");
        for (IInput i : getInputs()) {
            inputs.addElement(i.serializeToAmplet());
        }
        AC_ClassInstanceIDIsIndex outputs = AC_ClassInstanceIDIsIndex.create(AmpIDs.OUTPUTS_CID, "Outputs");
        for (IOutput o : getOutputs()) {
            outputs.addElement(o);
        }
        AmpClassCollection acc = new AmpClassCollection();
        acc.addClass(msg);
        acc.addClass(sRqd);
        acc.addClass(inputs);
        acc.addClass(outputs);
        return acc.serializeToAmplet().serializeToBytes();
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
    public boolean verifyCanSpend()
    {
        List<String> ids = new ArrayList<>();
        for (IInput i : inputs) {
            if (ids.contains(i.getID())) return false;
            ids.add(i.getID());
            if (entropyMap.get(i.getAddress().encodeForChain()) == null) {
                Ki.getInstance().debug("Entropy for this address: " + i.getAddress().encodeForChain() + " is null");
                return false;
            }
            if (!i.canSpend(keys.get(0), entropyMap.get(i.getAddress().encodeForChain()), null, i.getAddress().isP2SH(), i.getAddress().getKeyType()))
                return false;
        }
        return true;

    }

    @Override
    public boolean verifyInputToOutput() {
        if (inputs.size() < 1 && outputs.size() < 1) return false;
        for(Token t:Token.values()) {
            BigInteger allInput = BigInteger.ZERO;
            for (IInput i : inputs) {
                if(i.getToken().equals(t))
                allInput = allInput.add(i.getAmount());
                if(i.getAmount().compareTo(BigInteger.ZERO) < 0)
                {
                    return false;
                }
            }

            BigInteger allOutput = BigInteger.ZERO;
            for (IOutput o : outputs) {
                if(o.getToken().equals(t))
                allOutput = allOutput.add(o.getAmount());
                if(o.getAmount().compareTo(BigInteger.ZERO) < 0)
                {
                    return false;
                }
            }
            if (allInput.compareTo(allOutput) < 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean verifySpecial(IKiAPI ki) {
        if (type.equals(TransactionType.STANDARD))
            return true;
        else if (type.equals(TransactionType.NEW_TRANS))
            return true;
        return false;
    }

    @Override
    public BigInteger getFee() {
        BigInteger allInput = BigInteger.ZERO;
        for (IInput i : inputs)
        {
            if(i.getToken().equals(Token.ORIGIN))
                allInput = allInput.add(i.getAmount());

        }
        BigInteger allOutput = BigInteger.ZERO;
        for (IOutput o : outputs)
        {
            if(o.getToken().equals(Token.ORIGIN))
                allOutput = allOutput.add(o.getAmount());
        }
        return allInput.subtract(allOutput);
    }

    @Override
    public void makeChange(BigInteger fee, IAddress cAdd) {
        makeChangeSecondary(cAdd);
        BigInteger allInput = BigInteger.ZERO;
        for (IInput i : inputs)
        {
            if(i.getToken().equals(Token.ORIGIN))
                allInput = allInput.add(i.getAmount());

        }
        BigInteger allOutput = BigInteger.ZERO;
        for (IOutput o : outputs)
        {
            if(o.getToken().equals(Token.ORIGIN))
                allOutput = allOutput.add(o.getAmount());
        }
        if(allInput.subtract(allOutput).compareTo(fee) <= 0)
        {
            //not enough left to make fee
            return;
        }
        Output o = new Output(allInput.subtract(allOutput).subtract(fee), cAdd, Token.ORIGIN, outputs.size() + 1, System.currentTimeMillis(), Output.VERSION);
        outputs.add(o);

    }

    private void makeChangeSecondary(IAddress cAdd)
    {
        for(Token t:Token.values()) {
            if(t.equals(Token.ORIGIN)) continue;
            BigInteger allInput = BigInteger.ZERO;
            boolean hasToken = false;
            for (IInput i : inputs) {
                if (i.getToken().equals(t)) {
                    allInput = allInput.add(i.getAmount());
                    hasToken = true;
                }

            }
            if (!hasToken) continue;
            BigInteger allOutput = BigInteger.ZERO;
            for (IOutput o : outputs) {
                if (o.getToken().equals(t))
                    allOutput = allOutput.add(o.getAmount());
            }

            Output o = new Output(allInput.subtract(allOutput), cAdd, t, outputs.size() + 1, System.currentTimeMillis(), Output.VERSION);
            outputs.add(o);
        }
    }

    @Override
    public boolean verifySigs() {
        if (sigsRequired < 1) return false;
        int vCount = 0;
        for (Map.Entry<String, String> key : keySigMap.entrySet())
        {
            if (type.equals(TransactionType.STANDARD))
                if (EncryptionManager.verifySig(toSign(), key.getValue(), key.getKey(), KeyType.BRAINPOOLP512T1))
                    vCount++;
                else if (EncryptionManager.verifySig(toSignBytes(), Utils.fromBase64(key.getValue()), key.getKey(), KeyType.BRAINPOOLP512T1))
                    vCount++;
            if(vCount >= sigsRequired)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Amplet serializeToAmplet() {
        AC_SingleElement message = null;
        if (this.message != null && !this.message.isEmpty())

            message = AC_SingleElement.create(AmpIDs.MESSAGE_ID_GID, this.message);

        AC_SingleElement sigsRequiredGID = AC_SingleElement.create(AmpIDs.SIGS_REQUIRED_GID, this.sigsRequired);
        AC_ClassInstanceIDIsIndex inputs = AC_ClassInstanceIDIsIndex.create(AmpIDs.INPUTS_CID, "Inputs");
        for (IInput i : getInputs()) {
            inputs.addElement(i.serializeToBytes());
        }
        AC_ClassInstanceIDIsIndex outputs = AC_ClassInstanceIDIsIndex.create(AmpIDs.OUTPUTS_CID, "Outputs");
        for (IOutput o : getOutputs()) {
            outputs.addElement(o);
        }

        AC_ClassInstanceIDIsIndex keys = AC_ClassInstanceIDIsIndex.create(AmpIDs.KEYS_CID, "Keys");
        if (this.keys != null && !this.keys.isEmpty())
            for (String key : this.keys) {

                keys.addElement(key);
            }

        AC_SingleElement type = null;

        type = AC_SingleElement.create(AmpIDs.TYPE_GID, this.type.toString());

        AC_SingleElement keySigMap;
        try {
            keySigMap = AC_SingleElement.create(AmpIDs.KEY_SIG_MAP_GID, Utils.mapToByteArray(this.keySigMap));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        AC_SingleElement entropyMap;
        try {
            entropyMap = AC_SingleElement.create(AmpIDs.ENTROPY_MAP_GID, Utils.mapToByteArray(this.entropyMap));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        AmpClassCollection amp = new AmpClassCollection();
        amp.addClass(entropyMap);
        amp.addClass(keySigMap);
        if (message != null)
            amp.addClass(message);
        amp.addClass(sigsRequiredGID);
        if (this.inputs != null && !this.inputs.isEmpty())
            amp.addClass(inputs);
        if (this.outputs != null && !this.outputs.isEmpty())
            amp.addClass(outputs);
        if (this.keys != null && !this.keys.isEmpty())
            amp.addClass(keys);
        amp.addClass(type);
        return amp.serializeToAmplet();
    }

    public static ITrans fromAmplet(Amplet amp) throws InvalidTransactionException {
        TransactionType type = null;
        try {
            UnpackedGroup t = amp.unpackGroup(AmpIDs.TYPE_GID);

            type = TransactionType.valueOf(t.getElementAsString(0));

        } catch (Exception e) {
            return NewTrans.fromAmplet(amp);
        }
        if (type.equals(TransactionType.NEW_TRANS)) return NewTrans.fromAmplet(amp);
        UnpackedGroup eMap = amp.unpackGroup(AmpIDs.ENTROPY_MAP_GID);
        Map<String, String> entropyMap;
        try {
            entropyMap = Utils.toObject(eMap.getElement(0));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        UnpackedGroup kMap = amp.unpackGroup(AmpIDs.KEY_SIG_MAP_GID);
        Map<String, String> keyMap;
        try {
            keyMap = Utils.toObject(kMap.getElement(0));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        UnpackedGroup msg = amp.unpackGroup(AmpIDs.MESSAGE_ID_GID);
        String message = "";
        if (msg != null)
            message = msg.getElementAsString(0);

        UnpackedGroup sigsRqd = amp.unpackGroup(AmpIDs.SIGS_REQUIRED_GID);
        int sigsRequired = sigsRqd.getElementAsInt(0);
        List<UnpackedGroup> inputsAmp = amp.unpackClass(AmpIDs.INPUTS_CID);
        List<IInput> inputs = new ArrayList<>();
        if (inputsAmp != null)
            for (UnpackedGroup p : inputsAmp) {
                inputs.add(Input.fromBytes(p.getElement(0)));

            }
        List<UnpackedGroup> outputsAmp = amp.unpackClass(AmpIDs.OUTPUTS_CID);
        List<IOutput> outputs = new ArrayList<>();
        if (outputsAmp != null)
            for (UnpackedGroup p : outputsAmp) {
                outputs.add(Output.fromBytes(p.getElement(0)));

            }
        List<UnpackedGroup> keysAmp = amp.unpackClass(AmpIDs.KEYS_CID);
        List<String> keys = new ArrayList<>();
        if (keysAmp != null)
            for (UnpackedGroup p : keysAmp) {
                keys.add(p.getElementAsString(0));
            }

        return new Transaction(message, sigsRequired, keyMap, outputs, inputs, entropyMap, keys, type);
    }
}
