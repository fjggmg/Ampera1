package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.data.JSONManager;
import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.MKiTransaction;
import com.lifeform.main.transactions.Transaction;
import org.bitbucket.backspace119.generallib.io.network.ConnectionManager;
import org.bitbucket.backspace119.generallib.io.network.Packet;

import java.util.Map;

/**
 * Created by Bryan on 7/24/2017.
 */
public class TransactionPacket implements Packet {

    private IKi ki;
    public TransactionPacket(IKi ki)
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

        if(map != null && map.containsKey("transaction")) {
            this.data = map;
            data.put("type","TransactionPacket");
        }

    }

    @Override
    public void process(ConnectionManager cm) {
        ki.getMainLog().info("Transaction received from network: " + data.get("transaction"));
        ITrans trans = Transaction.fromJSON(data.get("transaction"));
        if(ki.getTransMan().verifyTransaction(trans)) {
            ki.getTransMan().getPending().add(trans);
            ki.getMainLog().info("New transaction from network added to pending pool");
            ki.getNetMan().broadcastAllBut(this,cm.getID());
        }
    }

    @Override
    public String toJSON() {
        return JSONManager.parseMapToJSON(data).toJSONString();
    }

    public static TransactionPacket fromJSON(IKi ki,String json)
    {
        TransactionPacket tp = new TransactionPacket(ki);
        tp.setData(JSONManager.parseJSONtoMap(json));
        return tp;
    }
}
