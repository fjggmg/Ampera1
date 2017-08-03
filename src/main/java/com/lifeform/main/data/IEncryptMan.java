package com.lifeform.main.data;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by Bryan on 7/14/2017.
 */
public interface IEncryptMan {

    public boolean verifySig(String signed, String sig,String pubKey);
    public String sign(String toSign);
    public PublicKey pubKeyFromString(String key);
    public PrivateKey privKeyFromString(String key);
    public PublicKey getPublicKey();
    public PrivateKey getPrivateKey();
    public KeyPair generateKeys();
    public void saveKeys();
    public KeyPair loadKeys();
}
