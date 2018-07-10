package com.ampex.main.blockchain;

import com.ampex.amperabase.IBlockAPI;
import com.ampex.main.IKi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockVerificationHelper implements IBlockVerificationHelper {

    private IBlockAPI block;
    private IKi ki;
    private ExecutorService executor;
    public BlockVerificationHelper(IKi ki) {
        this.ki = ki;
        this.executor = Executors.newWorkStealingPool();
    }

    public void init(IBlockAPI block)
    {
        this.block = block;
    }

    @Override
    public boolean verifyTransactions() {
        if(block == null)
        {
            ki.debug("Block passed to BVH was null");
            return false;
        }
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
            executor.execute(worker);
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

    @Deprecated
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

            return false;
        }
        ki.debug("Transactions all verified");
        return true;
    }
}
