package com.ampex.main.blockchain;

public enum BlockState {

    SUCCESS(true, false),
    WRONG_HEIGHT(false, false),
    NO_PREVIOUS(false, true),
    PREVID_MISMATCH(false, false),
    BACKWARDS_TIMESTAMP(false, false),
    TIMESTAMP_WRONG(false, true),
    ID_MISMATCH(false, false),
    NO_SOLVE(false, false),
    NO_COINBASE(false, false),
    BAD_COINBASE(false, false),
    BAD_TRANSACTIONS(false, false),
    DOUBLE_SPEND(false, false),
    FAILED_ADD_COINBASE(false, true),
    FAILED_ADD_TRANS(false, true);

    private final boolean succes;
    private final boolean retry;

    BlockState(boolean success, boolean retry) {
        this.succes = success;
        this.retry = retry;
    }

    public boolean success() {
        return succes;
    }

    public boolean retry() {
        return retry;
    }
}
