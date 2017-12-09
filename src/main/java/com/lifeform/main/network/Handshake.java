package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.transactions.ITrans;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Created by Bryan on 7/25/2017.
 */
public class Handshake implements Serializable, Packet {
    public static final String VERSION = NetMan.NET_VER;
    public static short CHAIN_VER;
    String ID;
    String version;
    BigInteger currentHeight;
    String mostRecentBlock;
    short chainVer;
    boolean isRelay;
    long startTime;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.debug("Received handshake: ");
        ki.debug("ID: " + ID);
        ki.debug("Most recent block: " + mostRecentBlock);
        ki.debug("version: " + version);
        ki.debug("Height: " + currentHeight);
        ki.debug("Chain ver: " + chainVer);
        ki.debug("Address: " + connMan.getAddress());
        pg.startHeight = currentHeight;
        if (chainVer != Handshake.CHAIN_VER) {
            ki.debug("Mismatched chain versions, disconnecting");
            connMan.disconnect();
            return;
        }
        if (!version.equals(Handshake.VERSION)) {
            ki.debug("Mismatched network versions, disconnecting");
            connMan.disconnect();
            return;
        }
        if (ID.equals(EncryptionManager.sha224(ki.getEncryptMan().getPublicKeyString() + startTime))) {
            ki.debug("Connected to ourself, disconnecting");
            connMan.disconnect();
            return;
        }

        connMan.setID(ID);
        ki.getNetMan().connectionInit(ID, connMan);
        connMan.setStartTime(startTime);
        if (ki.getOptions().lite) {
            TransactionDataRequest tdr = new TransactionDataRequest();
            tdr.addresses = ki.getAddMan().getAll();
            connMan.sendPacket(tdr);
            DifficultyRequest dr = new DifficultyRequest();
            connMan.sendPacket(dr);
        }
        if (isRelay) {
            if (pg.relays == null) pg.relays = new ArrayList<>();
            pg.relays.add(connMan.getAddress());
        }
        if (pg.relays != null) {
            RelayList rl = new RelayList();
            rl.relays = pg.relays;
            connMan.sendPacket(rl);
            if (ki.getNetMan().getConnections().size() > 10 && ki.getNetMan().isRelay()) {
                DisconnectRequest dr = new DisconnectRequest();
                connMan.sendPacket(dr);
            }
        }

        if (ki.getChainMan().currentHeight().compareTo(BigInteger.valueOf(-1L)) != 0)
            if (currentHeight.compareTo(ki.getChainMan().currentHeight()) == 0) {
                for (ITrans trans : ki.getTransMan().getPending()) {
                    TransactionPacket tp = new TransactionPacket();
                    tp.trans = trans.toJSON();
                    connMan.sendPacket(tp);
                }
                pg.doneDownloading = true;
                if (ki.getOptions().pDebug)
                    ki.debug("Relay and Node agree on last block, done downloading");
            }
        if (currentHeight.compareTo(BigInteger.valueOf(-1L)) != 0)
            if (ki.getChainMan().currentHeight().compareTo(currentHeight) > 0) {
                if (!ki.getChainMan().getByHeight(currentHeight).ID.equals(mostRecentBlock)) {
                    pg.onRightChain = false;
                }
            }else if(ki.getChainMan().currentHeight().compareTo(currentHeight) == 0 && ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID.equals(mostRecentBlock))
            {
                pg.doneDownloading = true;
            }
        if (ki.getChainMan().currentHeight().compareTo(currentHeight) < 0) {
            ki.debug("Requesting blocks we're missing from the network");
            //pg.doneDownloading = true;
            BlockRequest br = new BlockRequest();
            br.lite = ki.getOptions().lite;
            br.fromHeight = ki.getChainMan().currentHeight();
            connMan.sendPacket(br);
        } else if (ki.getChainMan().currentHeight().compareTo(BigInteger.ZERO) > 0 && !ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID.equals(mostRecentBlock)) {
            if (ki.getChainMan().currentHeight().compareTo(currentHeight) < 0) {
                ki.debug("Discrepency between chains, starting resolution process");
                LastAgreedStart las = new LastAgreedStart();
                las.height = ki.getChainMan().currentHeight();
                pg.laFlag = true;
                connMan.sendPacket(las);
            }

        }
    }

    @Override
    public int packetType() {
        return PacketType.HS.getIndex();
    }
}
