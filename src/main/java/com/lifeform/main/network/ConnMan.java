package com.lifeform.main.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.Listener;
import com.lifeform.main.IKi;
import com.lifeform.main.MainGUI;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.data.JSONManager;
import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.MKiTransaction;
import com.lifeform.main.transactions.Transaction;
import org.bitbucket.backspace119.generallib.io.network.ConnectionManager;
import org.bitbucket.backspace119.generallib.io.network.Packet;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Bryan on 7/20/2017.
 */
public class ConnMan extends Listener implements org.bitbucket.backspace119.generallib.io.network.ConnectionManager {

    private Client client;
    private IKi ki;
    private String ID;
    private final ConnectionManager instance;
    private List<String> resentBlocks = new ArrayList<>();
    public ConnMan(IKi ki, Client client, String ID)
    {
        this.instance = this;
        this.ID = ID;
        this.client = client;
        this.ki = ki;

    }

    //probably doesn't need to return anything with new networking lib
    //it was always ignored anyway (shrugs)
    @Override
    public boolean sendPacket(Packet packet) {
        client.sendTCP(packet.toJSON());
        return true;
    }

    @Override
    public boolean sendPacket(Object o) {
        client.sendTCP(o);
        return true;
    }

    @Override
    public void received(Connection connection, Object object)
    {

        if(object instanceof String)
        listen((String) object);

        if(object instanceof BadBlock)
        {
            BadBlock bb = (BadBlock) object;
            Block b = ki.getChainMan().getByID(bb.ID);
            ki.getChainMan().setCanMine(false);
            if(b != null)
            {


                    /*
                    if(!resentBlocks.contains(b.ID)) {
                        NewBlockPacket nbp = new NewBlockPacket(ki);
                        Map<String, String> data = new HashMap<>();
                        data.put("block", b.toJSON());
                        nbp.setData(data);
                        sendPacket(nbp);
                    }else{
                        resentBlocks.remove(b.ID);
                        //TODO add code later about changing relays or checking with someone else
                        ki.getChainMan().undoToBlock(b.ID);
                        BlockRequestPacket brp = new BlockRequestPacket(ki);
                        Map<String,String> data = new HashMap<>();
                        data.put("height",ki.getChainMan().currentHeight().toString());
                        brp.setData(data);
                        sendPacket(brp);
                    }
                    */

                if(ki.getChainMan().currentHeight().compareTo(b.height) <= 0)
                {
                    ki.getChainMan().undoToBlock(ki.getChainMan().getByHeight(ki.getChainMan().currentHeight().subtract(BigInteger.valueOf(20L))).ID);
                    BlockRequestPacket brp = new BlockRequestPacket(ki);
                    Map<String,String> data = new HashMap<>();
                    data.put("height",ki.getChainMan().currentHeight().toString());
                    brp.setData(data);
                    sendPacket(brp);
                }else{
                    ki.getChainMan().undoToBlock(b.ID);

                }
                ki.getChainMan().setCanMine(true);
                //try a resend if this is most recent block


                //packet should have some info about what failed, mostly about what the current height is
                //if this is a bit back on the chain we'll need to pull a block update from then and invalidate our own chain
                //
            }
        }else if(object instanceof OnFork) {
            if (ki.getChainMan().currentHeight().compareTo(BigInteger.valueOf(-1L)) != 0) {
                OnFork of = (OnFork) object;
                ki.getChainMan().undoToBlock(ki.getChainMan().getByHeight(of.undoTo).ID);
            }
        }else if(object instanceof NewTransactionPacket)
        {
            NewTransactionPacket ntp = (NewTransactionPacket) object;
            if(ki.getTransMan().verifyTransaction(Transaction.fromJSON(ntp.trans))) ki.getTransMan().getPending().add(Transaction.fromJSON(ntp.trans));
        }else if(object instanceof BlockProp)
        {
            BlockProp bp = (BlockProp) object;

            MainGUI.blockPropped = bp.ID;
        }

    }

    @Override
    public boolean isConnected() {
        return client.isConnected();
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
                                    if(height.compareTo(ki.getChainMan().currentHeight()) > 0)
                                    {
                                        ki.getMainLog().info("Received request for blocks past what we have");
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
