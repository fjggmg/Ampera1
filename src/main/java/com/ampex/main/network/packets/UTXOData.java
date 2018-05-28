package com.ampex.main.network.packets;

import amp.HeadlessPrefixedAmplet;
import com.ampex.amperabase.IOutput;
import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;
import com.ampex.main.transactions.Output;
import com.ampex.main.transactions.TransactionManagerLite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UTXOData implements Packet {

    byte[] utxos;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (ki.getOptions().lite) {
            ki.getMainLog().info("Received transaction data");
            List<IOutput> outputs = new ArrayList<>();
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(utxos);
            while (hpa.hasNextElement()) {
                IOutput out = Output.fromBytes(hpa.getNextElement());
                //if (out == null) continue;
                ki.debug("Output info: " + out.getID() + " Address " + out.getAddress().encodeForChain());
                outputs.add(out);
                //ki.getMainLog().info("Output: " + Output.fromJSON(o).getID());
            }
            ((TransactionManagerLite) ki.getTransMan()).addUTXOs(outputs);

        }
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        utxos = Arrays.copyOf(serialized, serialized.length);
    }

    @Override
    public byte[] serializeToBytes() {
        return Arrays.copyOf(utxos, utxos.length);
    }
}
