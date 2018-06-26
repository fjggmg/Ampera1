package com.ampex.main.transactions.addresses;

import amp.HeadlessPrefixedAmplet;
import com.ampex.amperabase.AddressLength;
import com.ampex.amperabase.IAddress;
import com.ampex.amperabase.KeyType;
import com.ampex.main.IKi;
import com.ampex.main.data.files.StringFileHandler;
import database.XodusAmpMap;
import engine.binary.IBinary;
import engine.binary.on_ice.Binary;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Bryan on 8/10/2017.
 */
public class AddressManager implements IAddMan {
    private static final String DEFAULT_ENTROPY = "Entropy goes here, Please reset me to something else";
    private String entropy = "Entropy goes here, Please reset me to something else";
    private String addFile = "addresses.origin";
    private String addEntFile = "addresses.entropy";
    private String addFolder = "addresses/";
    private IKi ki;
    private List<IAddress> addresses = new ArrayList<>();
    private Map<String, String> entropyMap = new ConcurrentHashMap<>();
    private IAddress main;
    private List<IAddress> inactive = new ArrayList<>();
    private XodusAmpMap addressMap = new XodusAmpMap("ampAdds");
    private XodusAmpMap addressBinMap = new XodusAmpMap("ampBins");
    public AddressManager(IKi ki) {
        this.ki = ki;
    }

    /**
     * This returns a 256 bit address, will probably let you select length soon
     *
     * @param keyType type of key to use
     * @return newly formed address with set entropy and length of 256 bits
     */
    @Override
    public IAddress getNewAdd(KeyType keyType, boolean save) {
        IAddress a = null;
        try {
            a = NewAdd.createNew(ki.getEncryptMan().getPublicKeyString(keyType), entropy, AddressLength.SHA256, false, keyType);
        } catch (InvalidAddressException e) {
            e.printStackTrace();
            return null;
        }
        entropyMap.put(a.encodeForChain(), entropy);
        inactive.add(a);
        if (save) {
            save();
        }
        return a;
    }

    private IAddress getNewAddOld() {
        IAddress a = null;
        try {
            a = Address.createNew(ki.getEncryptMan().getPublicKeyString(KeyType.BRAINPOOLP512T1), entropy);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return a;
    }

    @Override
    public void blockTick() {
    }

    @Override
    public IAddress getMainAdd() {
        return main;
    }

    @Override
    public void load() {
        if (addressMap.getBytes("firstRun") == null && new File(addFolder + addFile).exists()) {
            try {
                StringFileHandler fh = new StringFileHandler(ki, addFolder + addFile);
                if (!(fh.getLines().size() == 0)) {
                    main = Address.decodeFromChain(fh.getLine(0));
                    addresses.add(main);
                    for (String s : fh.getLines()) {
                        if (!main.encodeForChain().equals(s)) {
                            addresses.add(Address.decodeFromChain(s));
                        }
                    }
                }
                StringFileHandler fh2 = new StringFileHandler(ki, addFolder + addEntFile);
                if (fh2.getLines().size() != 0) {
                    try {
                        JSONObject jo = (JSONObject) new JSONParser().parse(fh2.getLine(0));
                        for (Map.Entry<String, String> add : (Set<Map.Entry<String, String>>) jo.entrySet()) {
                            entropyMap.put(add.getKey(), add.getValue());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                List<IAddress> toRemove = new ArrayList<>();
                for (String a : entropyMap.keySet()) {
                    IAddress add = Address.decodeFromChain(a);
                    entropy = getEntropyForAdd(add);
                    if (add.getKeyType() != null) {
                        if (!add.encodeForChain().equals(getNewAddOld().encodeForChain())) {
                            ki.debug("Address loaded is not for the keys we have, deleting address");
                            toRemove.add(add);
                        }
                    } else {
                        toRemove.add(add);
                    }
                }
                for (IAddress add : toRemove) {
                    if (main.encodeForChain().equals(add.encodeForChain())) {
                        entropy = DEFAULT_ENTROPY;
                        main = getNewAdd(main.getKeyType(), false);
                    } else {
                        addresses.remove(add);
                        entropyMap.remove(add.encodeForChain());
                    }
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                ki.getMainLog().warn("Unable to load from old address system, if you did not have any addresses before 18.0, this is not a problem, if you did, report this.", e);
            }
        } else {
            if (addressMap.getBytes("addresses") != null) {
                HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(addressMap.getBytes("addresses"));
                main = Address.fromByteArray(hpa.getNextElement());
                addresses.add(main);
                while (hpa.hasNextElement()) {
                    addresses.add(Address.fromByteArray(hpa.getNextElement()));
                }
                for (IAddress a : addresses) {
                    try {
                        if (addressMap.getBytes(a.encodeForChain()) == null) {
                            ki.getMainLog().warn("Null entropy for address: " + a.encodeForChain() + " address database possibly corrupted");
                            continue;
                        }
                        entropyMap.put(a.encodeForChain(), new String(addressMap.getBytes(a.encodeForChain()), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public synchronized void save() {
        //safety check?
        if (main == null) return;
        //addressMap.clearSafe(); //shouldn't be needed, we're overwriting
        addressMap.putBytes("firstRun", new byte[]{1});
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        List<IAddress> toRemove = new ArrayList<>();

        for (IAddress address : addresses) {
            //this will never fire
            if (getEntropyForAdd(address) == null) {
                ki.getMainLog().error("Entropy for address: " + address.encodeForChain() + " is null");
                toRemove.add(address);
            }
        }
        addresses.removeAll(toRemove);
        hpa.addBytes(main.toByteArray());
        try {
            addressMap.putBytes(main.encodeForChain(), getEntropyForAdd(main).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            ki.getMainLog().fatal("Unable to save entropy for main address: " + main.encodeForChain(), e);
        }
        for (IAddress address : addresses) {
            if (!address.encodeForChain().equals(main.encodeForChain())) {
                hpa.addBytes(address.toByteArray());
                try {
                    addressMap.putBytes(address.encodeForChain(), getEntropyForAdd(address).getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    ki.getMainLog().fatal("Unable to save entropy for address: " + address.encodeForChain(), e);
                }
            }
        }
        addressMap.putBytes("addresses", hpa.serializeToBytes());

    }

    @Override
    public String getEntropyForAdd(IAddress a) {

        if (!entropyMap.containsKey(a.encodeForChain())) return "";
        return entropyMap.get(a.encodeForChain());
    }

    @Override
    public void setMainAdd(IAddress a) {
        main = a;
        inactive.remove(a);
        addresses.add(a);
        save();
    }

    @Override
    public IAddress createNew(String binOrKey, String entropy, String prefix, AddressLength l, boolean p2sh, KeyType keyType) {

        IAddress a;
        if (prefix != null && prefix.length() == 5)
            try {
                a = NewAdd.createNew(binOrKey, entropy, l, prefix, p2sh, keyType);
            } catch (InvalidAddressException e) {
                e.printStackTrace();
                return null;
            }
        else
            try {
                a = NewAdd.createNew(binOrKey, entropy, l, p2sh, keyType);
            } catch (InvalidAddressException e) {
                e.printStackTrace();
                return null;
            }

        entropyMap.put(a.encodeForChain(), entropy);
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

        List<IAddress> toRemove = new ArrayList<>();
        for (IAddress add : addresses) {
            if (add.encodeForChain().equals(address.encodeForChain()))
                toRemove.add(add);

        }
        addresses.removeAll(toRemove);
        inactive.removeAll(toRemove);
        entropyMap.remove(address.encodeForChain());
        save();
    }

    @Override
    public void associateBinary(IAddress address, IBinary bin) {
        //ki.debug("Saving binary to: " + "binary" + address.encodeForChain());
        //ki.debug("Binary shit: " + Arrays.toString(bin.serializeToAmplet().serializeToBytes()));
        addressBinMap.put("binary" + address.encodeForChain(), bin);
        try {
            if (getBinary(address) == null) {
                ki.getMainLog().error("Error saving binary in address database");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinary getBinary(IAddress address) {
        //ki.debug("Loading binary from: " + "binary" + address.encodeForChain());
        //ki.debug("Binary shit from map: " + Arrays.toString(addressBinMap.getBytes("binary" + address.encodeForChain())));
        try {
            return Binary.deserializeFromAmplet(addressBinMap.get("binary" + address.encodeForChain()));
        } catch (Exception e) {
            ki.getMainLog().warn("Unable to get script for address: " + address.encodeForChain(), e);
            return null;
        }
    }

    @Override
    public void close() {
        save();
        addressMap.close();
    }

    @Override
    public IAddress createFromByteArray(byte[] array) {
        return Address.fromByteArray(array);
    }

    @Override
    public IAddress decodeFromChain(String s) {
        return Address.decodeFromChain(s);
    }
}
