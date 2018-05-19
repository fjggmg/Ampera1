package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.network.IConnectionManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RelayList implements Serializable, Packet {
    private static final long serialVersionUID = 184L;
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

}
