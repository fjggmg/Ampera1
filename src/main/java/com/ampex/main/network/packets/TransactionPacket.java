package com.ampex.main.network.packets;

import amp.Amplet;
import amp.HeadlessPrefixedAmplet;
import com.ampex.amperabase.IInput;
import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;
import com.ampex.main.transactions.ITrans;
import com.ampex.main.transactions.InvalidTransactionException;
import com.ampex.main.transactions.Transaction;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionPacket implements Packet {

    public byte[] trans;
    public String block;

    private static ConcurrentHashMap.KeySetView<Object, Boolean> done = ConcurrentHashMap.newKeySet();
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if(ki.getOptions().pDebug)
        ki.debug("Received transaction packet");
        if (done.contains(trans) && (block == null || block.isEmpty())) {
            if (ki.getOptions().pDebug)
                ki.debug("Discarding because we already have this transaction for a block");
            return;
        }
        ITrans trans = null;
        try {
            ki.debug("Deserializing transaction...");
            Amplet amp = Amplet.create(this.trans);
            ki.debug("Created amplet");
            trans = Transaction.fromAmplet(amp);
            ki.debug("Deserialized");
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
            return;
        }
        if (block == null || block.isEmpty()) {
            ki.debug("Non-block transaction, verifying...");
            if (trans == null || trans.getInputs() == null) {
                if (ki.getOptions().pDebug)
                    ki.debug("Null trans or null inputs, discarding");
                return;
            }
            ki.debug("Transaction not null, result of verify: ");
            ki.debug("" + ki.getTransMan().verifyTransaction(trans));
            if (ki.getTransMan().verifyTransaction(trans)) {
                ki.debug("Verified");

                for (ITrans t : ki.getTransMan().getPending()) {
                    for (IInput i : t.getInputs()) {
                        for (IInput i2 : trans.getInputs()) {
                            if (i.getID().equals(i2.getID())) {
                                if (ki.getOptions().pDebug)
                                    ki.debug("Got bad transaction from network, double spend.");
                                return;
                            }
                        }
                    }
                }
                if (trans.getOutputs().get(0).getTimestamp() < System.currentTimeMillis() - 3_600_000) {
                    if (ki.getOptions().pDebug) {
                        ki.debug("Old transaction, discarding");
                    }
                    return;
                }

                if (trans.getOutputs().get(0).getAmount().compareTo(BigInteger.ZERO) <= 0) {
                    if (ki.getOptions().pDebug) {
                        ki.debug("Zero output, discarding");
                    }
                    return;
                }
                ki.getTransMan().getPending().add(trans);
                for (IInput i : trans.getInputs()) {

                    ki.getTransMan().getUsedUTXOs().add(i.getID());
                }
                if (ki.getNetMan().isRelay()) {
                    ki.debug("BROADCASTING TRANSACTION PACKET");
                    ki.getNetMan().broadcastAllBut(connMan.getID(), this);
                }

                if (ki.getOptions().pDebug) {
                    ki.debug("====TRANSACTION IS VERIFIED AND ADDED====");
                }
                if (ki.getOptions().poolRelay) {
                    ki.newTransPool();
                }
            } else {
                ki.debug("Transaction did not verify");
            }

        } else {

                if (pg.bMap.get(pg.headerMap.get(block)) != null) {
                    ki.debug("Adding transaction to block list");

                    pg.bMap.get(pg.headerMap.get(block)).add(trans);

                    done.add(trans);
                }

        }
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(serialized);
            trans = hpa.getNextElement();
            if (hpa.hasNextElement()) {
                block = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
            }
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create TransactionPacket from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.addBytes(trans);
        if (block != null)
            hpa.addElement(block);
        return hpa.serializeToBytes();
    }
}
