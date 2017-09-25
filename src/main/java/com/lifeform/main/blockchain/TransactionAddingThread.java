package com.lifeform.main.blockchain;

import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.ITransMan;

public class TransactionAddingThread extends Thread {
    private TransactionVerifierThread.VerificationState state = TransactionVerifierThread.VerificationState.WORKING;
    private ITrans trans;
    private ITransMan transMan;

    public TransactionAddingThread(ITransMan transMan, ITrans trans) {
        this.trans = trans;
        this.transMan = transMan;
    }

    public void run() {
        if (!transMan.addTransactionNoVerify(trans))
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
