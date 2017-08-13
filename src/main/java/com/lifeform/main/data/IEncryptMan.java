package com.lifeform.main.data;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by Bryan on 7/14/2017.
 */
public interface IEncryptMan {

    public String sign(String toSign);
    public PrivateKey privKeyFromString(String key);
    public PublicKey getPublicKey();
    public PrivateKey getPrivateKey();
    public KeyPair generateKeys();
    public void saveKeys();
    public KeyPair loadKeys();
    String getPublicKeyString();
    PublicKey pubKeyFromShortenedString(String key);
}
