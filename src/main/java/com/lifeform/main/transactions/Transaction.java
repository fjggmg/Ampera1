package com.lifeform.main.transactions;

import amp.Amplet;
import amp.classification.AmpClassCollection;
import amp.classification.classes.AC_ClassInstanceIDIsIndex;
import amp.classification.classes.AC_SingleElement;
import amp.group_primitives.UnpackedGroup;
import com.lifeform.main.IKi;
import com.lifeform.main.Ki;
import com.lifeform.main.blockchain.IChainMan;
import com.lifeform.main.data.AmpIDs;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.JSONManager;
import com.lifeform.main.data.Utils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by Bryan on 8/8/2017.
 */
public class Transaction implements ITrans {


    /**
     *
     * @param message
     * @param sigsRequired
     * @param keySigMap if this is null it will be initialized so you can add signatures to it
     * @param outputs
     * @param inputs
     * @param entropyMap
     * @param keys this will be reordered automatically, multisig wallets order keys by lowest hash of key first and so on
     */
    public Transaction(String message, int sigsRequired, Map<String, String> keySigMap, List<Output> outputs, List<Input> inputs, Map<String, String> entropyMap, List<String> keys, TransactionType type) throws InvalidTransactionException
    {
        this(message, sigsRequired, outputs, inputs, type);
        Collections.sort(keys);
        if (keySigMap != null) {
            this.keySigMap = keySigMap;

        }
        this.entropyMap = entropyMap;
        this.keys = keys;

    }


    public Transaction(String message, int sigsRequired, List<Output> outputs, List<Input> inputs, TransactionType type) throws InvalidTransactionException
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
    TransactionType type;
    List<String> keys;
    Map<String,String> entropyMap; //for keys
    Map<String,String> keySigMap = new HashMap<>();
    List<Output> outputs;
    List<Input> inputs;

    int sigsRequired;

    String message;

    @Deprecated
    @Override
    public String toJSON() {
        JSONObject jo = new JSONObject();
        jo.put("message",message);
        jo.put("sigsRequired",Integer.toString(sigsRequired));
        List<String> sInputs = new ArrayList<>();
        for(Input i:inputs) {
            sInputs.add(i.toJSON());
        }

        jo.put("inputs", JSONManager.parseListToJSON(sInputs).toJSONString());

        List<String> sOutputs = new ArrayList<>();
        for(Output o:outputs) {
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

    public static Transaction fromJSON(String JSON) throws InvalidTransactionException {
        try {
            JSONObject jo = (JSONObject) new JSONParser().parse(JSON);
            String message = (String) jo.get("message");
            int sigsRequired = Integer.parseInt((String) jo.get("sigsRequired"));
            List<Input> inputs = new ArrayList<>();
            for(String input:JSONManager.parseJSONToList((String)jo.get("inputs")))
            {
                inputs.add(Input.fromJSONString(input));
            }

            List<Output> outputs = new ArrayList<>();
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
     * same as "toJSON" but does not include key map to avoid chicken->egg
     * @return string to sign
     */
    @Override
    public String toSign()
    {
        JSONObject jo = new JSONObject();
        jo.put("message",message);
        jo.put("sigsRequired",sigsRequired);
        List<String> sInputs = new ArrayList<>();
        for(Input i:inputs)
        {
            sInputs.add(i.toJSON());
        }

        jo.put("inputs", JSONManager.parseListToJSON(sInputs));

        List<String> sOutputs = new ArrayList<>();
        for(Output o:outputs)
        {
            sOutputs.add(o.toJSON());
        }

        jo.put("outputs", JSONManager.parseListToJSON(sInputs));
        return jo.toJSONString();
    }

    @Override
    public byte[] toSignBytes() {
        AC_SingleElement msg = null;

        msg = AC_SingleElement.create(AmpIDs.MESSAGE_ID_GID, message);

        AC_SingleElement sRqd = AC_SingleElement.create(AmpIDs.SIGS_REQUIRED_GID, sigsRequired);
        AC_ClassInstanceIDIsIndex inputs = AC_ClassInstanceIDIsIndex.create(AmpIDs.INPUTS_CID, "Inputs");
        for (Input i : getInputs()) {
            inputs.addElement(i.serializeToAmplet());
        }
        AC_ClassInstanceIDIsIndex outputs = AC_ClassInstanceIDIsIndex.create(AmpIDs.OUTPUTS_CID, "Outputs");
        for (Output o : getOutputs()) {
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
    public List<Output> getOutputs() {
        return outputs;
    }

    @Override
    public List<Input> getInputs() {
        return inputs;
    }
    @Override
    public boolean verifyCanSpend()
    {
        List<String> ids = new ArrayList<>();
        for (Input i : inputs) {

            //Ki.getInstance().debug("Input information:: ID: " + i.getID() + " Index: " + i.getIndex() );
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
            for (Input i : inputs) {
                if(i.getToken().equals(t))
                allInput = allInput.add(i.getAmount());
                if(i.getAmount().compareTo(BigInteger.ZERO) < 0)
                {
                    return false;
                }
            }

            BigInteger allOutput = BigInteger.ZERO;
            for (Output o : outputs) {
                if(o.getToken().equals(t))
                allOutput = allOutput.add(o.getAmount());
                if(o.getAmount().compareTo(BigInteger.ZERO) < 0)
                {
                    //Ki.getInstance().debug("Zero or negative transaction");
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
    public boolean verifySpecial(IKi ki) {
        if (type.equals(TransactionType.STANDARD))
            return true;
        else if (type.equals(TransactionType.NEW_TRANS))
            return true;
        return false;
    }

    @Override
    public BigInteger getFee() {
        BigInteger allInput = BigInteger.ZERO;
        for(Input i: inputs)
        {
            if(i.getToken().equals(Token.ORIGIN))
                allInput = allInput.add(i.getAmount());

        }
        BigInteger allOutput = BigInteger.ZERO;
        for(Output o: outputs)
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
        for(Input i: inputs)
        {
            if(i.getToken().equals(Token.ORIGIN))
                allInput = allInput.add(i.getAmount());

        }
        BigInteger allOutput = BigInteger.ZERO;
        for(Output o: outputs)
        {
            if(o.getToken().equals(Token.ORIGIN))
                allOutput = allOutput.add(o.getAmount());
        }
        if(allInput.subtract(allOutput).compareTo(fee) <= 0)
        {
            //not enough left to make fee
            return;
        }
        Output o = new Output(allInput.subtract(allOutput).subtract(fee), cAdd, Token.ORIGIN, outputs.size() + 1, System.currentTimeMillis(), (byte) 2);
        outputs.add(o);

    }

    private void makeChangeSecondary(IAddress cAdd)
    {
        for(Token t:Token.values()) {
            if(t.equals(Token.ORIGIN)) continue;
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

            Output o = new Output(allInput.subtract(allOutput), cAdd, t, outputs.size() + 1, System.currentTimeMillis(), (byte) 2);
            outputs.add(o);
        }
    }

    @Override
    public boolean verifySigs() {
        if (sigsRequired < 1) return false;
        int vCount = 0;
        for(String key:keySigMap.keySet())
        {
            if (type.equals(TransactionType.STANDARD))
                if (EncryptionManager.verifySig(toSign(), keySigMap.get(key), key, KeyType.BRAINPOOLP512T1)) vCount++;
                else if (EncryptionManager.verifySig(toSignBytes(), Utils.fromBase64(keySigMap.get(key)), key, KeyType.BRAINPOOLP512T1))
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
        for (Input i : getInputs()) {
            inputs.addElement(i);
        }
        AC_ClassInstanceIDIsIndex outputs = AC_ClassInstanceIDIsIndex.create(AmpIDs.OUTPUTS_CID, "Outputs");
        for (Output o : getOutputs()) {
            outputs.addElement(o);
        }

        AC_ClassInstanceIDIsIndex keys = AC_ClassInstanceIDIsIndex.create(AmpIDs.KEYS_CID, "Keys");
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
        //System.out.println("Deserializing Transaction from amp1");
        TransactionType type = null;
        try {
            UnpackedGroup t = amp.unpackGroup(AmpIDs.TYPE_GID);

            type = TransactionType.valueOf(t.getElementAsString(0));

        } catch (Exception e) {
            //System.out.println("Transaction does not appear to be old format, sending to NewTrans system.");
            return NewTrans.fromAmplet(amp);
        }
        //System.out.println("Deserializing Transaction from amp2");
        if (type.equals(TransactionType.NEW_TRANS)) return NewTrans.fromAmplet(amp);
        UnpackedGroup eMap = amp.unpackGroup(AmpIDs.ENTROPY_MAP_GID);
        Map<String, String> entropyMap;
        //System.out.println("Deserializing Transaction from amp3");
        try {
            entropyMap = Utils.toObject(eMap.getElement(0));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        //System.out.println("Deserializing Transaction from amp4");
        UnpackedGroup kMap = amp.unpackGroup(AmpIDs.KEY_SIG_MAP_GID);
        Map<String, String> keyMap;
        try {
            keyMap = Utils.toObject(kMap.getElement(0));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        //System.out.println("Deserializing Transaction from amp5");
        UnpackedGroup msg = amp.unpackGroup(AmpIDs.MESSAGE_ID_GID);
        String message = "";
        if (msg != null)
            message = msg.getElementAsString(0);

        //System.out.println("Deserializing Transaction from amp6");
        UnpackedGroup sigsRqd = amp.unpackGroup(AmpIDs.SIGS_REQUIRED_GID);
        int sigsRequired = sigsRqd.getElementAsInt(0);
        List<UnpackedGroup> inputsAmp = amp.unpackClass(AmpIDs.INPUTS_CID);
        List<Input> inputs = new ArrayList<>();
        if (inputsAmp != null)
            for (UnpackedGroup p : inputsAmp) {
                inputs.add(Input.fromBytes(p.getElement(0)));

            }
        //System.out.println("Deserializing Transaction from amp7");
        List<UnpackedGroup> outputsAmp = amp.unpackClass(AmpIDs.OUTPUTS_CID);
        List<Output> outputs = new ArrayList<>();
        if (outputsAmp != null)
            for (UnpackedGroup p : outputsAmp) {
                outputs.add(Output.fromBytes(p.getElement(0)));

            }
        //System.out.println("Deserializing Transaction from amp8");
        List<UnpackedGroup> keysAmp = amp.unpackClass(AmpIDs.KEYS_CID);
        List<String> keys = new ArrayList<>();
        if (keysAmp != null)
            for (UnpackedGroup p : keysAmp) {
                keys.add(p.getElementAsString(0));
            }
        //System.out.println("Deserializing Transaction from amp9");

        return new Transaction(message, sigsRequired, keyMap, outputs, inputs, entropyMap, keys, type);
    }
}
