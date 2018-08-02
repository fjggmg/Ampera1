package com.ampex.main.pool;

import com.ampex.amperabase.IOutput;
import com.ampex.amperabase.ITransAPI;
import com.ampex.amperanet.packets.TransactionPacket;
import com.ampex.main.IKi;
import com.ampex.main.transactions.ITrans;
import com.ampex.main.transactions.NewTrans;
import mining_pool.IPool;
import mining_pool.SafeTransactionGenerator;
import mining_pool.events.IPoolEventHandler;
import mining_pool.events.PoolEventType;

public class KiEventHandler implements IPoolEventHandler {
    private IKi ki;

    public KiEventHandler(IKi ki) {
        this.ki = ki;
    }

    @Override
    public void handle(IPool pool, PoolEventType poolEventType) throws Exception {
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
                ITransAPI t = stg.generatePayPeriodTransaction("Payment for pool mining");
                if (t == null) return;
                ITrans trans = NewTrans.fromAmplet(t.serializeToAmplet());
                if (ki.getTransMan().verifyTransaction(trans)) {
                    ki.getTransMan().getPending().add(trans);
                    ki.debug("Transaction info: ");
                    ki.debug("Outputs: " + t.getOutputs().size());
                    for (IOutput o : t.getOutputs()) {
                        ki.debug(o.getAddress().encodeForChain() + ":" + o.getAmount());
                    }
                    ki.debug("Inputs: " + t.getInputs().size());

                    ki.getTransMan().getPending().add(trans);
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
