package com.lifeform.main.transactions;

import com.lifeform.main.IKi;
import com.lifeform.main.data.files.StringFileHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Bryan on 8/10/2017.
 */
public class AddressManager implements IAddMan {
    static final String DEFAULT_ENTROPY = "Entropy goes here, Please reset me to something else";
    String entropy = "Entropy goes here, Please reset me to something else";
    String addFile = "addresses.origin";
    String addEntFile = "addresses.entropy";
    String addFolder = "addresses/";
    private IKi ki;
    private final int depth = 30;
    private List<IAddress> addresses = new ArrayList<>();
    private Map<IAddress, Integer> verifyCounter = new HashMap<>();
    private Map<IAddress, String> entropyMap = new ConcurrentHashMap<>();
    private IAddress main;
    private List<IAddress> inactive = new ArrayList<>();
    public AddressManager(IKi ki)
    {
        File f = new File(addFolder);
        f.mkdirs();
        this.ki = ki;
    }

    @Override
    public IAddress getNewAdd() {
        IAddress a = null;
        try {
            a = NewAdd.createNew(ki.getEncryptMan().getPublicKeyString(), entropy, AddressLength.SHA256, false);
        } catch (InvalidAddressException e) {
            e.printStackTrace();
            return null;
        }
        entropyMap.put(a,entropy);
        inactive.add(a);
        save();
        return a;
    }

    @Override
    public List<IAddress> getActive() {
        return addresses;
    }

    @Override
    public void receivedOn(IAddress address) {
        if(address.encodeForChain().equals(main.encodeForChain())) return;
        IAddress toRemove = null;
        for (IAddress a : inactive) {
            if (a.encodeForChain().equals(address.encodeForChain())) {
                addresses.add(address);
                toRemove = address;
            }
        }
        inactive.remove(toRemove);
    }

    @Override
    public void usedEntirely(IAddress address) {
            //possibly not needed
    }

    @Override
    public void verified(IAddress address) {
        verifyCounter.put(address,0);
    }

    @Override
    public void blockTick() {
        List<IAddress> toRemove = new ArrayList<>();
        for (IAddress key : verifyCounter.keySet())
        {
            verifyCounter.put(key,verifyCounter.get(key) + 1);
            if(verifyCounter.get(key) > depth)
            {
                toRemove.add(key);
            }
        }
        for (IAddress a : toRemove)
        {

            verifyCounter.remove(a);
            if(!a.encodeForChain().equals(main.encodeForChain()))
            addresses.remove(a);
        }

    }

    @Override
    public IAddress getMainAdd() {
        return main;
    }

    @Override
    public void load() {
        StringFileHandler fh = new StringFileHandler(ki,addFolder + addFile);
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
        StringFileHandler fh2 = new StringFileHandler(ki,addFolder + addEntFile);
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

        List<IAddress> toRemove = new ArrayList<>();
        for (IAddress add : entropyMap.keySet())
        {
            entropy = entropyMap.get(add);
            if(!add.encodeForChain().equals(getNewAdd().encodeForChain()))
            {
                ki.debug("Address loaded is not for the keys we have, deleting address");
                toRemove.add(add);
            }
        }
        for (IAddress add : toRemove)
        {
            if(main.encodeForChain().equals(add.encodeForChain()))
            {
                entropy = DEFAULT_ENTROPY;
                main = getNewAdd();
            }else{
                addresses.remove(add);
                entropyMap.remove(add);
            }
        }
    }

    @Override
    public void save() {
        StringFileHandler fh = new StringFileHandler(ki,addFolder + addFile);
        fh.delete();
        if(main != null)
        fh.addLine(main.encodeForChain());
        if(!getActive().isEmpty()) {
            for (IAddress a : getActive()) {
                if (!main.encodeForChain().equals(a.encodeForChain()))
                    fh.addLine(a.encodeForChain());
            }
            fh.save();
            JSONObject jo = new JSONObject();
            for (IAddress a : getActive()) {
                jo.put(a.encodeForChain(), getEntropyForAdd(a));
            }

            StringFileHandler fh2 = new StringFileHandler(ki, addFolder + addEntFile);
            fh2.delete();
            fh2.addLine(jo.toJSONString());
            fh2.save();
        }
    }

    @Override
    public String getEntropyForAdd(IAddress a) {

        for (IAddress add : entropyMap.keySet())
        {
            if(add.encodeForChain().equals(a.encodeForChain())) return entropyMap.get(add);
        }
        return entropyMap.get(a);
    }

    @Override
    public void setMainAdd(IAddress a) {
        main = a;
        inactive.remove(a);
        addresses.add(a);
        save();
    }

    @Override
    public IAddress createNew(String binOrKey, String entropy, String prefix, AddressLength l, boolean p2sh) {

        IAddress a;
        if (prefix != null && prefix.length() == 5)
            try {
                a = NewAdd.createNew(binOrKey, entropy, l, prefix, p2sh);
            } catch (InvalidAddressException e) {
                e.printStackTrace();
                return null;
            }
        else
            try {
                a = NewAdd.createNew(binOrKey, entropy, l, p2sh);
            } catch (InvalidAddressException e) {
                e.printStackTrace();
                return null;
            }

        entropyMap.put(a, entropy);
        addresses.add(a);
        save();
        return a;
    }

    @Override
    public List<IAddress> getAll() {
        List<IAddress> all = new ArrayList<>();
        all.addAll(inactive);
        all.addAll(addresses);
        all.add(main);
        return all;

    }

    @Override
    public void deleteAddress(IAddress address) {
        inactive.remove(address);
        addresses.remove(address);
        save();

    }
}
