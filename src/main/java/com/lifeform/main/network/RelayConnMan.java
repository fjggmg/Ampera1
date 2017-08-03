package com.lifeform.main.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.lifeform.main.IKi;
import com.lifeform.main.data.JSONManager;
import com.lifeform.main.data.Utils;
import org.bitbucket.backspace119.generallib.io.network.ConnectionManager;
import org.bitbucket.backspace119.generallib.io.network.Packet;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Bryan on 7/25/2017.
 */
public class RelayConnMan extends Listener implements ConnectionManager {
    private Connection connection;
    private IKi ki;
    private String ID;
    private final ConnectionManager instance;
    public RelayConnMan(IKi ki, Connection connection, String ID)
    {
        this.instance = this;
        this.ID = ID;
        this.connection = connection;
        this.ki = ki;

    }

    //probably doesn't need to return anything with new networking lib
    //it was always ignored anyway (shrugs)
    @Override
    public boolean sendPacket(Packet packet) {
        connection.sendTCP(packet.toJSON());
        return true;
    }

    @Override
    public boolean sendPacket(Object o) {
        connection.sendTCP(o);
        return true;
    }

    @Override
    public void received(Connection connection, Object object)
    {
        //ki.getMainLog().info("received packet");
        if(connection.getID() == this.connection.getID()) {
            if (object instanceof String) {
                listen((String) object);
            }
            else if(object instanceof NewTransactionPacket)
            {
                ki.getMainLog().info("Received new transaction packet from node");
                NewTransactionPacket ntp = (NewTransactionPacket) object;
                if(ntp.trans.relayer.equals(Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded())))
                {
                    ki.getMainLog().info("Verified we are relaying this transaction");
                    //TODO verify we're being paid later
                    ntp.trans.relaySignature = ki.getEncryptMan().sign(ntp.trans.preSigAll());
                    sendPacket(ntp);
                }
            }
        }
    }

    @Override
    public boolean isConnected() {
        return connection.isConnected();
    }

    private List<Packet> packets = new CopyOnWriteArrayList<>();
    @Override
    public List<Packet> receivedPackets() {
        return packets;
    }

    boolean run = true;

    @Override
    public void listen(String line) {

        Map<String, String> map = JSONManager.parseJSONtoMap(line);
        if (map != null && map.containsKey("type"))
            switch (map.get("type")) {
                case "BlockPacket":

                    //packets.add(BlockPacket.fromJSON(ki, line));
                    BlockPacket.fromJSON(ki,line).process(this);
                    break;
                case "BlockRequestPacket":

                    BigInteger height = new BigInteger(map.get("height"));
                    height = height.add(BigInteger.ONE);
                    ki.getMainLog().info("Height requested: " + height);
                    if(height.compareTo(ki.getChainMan().currentHeight()) > 0)
                    {
                        ki.getMainLog().info("Received request for blocks past what we have");
                        OnFork of = new OnFork();
                        of.undoTo = ki.getChainMan().currentHeight();
                        sendPacket(of);
                    }else {
                        while (height.compareTo(ki.getChainMan().currentHeight()) <= 0) {
                            BlockPacket bp = new BlockPacket(ki);
                            Map<String, String> bdat = new HashMap<>();
                            bdat.put("block", ki.getChainMan().getByHeight(height).toJSON());
                            bp.setData(bdat);
                            sendPacket(bp);
                            height = height.add(BigInteger.ONE);
                        }
                    }
                    break;
                case "TransactionPacket":
                    TransactionPacket tp = new TransactionPacket(ki);
                    tp.setData(map);
                    tp.process(this);

                    break;
                case "NewBlockPacket":
                    NewBlockPacket nb = new NewBlockPacket(ki);
                    nb.setData(map);
                    nb.process(instance);

                    break;
                default:

                    break;
            }



    }





    @Override
    public String getID() {
        return ID;
    }
}
