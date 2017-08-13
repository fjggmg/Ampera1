package com.lifeform.main.transactions;

import com.lifeform.main.IKi;
import com.lifeform.main.data.files.StringFileHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bryan on 8/10/2017.
 */
public class AddressManager implements IAddMan {
    String entropy = "Entropy goes here, Please reset me to something else";
    String addFile = "addresses.origin";
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
        for(Address a:inactive) {
            if(a.encodeForChain().equals(address.encodeForChain()))
            addresses.add(address);
        }
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
    }

    @Override
    public void save() {
        StringFileHandler fh = new StringFileHandler(ki,addFile);
        fh.delete();
        if(main != null)
        fh.addLine(main.encodeForChain());
        if(!getActive().isEmpty())
        for(Address a:getActive())
        {
            if(!main.encodeForChain().equals(a.encodeForChain()))
            fh.addLine(a.encodeForChain());
        }

    }

    @Override
    public String getEntropyForAdd(Address a) {
        return entropyMap.get(a);
    }

    @Override
    public void setMainAdd(Address a) {
        main = a;
        save();
    }
}
