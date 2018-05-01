package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.adx.Order;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.network.adx.OrderMatched;
import com.lifeform.main.network.adx.OrderPacket;
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
        if (ID.equals(EncryptionManager.sha224(ki.getEncryptMan().getPublicKeyString(ki.getAddMan().getMainAdd().getKeyType()) + startTime))) {
            ki.debug("Connected to ourself, disconnecting");
            connMan.disconnect();
            return;
        }
        connMan.gotHS();

        if (connMan.getChannel() != null && connMan.getChannel().remoteAddress() != null)
            ki.debug("Address: " + connMan.getAddress());
        else {
            //SOMETHING IS SERIOUSLY FUCKED UP
            connMan.disconnect();
            return;
        }

        connMan.setID(ID);
        if (!ki.getNetMan().connectionInit(ID, connMan)) {
            //TODO uncommented next line to test against possible attacks, will update this to require some work to prove connect is legit later
            connMan.disconnect();
            ki.debug("Already connected to this address");
            return;
        }
        ki.debug("Received handshake: ");
        ki.debug("ID: " + ID);
        //ki.debug("Most recent block: " + mostRecentBlock);
        ki.debug("version: " + version);
        ki.debug("Height: " + currentHeight);
        ki.debug("Chain ver: " + chainVer);
        if (!ki.getOptions().relay)
            ki.debug("Is Relay: " + isRelay);
        if (!ki.getOptions().lite && ki.getGUIHook() != null)
            ki.getGUIHook().setStart(currentHeight);
        if (ki.getOptions().poolRelay)
            ki.setStartHeight(currentHeight);
        connMan.setStartTime(startTime);
        if (ki.getChainMan().currentHeight().compareTo(BigInteger.valueOf(-1L)) == 0)
            connMan.sendPacket(new DoneDownloading());
        if (ki.getOptions().lite) {
            TransactionDataRequest tdr = new TransactionDataRequest();
            connMan.sendPacket(tdr);
            DifficultyRequest dr = new DifficultyRequest();
            connMan.sendPacket(dr);
        }
        if (isRelay) {
            if (pg.relays == null) pg.relays = new ArrayList<>();
            pg.relays.add(connMan.getAddress().split(":")[0].replace("/", ""));
            ki.getNetMan().addRelays(pg.relays);

        }

            RelayList rl = new RelayList();
            rl.relays = pg.relays;
        if (rl.relays == null) rl.relays = new ArrayList<>();
        rl.relays.addAll(ki.getNetMan().getRelays());
            connMan.sendPacket(rl);
            if (ki.getNetMan().getConnections().size() > 10 && ki.getNetMan().isRelay()) {
                DisconnectRequest dr = new DisconnectRequest();
                //connMan.sendPacket(dr);
            }

        for (Order o : ki.getExMan().getOrderBook().buys()) {
            OrderPacket op = new OrderPacket();
            op.order = o.serializeToBytes();
            op.transaction = ki.getExMan().txIDforOrderID(o.getID());
            connMan.sendPacket(op);
        }
        for (Order o : ki.getExMan().getOrderBook().sells()) {
            OrderPacket op = new OrderPacket();
            op.order = o.serializeToBytes();
            op.transaction = ki.getExMan().txIDforOrderID(o.getID());
            connMan.sendPacket(op);
        }

        for (Order o : ki.getExMan().getOrderBook().matched()) {
            OrderPacket op = new OrderPacket();
            op.transaction = o.getTxid();
            op.matched = true;
            op.order = o.serializeToBytes();
            connMan.sendPacket(op);
        }

        if (ki.getChainMan().currentHeight().compareTo(BigInteger.valueOf(-1L)) != 0)
            if (currentHeight.compareTo(ki.getChainMan().currentHeight()) == 0) {
                for (ITrans trans : ki.getTransMan().getPending()) {
                    TransactionPacket tp = new TransactionPacket();
                    tp.trans = trans.serializeToAmplet().serializeToBytes();
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
            }
        if (ki.getChainMan().currentHeight().compareTo(currentHeight) < 0) {
            ki.debug("Requesting blocks we're missing from the network");
            //pg.doneDownloading = true;
            BlockRequest br = new BlockRequest();
            br.lite = ki.getOptions().lite;
            br.fromHeight = ki.getChainMan().currentHeight();
            connMan.sendPacket(br);
        }
    }

    @Override
    public int packetType() {
        return PacketType.HS.getIndex();
    }
}
