package com.lifeform.main.network.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.network.TransactionPacket;
import com.lifeform.main.transactions.ITrans;
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
                SafeTransactionGenerator stg = new SafeTransactionGenerator(ki.getPoolManager(), ki.getAddMan().getMainAdd(), ki);
                ITrans t = stg.generatePayPeriodTransaction("Payment for pool mining");
                TransactionPacket tp = new TransactionPacket();
                tp.trans = t.toJSON();
                ki.getNetMan().broadcast(tp);
                break;
        }
    }
}
