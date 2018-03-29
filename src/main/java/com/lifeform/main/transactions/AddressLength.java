package com.lifeform.main.transactions;

/**
 * assumed SHA3 algo
 */
public enum AddressLength {

    SHA224((byte) 0),
    SHA256((byte) 1),
    SHA384((byte) 2),
    SHA512((byte) 3);
    private final byte indicator;

    AddressLength(byte indicator) {
        this.indicator = indicator;
    }

    public static AddressLength byIndicator(byte b) {
        for (AddressLength l : values()) {
            if (l.indicator == b) return l;
        }
        return null;
    }

    public byte getIndicator() {
        return indicator;
    }
}
