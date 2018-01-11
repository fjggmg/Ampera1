package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;

import java.util.ArrayList;
import java.util.List;

public class BlockVerificationHelper implements IBlockVerificationHelper {

    private Block block;
    private IKi ki;

    public BlockVerificationHelper(IKi ki, Block block) {
        this.block = block;
        this.ki = ki;
    }


    @Override
    public boolean verifyTransactions() {
        ki.debug("Beginning transaction verification from block management side");
        if (block.getTransactionKeys().size() == 0) {
            ki.debug("Block has no transactions, stopping verifier");
            return true;
        }
        ki.debug("Block has transactions");
        List<TransactionVerifierThread> workers = new ArrayList<>();
        for (String t : block.getTransactionKeys()) {
            TransactionVerifierThread worker = new TransactionVerifierThread(ki.getTransMan(), block.getTransaction(t));
            workers.add(worker);
            worker.start();
        }
        ki.debug("Created and started worker threads");
        List<TransactionVerifierThread> toRemove = new ArrayList<>();
        while (workers.size() > 0) {

            for (TransactionVerifierThread worker : workers) {
                if (worker.getVerificationState().equals(TransactionVerifierThread.VerificationState.SUCCESS)) {
                    ki.debug("Worker finished, removing from list");
                    toRemove.add(worker);
                } else if (worker.getVerificationState().equals(TransactionVerifierThread.VerificationState.FAILURE)) {
                    ki.debug("Found bad transaction");
                    return false;
                }
            }
            workers.removeAll(toRemove);
            toRemove.clear();
        }
        ki.debug("Transactions all verified");
        return true;
    }

    @Override
    public boolean addTransactions() {
        ki.debug("Beginning transaction adding from block management side");
        if (block.getTransactionKeys().size() == 0) {
            ki.debug("Block has no transactions, stopping adder");
            return true;
        }
        ki.debug("Block has transactions");
        List<TransactionAddingThread> workers = new ArrayList<>();
        for (String t : block.getTransactionKeys()) {
            TransactionAddingThread worker = new TransactionAddingThread(ki.getTransMan(), block.getTransaction(t));
            workers.add(worker);
            worker.start();
        }
        ki.debug("Created and started worker threads");
        List<TransactionAddingThread> toRemove = new ArrayList<>();
        boolean revert = false;
        while (workers.size() > 0) {

            for (TransactionAddingThread worker : workers) {
                if (worker.getVerificationState().equals(TransactionVerifierThread.VerificationState.SUCCESS)) {
                    ki.debug("Worker finished, removing from list");
                    toRemove.add(worker);
                } else if (worker.getVerificationState().equals(TransactionVerifierThread.VerificationState.FAILURE)) {
                    revert = true;
                    ki.debug("Found bad transaction");
                    break;
                }
            }
            workers.removeAll(toRemove);
            toRemove.clear();
        }

        if (revert) {
            for (String trans : block.getTransactionKeys()) {
                //ki.getTransMan().undoTransaction(block.getTransaction(trans));

            }
            return false;
        }
        ki.debug("Transactions all verified");
        return true;
    }
}
