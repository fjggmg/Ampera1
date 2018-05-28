package com.ampex.main.network.packets;

import amp.HeadlessPrefixedAmplet;
import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class RelayList implements Packet {

    List<String> relays;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {

        if (pg.relays == null) pg.relays = new ArrayList<>();
        pg.relays.addAll(relays);
        ki.getNetMan().addRelays(relays);
        if (ki.getNetMan().getConnections().size() < 4) {
            for (String IP : ki.getNetMan().getRelays()) {
                boolean cont = false;
                for (IConnectionManager c : ki.getNetMan().getConnections()) {
                    if (c.getAddress().split(":")[0].replace("/", "").equals(IP)) {
                        cont = true;
                        break;
                    }
                }
                if (cont) continue;

                ki.getNetMan().attemptConnect(IP);
            }
        }

    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(serialized);
            relays = new ArrayList<>();
            while (hpa.hasNextElement()) {
                relays.add(new String(hpa.getNextElement(), Charset.forName("UTF-8")));
            }
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create RelayList from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {

        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        if (relays != null)
            for (String relay : relays) {
                hpa.addElement(relay);
            }
        return hpa.serializeToBytes();
    }
}
