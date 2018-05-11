package com.lifeform.main.network.packets;

import amp.HeadlessPrefixedAmplet;
import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.transactions.Output;
import com.lifeform.main.transactions.TransactionManagerLite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UTXOData implements Serializable, Packet {
    private static final long serialVersionUID = 184L;
    byte[] utxos;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (ki.getOptions().lite) {
            ki.getMainLog().info("Received transaction data");
            List<Output> outputs = new ArrayList<>();
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(utxos);
            while (hpa.hasNextElement()) {
                Output out = Output.fromBytes(hpa.getNextElement());
                if (out == null) continue;
                ki.debug("Output info: " + out.getID() + " Address " + out.getAddress().encodeForChain());
                outputs.add(out);
                //ki.getMainLog().info("Output: " + Output.fromJSON(o).getID());
            }
            ((TransactionManagerLite) ki.getTransMan()).addUTXOs(outputs);

        }
    }

}
