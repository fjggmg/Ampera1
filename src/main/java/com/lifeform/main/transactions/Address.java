package com.lifeform.main.transactions;

import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.Utils;
import org.json.simple.JSONObject;

/**
 * Created by Bryan on 8/8/2017.
 */
public class Address {

    public static final byte VERSION = 0X7F;
    public Address(byte version, String ID, String checksum)
    {
        this.version = version;
        this.ID = ID;
        this.checksum = checksum;
    }
    private byte version;
    private String ID;
    private String checksum;

    public String encodeForChain()
    {
        return version + ID + checksum;
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

    public boolean isValid()
    {
        String check = EncryptionManager.sha224Hex(ID);
        char[] idChar = check.toCharArray();
        char[] cChar = {idChar[idChar.length-4],idChar[idChar.length-3],idChar[idChar.length-2],idChar[idChar.length-1]};
        String sum = new String(cChar);
        return sum.equals(checksum);
    }

    public boolean canSpend(String keys, String entropy)
    {
        Address a = createNew(keys,entropy);
        //System.out.println("Address 1: " + a.encodeForChain());
        //System.out.println("Address 2: " + encodeForChain());
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

    public static Address decodeFromChain(String encoded)
    {
        char[] eChar = encoded.toCharArray();
        char[] vChar = {eChar[0],eChar[1],eChar[2]};
        byte version = Byte.parseByte(new String(vChar));
        String ID = "";
        for(int i = 3; i < eChar.length - 4;i++)
        {
            ID = ID + eChar[i];
        }

        char[] cChar = {eChar[eChar.length-4],eChar[eChar.length-3],eChar[eChar.length-2],eChar[eChar.length-1]};

        String checksum = new String(cChar);

        return new Address(version,ID,checksum);
    }


    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Address)
        {
            Address a = (Address) o;
            return a.encodeForChain().equals(encodeForChain());
        }else{
            return false;
        }
    }
}
