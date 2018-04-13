package com.lifeform.main.transactions;

public enum KeyType {
    NONE((byte) -1),
    BRAINPOOLP512T1((byte) 0),
    ED25519((byte) 1),;

    private final byte value;

    KeyType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static KeyType byValue(byte value) {
        for (KeyType type : values()) {
            if (type.getValue() == value) return type;
        }
        return null;
    }

}
