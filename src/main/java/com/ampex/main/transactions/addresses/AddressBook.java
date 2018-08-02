package com.ampex.main.transactions.addresses;

import amp.HeadlessPrefixedAmplet;
import com.ampex.amperabase.IAddress;
import com.ampex.amperabase.IAddressBook;
import database.XodusAmpMap;

import java.util.HashMap;
import java.util.Map;

public class AddressBook implements IAddressBook {

    private XodusAmpMap store;
    private Map<String, IAddMan> book = new HashMap<>();
    public AddressBook()
    {
        store = new XodusAmpMap("addressBook");

        if(store.getBytes("book") != null) {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(store.getBytes("book"));

        }

    }
    @Override
    public void add(String s, IAddress iAddress) {

    }

    @Override
    public void remove(String s) {

    }

    @Override
    public void remove(IAddress iAddress) {

    }

    @Override
    public Map<String, IAddress> getBook() {
        return null;
    }
}
