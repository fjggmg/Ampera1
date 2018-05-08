package com.lifeform.main.data;

import amp.HeadlessPrefixedAmplet;
import amp.serialization.IAmpByteSerializable;
import com.lifeform.main.transactions.KeyType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class KeyKeyTypePair implements IAmpByteSerializable {
    public KeyKeyTypePair(@NotNull byte[] key, @NotNull KeyType keyType) {
        //find bugs wants me to have the next line for some reason? possibly report as bug to findbugs
        if (key == null) return;
        this.key = Arrays.copyOf(key, key.length);
        this.keyType = keyType;
    }

    private byte[] key;
    private KeyType keyType;

    @Override
    public byte[] serializeToBytes() {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.addBytes(key);
        hpa.addElement(keyType.getValue());
        return hpa.serializeToBytes();
    }

    public static KeyKeyTypePair fromBytes(byte[] bytes) {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(bytes);
        try {
            byte[] key = hpa.getNextElement();
            if (key == null) {
                System.out.println("key was null in KKTP");
                return null;
            }
            KeyType keyType = KeyType.byValue(hpa.getNextElement()[0]);
            if (keyType == null) throw new Exception("Key type invalid in KeyKeyTypePair");
            return new KeyKeyTypePair(key, keyType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] getKey() {
        return Arrays.copyOf(key, key.length);
    }

    public KeyType getKeyType() {
        return keyType;
    }

}
