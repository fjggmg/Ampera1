package com.lifeform.main;

public enum Settings {
    HIGH_SECURITY("highSecurity"),
    DEBUG_MODE("debugMode"),
    REQUIRE_PASSWORD("requirePassword"),
    DYNAMIC_FEE("dynamicFee"),
    AUTO_MINE("autoMine"),
    PPLNS_SERVER("pplnsServer"),
    PPLNS_CLIENT("pplnsClient");
    private final String key;

    Settings(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
