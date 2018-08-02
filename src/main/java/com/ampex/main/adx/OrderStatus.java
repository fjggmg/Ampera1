package com.ampex.main.adx;

public enum OrderStatus {
    COMPLETE(true, true, "NONE"),
    BAD_UTXOS_US(true, false, "OOU"),
    BAD_UTXOS_THEM(true, false, "OOT"),
    GENERAL_FAILURE(false, false, "GF"),
    TRANSACTION_FAILURE(true, false, "TF"),
    NOT_ENOUGH_FOR_FEE(true,false,"OOF");
    private final boolean success;
    private final boolean partial;
    private final String error;
    OrderStatus(boolean partial, boolean success, String error) {
        this.success = success;
        this.partial = partial;
        this.error = error;
    }

    public boolean succeeded() {
        return success;
    }

    public boolean partial() {
        return partial;
    }

    public String errorCode()
    {
        return error;
    }

}
