package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.data.JSONManager;
import org.bitbucket.backspace119.generallib.io.network.*;
import org.json.simple.JSONObject;

import java.util.Map;

/**
 * Created by Bryan on 7/20/2017.
 */
public class BlockPacket implements Packet {

    private IKi ki;
    public BlockPacket(IKi ki)
    {
        this.ki = ki;
    }
    private Map<String,String> data;

    @Override
    public Map<String, String> getData() {
        return data;
    }

    @Override
    public void setData(Map<String, String> map) {

        if(map.containsKey("block"))
        {
            //good packet, we should probably throw an exception if this is not....
            data = map;
        }
    }

    @Override
    public void process(ConnectionManager connMan) {
        if(!ki.getChainMan().addBlock(Block.fromJSON(data.get("block"))))
        {
            ki.getMainLog().warn("Received bad block from network, ignoring");
            if(ki.isRelay())
            {
                OnFork of = new OnFork();
                of.undoTo = ki.getChainMan().currentHeight();
                connMan.sendPacket(of);
            }

            //TODO: add mitigation code
        }else{
            ki.getMainLog().info("New block added from network");
        }
    }

    @Override
    public String toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("block",data.get("block"));
        obj.put("type","BlockPacket");
        return obj.toJSONString();
    }

    public static BlockPacket fromJSON(IKi ki,String json)
    {

        BlockPacket p = new BlockPacket(ki);
        Map<String,String> map = JSONManager.parseJSONtoMap(json);
        map.remove("type");
        p.setData(map);
        return p;
    }

}
