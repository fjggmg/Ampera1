package com.lifeform.main.data;

import com.lifeform.main.transactions.KeyType;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by Bryan on 7/14/2017.
 *
 * this is being converted to be capable of ED25519 keys as well, so far the conversion is shitty.
 * There is a pending rewrite on this for this reason
 */
public interface IEncryptMan {

    String sign(String toSign, KeyType type);

    PrivateKey privKeyFromString(String key, KeyType keyType);

    PublicKey getPublicKey(KeyType keyType);

    PrivateKey getPrivateKey(KeyType keyType);

    KeyPair generateKeys();

    KeyPair generateEDKeys();

    KeyPair loadEDKeys();
    void saveKeys();

    void saveEDKeys();

    byte[] sign(byte[] toSign, KeyType type);
    KeyPair loadKeys();

    String getPublicKeyString(KeyType keyType);
}
