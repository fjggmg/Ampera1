package com.lifeform.main;

/**
 * this enum is for settings that are data (parsed to string) and handled by the god object
 */
public enum StringSettings {
    POOL_FEE("poolFee"),
    POOL_STATIC_PPS("poolStaticPPS"),
    PRIMARY_COLOR("primaryColor"),
    SECONDARY_COLOR("secondaryColor"),
    POOL_PAYTO("poolPayto"),
    POOL_SERVER("poolServer");

    private final String key;

    StringSettings(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
