package com.lifeform.main;

public enum StringSettings {
    POOL_FEE("poolFee"),
    POOL_STATIC_PPS("poolStaticPPS");

    private final String key;

    StringSettings(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
