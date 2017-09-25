package com.lifeform.main.blockchain;

import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.ITransMan;

public class TransactionVerifierThread extends Thread {

    private VerificationState state = VerificationState.WORKING;
    private ITrans trans;
    private ITransMan transMan;

    public TransactionVerifierThread(ITransMan transMan, ITrans trans) {
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
