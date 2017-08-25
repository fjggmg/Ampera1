package com.lifeform.main.transactions;

import com.lifeform.main.IKi;
import com.lifeform.main.data.files.StringFileHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

/**
 * Created by Bryan on 8/10/2017.
 */
public class AddressManager implements IAddMan {
    String entropy = "Entropy goes here, Please reset me to something else";
    String addFile = "addresses.origin";
    String addEntFile = "addresses.entropy";
    private IKi ki;
    private final int depth = 30;
    private List<Address> addresses = new ArrayList<>();
    private Map<Address,Integer> verifyCounter = new HashMap<>();
    private Map<Address,String> entropyMap = new HashMap<>();
    private Address main;
    private List<Address> inactive = new ArrayList<>();
    public AddressManager(IKi ki)
    {
        this.ki = ki;
    }

    @Override
    public Address getNewAdd() {
        Address a = Address.createNew(ki.getEncryptMan().getPublicKeyString(),entropy);
        entropyMap.put(a,entropy);
        inactive.add(a);
        save();
        return a;
    }

    @Override
    public List<Address> getActive() {
        return addresses;
    }

    @Override
    public void receivedOn(Address address) {
        Address toRemove = null;
        for(Address a:inactive) {
            if (a.encodeForChain().equals(address.encodeForChain())) {
                addresses.add(address);
                toRemove = address;
            }
        }
        inactive.remove(toRemove);
    }

    @Override
    public void usedEntirely(Address address) {
            //possibly not needed
    }

    @Override
    public void verified(Address address) {
        verifyCounter.put(address,0);
    }

    @Override
    public void blockTick() {
        List<Address> toRemove = new ArrayList<>();
        for(Address key:verifyCounter.keySet())
        {
            verifyCounter.put(key,verifyCounter.get(key) + 1);
            if(verifyCounter.get(key) > depth)
            {
                toRemove.add(key);
            }
        }
        for(Address a:toRemove)
        {

            verifyCounter.remove(a);
            if(!a.encodeForChain().equals(main.encodeForChain()))
            addresses.remove(a);
        }

    }

    @Override
    public Address getMainAdd() {
        return main;
    }

    @Override
    public void load() {
        StringFileHandler fh = new StringFileHandler(ki,addFile);
        if(!(fh.getLines().size() == 0))
        {
            main = Address.decodeFromChain(fh.getLine(0));
            addresses.add(main);
            for(String s:fh.getLines())
            {
                if(!main.encodeForChain().equals(s))
                {
                    addresses.add(Address.decodeFromChain(s));
                }
            }
        }
        StringFileHandler fh2 = new StringFileHandler(ki,addEntFile);
        if(fh2.getLines().size() != 0) {
            try {
                JSONObject jo = (JSONObject) new JSONParser().parse(fh2.getLine(0));
                for (String add : (Set<String>) jo.keySet()) {
                    //ki.getMainLog().info("Shit found in the entropy file. it matches this: " + add + " " + jo.get(add));
                    entropyMap.put(Address.decodeFromChain(add), (String) jo.get(add));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void save() {
        StringFileHandler fh = new StringFileHandler(ki,addFile);
        fh.delete();
        if(main != null)
        fh.addLine(main.encodeForChain());
        if(!getActive().isEmpty()) {
            for (Address a : getActive()) {
                if (!main.encodeForChain().equals(a.encodeForChain()))
                    fh.addLine(a.encodeForChain());
            }
            fh.save();
            JSONObject jo = new JSONObject();
            for (Address a : getActive()) {
                jo.put(a.encodeForChain(), getEntropyForAdd(a));
            }

            StringFileHandler fh2 = new StringFileHandler(ki, addEntFile);
            fh2.delete();
            fh2.addLine(jo.toJSONString());
            fh2.save();
        }
    }

    @Override
    public String getEntropyForAdd(Address a) {

        for(Address add:entropyMap.keySet())
        {
            if(add.encodeForChain().equals(a.encodeForChain())) return entropyMap.get(add);
        }
        return entropyMap.get(a);
    }

    @Override
    public void setMainAdd(Address a) {
        main = a;
        inactive.remove(a);
        addresses.add(a);
        save();
    }
}
