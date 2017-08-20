package com.lifeform.main.transactions;

import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.JSONManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by Bryan on 8/8/2017.
 */
public class Transaction implements ITrans{

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
    public Transaction(String message, int sigsRequired,Map<String,String> keySigMap,List<Output> outputs, List<Input> inputs,Map<String,String> entropyMap, List<String> keys)
    {
        Collections.sort(keys);
        this.keySigMap = keySigMap;
        if(keySigMap == null)
        {
            this.keySigMap = new HashMap<>();
        }
        this.outputs = outputs;
        this.inputs = inputs;
        this.sigsRequired = sigsRequired;
        this.message = message;
        this.entropyMap = entropyMap;
        this.keys = keys;
    }
    public Transaction(String message,int sigsRequired,List<Output> outputs,List<Input> inputs)
    {
        this.outputs = outputs;
        this.inputs = inputs;
        this.sigsRequired = sigsRequired;
        this.message = message;
    }


    List<String> keys;
    Map<String,String> entropyMap; //for keys
    Map<String,String> keySigMap = new HashMap<>();

    List<Output> outputs;
    List<Input> inputs;

    int sigsRequired;

    String message;

    @Override
    public String toJSON()
    {
        JSONObject jo = new JSONObject();
        jo.put("message",message);
        jo.put("sigsRequired",Integer.toString(sigsRequired));
        List<String> sInputs = new ArrayList<>();
        for(Input i:inputs)
        {
            sInputs.add(i.toJSON());
        }

        jo.put("inputs", JSONManager.parseListToJSON(sInputs).toJSONString());

        List<String> sOutputs = new ArrayList<>();
        for(Output o:outputs)
        {
            sOutputs.add(o.toJSON());
        }

        jo.put("outputs", JSONManager.parseListToJSON(sOutputs).toJSONString());

        jo.put("keySigMap",JSONManager.parseMapToJSON(keySigMap).toJSONString());
        jo.put("keys", JSONManager.parseListToJSON(keys).toJSONString());

        jo.put("entropyMap",JSONManager.parseMapToJSON(entropyMap).toJSONString());
        return jo.toJSONString();
    }

    @Override
    public void addSig(String key, String sig) {
        keySigMap.put(key,sig);
    }


    public static Transaction fromJSON(String JSON)
    {
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
            for(String s:entropyMap.keySet())
            {
                System.out.println("Address: " + s + " value " + entropyMap.get(s));
            }
            List<String> keys = JSONManager.parseJSONToList((String)jo.get("keys"));
            return new Transaction(message,sigsRequired,keySigMap,outputs,inputs,entropyMap,keys);

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
        String sKeys = "";
        for(String key:keys)
        {
            sKeys = sKeys + key;
        }
        Map<String,Integer> idMap = new HashMap<>();
        for(Input i:inputs)
        {
            if(idMap.keySet().contains(i.getID()))
            {
                if(idMap.get(i.getID()) == i.getIndex()) {
                    System.out.println("input already used");
                    return false;
                }
            }
            idMap.put(i.getID(),i.getIndex());
            if(entropyMap.get(i.getAddress().encodeForChain()) == null){
                System.out.println("Entropy for this address: " + i.getAddress().encodeForChain() +  " is null");
                return false;
            }
            if(!i.canSpend(sKeys,entropyMap.get(i.getAddress().encodeForChain()))) return false;
        }
        return true;
    }

    @Override
    public boolean verifyInputToOutput() {
        for(Token t:Token.values()) {
            BigInteger allInput = BigInteger.ZERO;
            for (Input i : inputs) {
                if(i.getToken().equals(t))
                allInput = allInput.add(i.getAmount());
            }

            BigInteger allOutput = BigInteger.ZERO;
            for (Output o : outputs) {
                if(o.getToken().equals(t))
                allOutput = allOutput.add(o.getAmount());
            }
            if (allInput.compareTo(allOutput) < 0) {
                return false;
            }
        }
        return true;
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
    public void makeChange(BigInteger fee,Address cAdd) {
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
        Output o = new Output(allInput.subtract(allOutput).subtract(fee),cAdd,Token.ORIGIN,outputs.size() + 1);
        outputs.add(o);

    }


    private void makeChangeSecondary(Address cAdd)
    {
        for(Token t:Token.values()) {
            if(t.equals(Token.ORIGIN)) continue;
            BigInteger allInput = BigInteger.ZERO;
            for (Input i : inputs) {
                if (i.getToken().equals(t))
                    allInput = allInput.add(i.getAmount());

            }
            BigInteger allOutput = BigInteger.ZERO;
            for (Output o : outputs) {
                if (o.getToken().equals(t))
                    allOutput = allOutput.add(o.getAmount());
            }

            Output o = new Output(allInput.subtract(allOutput), cAdd, t, outputs.size() + 1);
            outputs.add(o);
        }
    }

    @Override
    public boolean verifySigs() {
        int vCount = 0;
        for(String key:keySigMap.keySet())
        {
            if(EncryptionManager.verifySig(toSign(),keySigMap.get(key),key)) vCount++;
            if(vCount >= sigsRequired)
            {
                return true;
            }
        }
        return false;
    }
}
