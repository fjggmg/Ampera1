package com.ampex.main.blockchain;

import com.ampex.amperabase.ITransAPI;
import com.ampex.main.transactions.ITransMan;

public class TransactionAddingThread extends Thread {
    private TransactionVerifierThread.VerificationState state = TransactionVerifierThread.VerificationState.WORKING;
    private ITransAPI trans;
    private ITransMan transMan;

    public TransactionAddingThread(ITransMan transMan, ITransAPI trans) {
        this.trans = trans;
        this.transMan = transMan;
    }

    public void run() {
        if (!transMan.addTransaction(trans))
            state = TransactionVerifierThread.VerificationState.FAILURE;
        else
            state = TransactionVerifierThread.VerificationState.SUCCESS;
    }

    public TransactionVerifierThread.VerificationState getVerificationState() {
        return state;
    }

    public enum VerificationState {
        WORKING,
        SUCCESS,
        FAILURE
    }

}
