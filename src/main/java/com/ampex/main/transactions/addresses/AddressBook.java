package com.ampex.main.transactions.addresses;

import amp.HeadlessPrefixedAmplet;
import com.ampex.amperabase.IAddress;
import com.ampex.amperabase.IAddressBook;
import database.XodusAmpMap;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class AddressBook implements IAddressBook {

    private XodusAmpMap store;
    private Map<String, IAddress> book = new HashMap<>();
    public AddressBook()
    {
        store = new XodusAmpMap("addressBook");

        if(store.getBytes("book") != null) {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(store.getBytes("book"));
            while(hpa.hasNextElement())
            {
                book.put(new String(hpa.getNextElement(), Charset.forName("UTF-8")),Address.fromByteArray(hpa.getNextElement()));
            }
        }

    }
    @Override
    public void add(String s, IAddress iAddress) {
        book.put(s,iAddress);
    }

    @Override
    public void remove(String s) {
        book.remove(s);
    }

    @Override
    public void remove(IAddress iAddress) {
        for(Map.Entry<String,IAddress> entry:book.entrySet())
        {
            if(entry.getValue().encodeForChain().equals(iAddress.encodeForChain()))
            {
                book.remove(entry.getKey());
            }
        }
    }

    @Override
    public Map<String, IAddress> getBook() {
        return book;
    }

    @Override
    public void close() {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        for(Map.Entry<String,IAddress> entry:book.entrySet())
        {
            hpa.addBytes(entry.getKey().getBytes(Charset.forName("UTF-8")));
            hpa.addBytes(entry.getValue().toByteArray());
        }
        store.putBytes("book",hpa.serializeToBytes());
    }
}
