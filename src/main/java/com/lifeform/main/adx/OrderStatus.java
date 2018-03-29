package com.lifeform.main.adx;

public enum OrderStatus {
    COMPLETE(true, true),
    BAD_UTXOS_US(true, false),
    BAD_UTXOS_THEM(true, false),
    GENERAL_FAILURE(false, false),
    TRANSACTION_FAILURE(true, false);
    private final boolean success;
    private final boolean partial;

    OrderStatus(boolean partial, boolean success) {
        this.success = success;
        this.partial = partial;
    }

    public boolean succeeded() {
        return success;
    }

    public boolean partial() {
        return partial;
    }
}
