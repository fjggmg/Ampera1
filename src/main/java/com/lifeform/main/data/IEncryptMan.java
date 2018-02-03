package com.lifeform.main.data;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by Bryan on 7/14/2017.
 */
public interface IEncryptMan {

    String sign(String toSign);

    PrivateKey privKeyFromString(String key);

    PublicKey getPublicKey();

    PrivateKey getPrivateKey();

    KeyPair generateKeys();

    void saveKeys();

    KeyPair loadKeys();
    String getPublicKeyString();
    PublicKey pubKeyFromShortenedString(String key);
}
