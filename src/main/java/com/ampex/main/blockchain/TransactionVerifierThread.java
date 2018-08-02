package com.ampex.main.blockchain;

import com.ampex.amperabase.ITransAPI;
import com.ampex.main.transactions.ITransMan;

public class TransactionVerifierThread extends Thread {

    private VerificationState state = VerificationState.WORKING;
    private ITransAPI trans;
    private ITransMan transMan;

    public TransactionVerifierThread(ITransMan transMan, ITransAPI trans) {
        this.trans = trans;
        this.transMan = transMan;
    }

    public void run() {
        if (!transMan.verifyTransaction(trans))
            state = VerificationState.FAILURE;
        else
            state = VerificationState.SUCCESS;
    }

    public VerificationState getVerificationState() {
        return state;
    }

    public enum VerificationState {
        WORKING,
        SUCCESS,
        FAILURE
    }
}
