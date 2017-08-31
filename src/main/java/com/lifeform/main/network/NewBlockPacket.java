package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.data.JSONManager;
import org.bitbucket.backspace119.generallib.io.network.ConnectionManager;
import org.bitbucket.backspace119.generallib.io.network.NetworkManager;
import org.bitbucket.backspace119.generallib.io.network.Packet;

import javax.print.DocFlavor;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bryan on 7/24/2017.
 */
public class NewBlockPacket implements Packet {

    private Map<String,String> data;
    private IKi ki;
    public NewBlockPacket(IKi ki)
    {
        this.ki = ki;
    }
    private static boolean lock = false;
    @Override
    public Map<String, String> getData() {
        return data;
    }

    @Override
    public void setData(Map<String, String> map) {
        if(map.containsKey("block"))
        {
            this.data = map;
            data.put("type","NewBlockPacket");

        }
    }

    @Override
    public void process(ConnectionManager connectionManager) {
        while(lock)
        {
            //wait
            try {
                Thread.sleep(20L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lock = true;
        Block b = Block.fromJSON(data.get("block"));
        if(ki.getChainMan().getByID(b.ID) != null)
        {
            ki.getMainLog().info("Received duplicate block, ignoring");
            lock = false;
            return;
        }

        if(connectionManager.getID().equals(b.solver) && (System.currentTimeMillis()  - b.timestamp > 120000) || (b.timestamp - System.currentTimeMillis() > 120000))
        {
            ki.getMainLog().info("Received block with bad timestamp, refusing");
            lock = false;
            return;
        }

        if(b.height.compareTo(ki.getChainMan().currentHeight().add(BigInteger.ONE)) > 0)
        {
            if(ki.isRelay()) {

                //TODO: we're now going to accept future blocks from nodes everywhere and ask for a chain update
                LastAgreedRequest lar = new LastAgreedRequest();
                lar.currentHeight = ki.getChainMan().currentHeight();
                connectionManager.sendPacket(lar);
            }else{
                ki.getMainLog().info("Received orphan block, caching for later verification");
                ki.getChainMan().verifyLater(b);
                /*
                BlockRequestPacket brp = new BlockRequestPacket(ki);
                Map<String,String> data = new HashMap<>();
                data.put("height",ki.getChainMan().currentHeight().toString());
                brp.setData(data);
                connectionManager.sendPacket(brp);
                */
            }
            /*
            BlockRequestPacket brp = new BlockRequestPacket(ki);
            Map<String,String> data = new HashMap<>();
            data.put("height",ki.getChainMan().currentHeight().toString());
            brp.setData(data);
            connectionManager.sendPacket(brp);
            */
            lock = false;
            return;
        }
        if(!ki.getChainMan().addBlock(b))
        {
            /*
            if(ki.isRelay()) {
                ki.getMainLog().warn("Received bad new block from network, sending BadBlock notification");
                BadBlock bb = new BadBlock();
                bb.ID = b.ID;
                bb.currentHeight = ki.getChainMan().currentHeight();
                connectionManager.sendPacket(bb);
            }else{
                ki.getChainMan().undoToBlock(ki.getChainMan().getByHeight(ki.getChainMan().currentHeight().subtract(BigInteger.valueOf(20L))).ID);
                BlockRequestPacket brp = new BlockRequestPacket(ki);
                Map<String,String> data = new HashMap<>();
                data.put("height",ki.getChainMan().currentHeight().toString());
                brp.setData(data);
                connectionManager.sendPacket(brp);
            }
            */
            ki.getMainLog().info("Received bad new block from network");
            BlockProp bp = new BlockProp();
            bp.ID = b.ID;
            connectionManager.sendPacket(bp);
        }else{
            ki.getMainLog().info("Received new block from network");

            if(ki.isRelay())
            ki.getNetMan().broadcastPacket(this);
        }

        lock = false;
    }

    @Override
    public String toJSON() {
        return JSONManager.parseMapToJSON(data).toJSONString();
    }
}
