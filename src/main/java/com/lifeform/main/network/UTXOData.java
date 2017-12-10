package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.transactions.Output;
import com.lifeform.main.transactions.TransactionManagerLite;

import java.io.Serializable;
import java.util.List;

public class UTXOData implements Serializable, Packet {

    List<Output> utxos;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (ki.getOptions().lite) {
            ki.getMainLog().info("Received transaction data: ");
            for (Output o : utxos) {
                ki.getMainLog().info("Output: " + o.getID());
            }
            ((TransactionManagerLite) ki.getTransMan()).addUTXOs(utxos);

        }
    }

    @Override
    public int packetType() {
        return 0;
    }
}
