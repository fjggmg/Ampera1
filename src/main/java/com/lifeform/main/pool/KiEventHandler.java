package com.lifeform.main.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.network.packets.TransactionPacket;
import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.Output;
import mining_pool.Pool;
import mining_pool.SafeTransactionGenerator;
import mining_pool.events.IPoolEventHandler;
import mining_pool.events.PoolEventType;

public class KiEventHandler implements IPoolEventHandler {
    private IKi ki;

    public KiEventHandler(IKi ki) {
        this.ki = ki;
    }

    @Override
    public void handle(Pool pool, PoolEventType poolEventType) throws Exception {
        switch (poolEventType) {
            case NEW_HEIGHT:

                break;
            case NEW_SHARE_DIFFICULTY:
                break;
            case NEW_PAY_PER_SHARE:
                break;
            case RECEIPTS_AVAILABLE:
                ki.debug("Paying miners in pool");
                SafeTransactionGenerator stg = new SafeTransactionGenerator(ki.getPoolManager(), ki);
                ITrans t = stg.generatePayPeriodTransaction("Payment for pool mining");
                if (t == null) return;
                if (ki.getTransMan().verifyTransaction(t)) {
                    ki.getTransMan().getPending().add(t);
                    ki.debug("Transaction info: ");
                    ki.debug("Outputs: " + t.getOutputs().size());
                    for (Output o : t.getOutputs()) {
                        ki.debug(o.getAddress().encodeForChain() + ":" + o.getAmount());
                    }
                    ki.debug("Inputs: " + t.getInputs().size());

                    ki.getTransMan().getPending().add(t);
                    TransactionPacket tp = new TransactionPacket();
                    tp.trans = t.serializeToAmplet().serializeToBytes();
                    ki.getNetMan().broadcast(tp);
                } else {
                    ki.debug("Transaction failed to verify");
                }
                break;
            default:
                break;
        }
    }
}
