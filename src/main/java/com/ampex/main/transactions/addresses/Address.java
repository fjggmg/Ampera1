package com.ampex.main.transactions.addresses;

import com.ampex.amperabase.AddressLength;
import com.ampex.amperabase.IAddress;
import com.ampex.amperabase.KeyType;
import com.ampex.main.data.EncryptionManager;
import com.ampex.main.data.Utils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * Created by Bryan on 8/8/2017.
 */
public class Address implements Serializable, IAddress {
    private static final long serialVersionUID = 184L;
    public static final byte VERSION = 0X7F;
    public Address(byte version, String ID, String checksum)
    {
        this.version = version;
        this.ID = ID;
        this.checksum = checksum;
        encoded = version + ID + checksum;
    }
    private byte version;
    private String ID;
    private String checksum;
    private String encoded;
    public String encodeForChain()
    {
        return encoded;
    }
    public byte getVersion()
    {
        return version;
    }
    public String getID()
    {
        return ID;
    }
    public String getChecksum()
    {
        return checksum;
    }

    public boolean isValid() {
        String check = EncryptionManager.sha224Hex(ID);
        char[] idChar = check.toCharArray();
        char[] cChar = {idChar[idChar.length - 4], idChar[idChar.length - 3], idChar[idChar.length - 2], idChar[idChar.length - 1]};
        String sum = new String(cChar);
        return toByteArrayStrict().length != 0 && sum.equals(checksum);
    }

    @Override
    public boolean canSpend(String keys, String entropy, boolean p2sh, KeyType keyType)
    {
        if (!keyType.equals(KeyType.BRAINPOOLP512T1)) return false;
        if (p2sh) return false;
        Address a = createNew(keys,entropy);
        return a.encodeForChain().equals(encodeForChain());
    }

    /**
     * VERY IMPORTANT! USE THIS WHEN SENDING/RECEIVING COINS OFTEN TO OBFUSCATE THE WALLET FOR PRIVACY/SECURITY!
     * AT THE VERY LEAST USE IT EVERY TIME COINS ARE SPENT FROM THE CURRENT ADDRESS!
     * ADDRESS REUSE CAN CAUSE CANCER!
     * @param entropy random String to hash along with the key to create a unique new Address, should be backed-up and kept track of
     *                to be able to spend coins in this Address in the future. Should be at least 224 bits for sufficient entropy
     * @param keys The keys to make this address with, provided for multisig wallets and/or multiple key wallets that have
     *            more than one key
     * @return a new Address from the given entropy, this is not as secure as completely changing keys, but is a good measure for
     *         day to day use
     */
    public static Address createNew(String keys,String entropy)
    {
        String hash = EncryptionManager.sha224(keys+entropy);
        String fullChecksum = EncryptionManager.sha224Hex(hash);
        char[] checkChar = fullChecksum.toCharArray();

        char[] last4 = {checkChar[checkChar.length-4], checkChar[checkChar.length-3], checkChar[checkChar.length-2], checkChar[checkChar.length-1]};

        String checksum = new String(last4);

        return new Address(VERSION,hash,checksum);
    }

    public String getChecksumGen() {
        String fullChecksum = EncryptionManager.sha224Hex(ID);
        char[] checkChar = fullChecksum.toCharArray();
        char[] last4 = {checkChar[checkChar.length - 4], checkChar[checkChar.length - 3], checkChar[checkChar.length - 2], checkChar[checkChar.length - 1]};

        String checksum = new String(last4);
        return checksum;
    }

    public static IAddress decodeFromChain(String encoded)
    {
        char[] eChar = encoded.toCharArray();
        char[] vChar = {eChar[0],eChar[1],eChar[2]};
        byte version = Byte.parseByte(new String(vChar));
        if (version != VERSION) return NewAdd.decodeFromChain(encoded);
        StringBuilder ID = new StringBuilder();
        for(int i = 3; i < eChar.length - 4;i++)
        {
            ID.append(eChar[i]);
        }
        char[] cChar = {eChar[eChar.length-4],eChar[eChar.length-3],eChar[eChar.length-2],eChar[eChar.length-1]};

        String checksum = new String(cChar);

        return new Address(version, ID.toString(), checksum);
    }

    public byte[] toByteArrayStrict() {
        byte[] payload;// = {};
        try {
            payload = Utils.fromBase64(ID);
        } catch (Exception e) {
            return new byte[0];
        }
        byte[] array;
        array = new byte[payload.length + 3];
        array[0] = version;
        int i = 1;
        for (byte b : payload) {
            array[i] = b;
            i++;
        }
        byte[] check = Utils.toByteArray(checksum);
        array[array.length - 2] = check[0];
        array[array.length - 1] = check[1];
        return array;
    }

    public byte[] toByteArray() {
        try {
            return encodeForChain().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public boolean hasPrefix() {
        return false;
    }

    @Override
    public String getPrefix() {
        return "";
    }

    @Override
    public boolean canSpendPrefixed(String key, String entropy, String prefix, boolean p2sh, KeyType keyType) {
        return false;
    }

    @Override
    public boolean isP2SH() {
        return false;
    }

    @Override
    public KeyType getKeyType() {
        return KeyType.BRAINPOOLP512T1;
    }

    @Override
    public AddressLength getAddressLength() {
        return AddressLength.SHA224;
    }

    /**
     * This method will point the array to NewAdd if it is not of the old address spec version, this way, we can reliably
     * make sure we're not breaking code because we can use the same code we were using before NewAdd came around. This
     * also means in the future if you are uncertain of what kind of address you have in a byte array you can pass it
     * here and rest assured you'll get something correct out of it
     *
     * @param array byte array containing address data
     * @return implementation of an address matching the version in the byte array
     */
    public static IAddress fromByteArray(byte[] array) {

        byte[] posVer = new byte[3];
        System.arraycopy(array, 0, posVer, 0, 3);
        String ver;
        try {
            ver = new String(posVer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return NewAdd.fromByteArray(array);
        }
        if (!ver.equals("" + VERSION)) return NewAdd.fromByteArray(array);
        try {
            return decodeFromChain(new String(array, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
