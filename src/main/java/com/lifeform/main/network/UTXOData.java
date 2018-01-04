package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.transactions.Output;
import com.lifeform.main.transactions.TransactionManagerLite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UTXOData implements Serializable, Packet {

    List<String> utxos;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (ki.getOptions().lite) {
            ki.getMainLog().info("Received transaction data");
            List<Output> outputs = new ArrayList<>();
            for (String o : utxos) {
                Output out = Output.fromJSON(o);
                ki.debug("Output info: " + out.getID() + " Address " + out.getAddress().encodeForChain());
                outputs.add(out);
                //ki.getMainLog().info("Output: " + Output.fromJSON(o).getID());
            }
            ((TransactionManagerLite) ki.getTransMan()).addUTXOs(outputs);

        }
    }

    @Override
    public int packetType() {
        return 0;
    }
}
