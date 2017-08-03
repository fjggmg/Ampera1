package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.data.JSONManager;
import org.bitbucket.backspace119.generallib.io.network.ConnectionManager;
import org.bitbucket.backspace119.generallib.io.network.NetworkManager;
import org.bitbucket.backspace119.generallib.io.network.Packet;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bryan on 7/20/2017.
 */
public class BlockRequestPacket implements Packet {

    private IKi ki;
    public BlockRequestPacket(IKi ki)
    {
        this.ki = ki;

    }

    @Override
    public Map<String, String> getData() {
        return data;
    }

    private Map<String,String> data;
    @Override
    public void setData(Map<String, String> map) {
        if(map.containsKey("height"))
        {
            data = map;
            data.put("type","BlockRequestPacket");
        }
    }

    @Override
    public void process(ConnectionManager networkManager) {

    }

    @Override
    public String toJSON() {
        return JSONManager.parseMapToJSON(data).toJSONString();
    }



}
