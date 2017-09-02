package com.lifeform.main.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.transactions.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Bryan on 7/25/2017.
 */
public class NetworkSetup {

    public static void setup(EndPoint endPoint)
    {
        Kryo kryo = endPoint.getKryo();

        kryo.register(Handshake.class);
        kryo.register(BigInteger.class);
        kryo.register(ITrans.class);
        kryo.register(TransactionPacket.class);
        kryo.register(Map.class);
        kryo.register(HashMap.class);
        kryo.register(Address.class);
        kryo.register(Output.class);
        kryo.register(Input.class);
        kryo.register(Byte.class);
        kryo.register(Integer.class);
        kryo.register(ArrayList.class);
        kryo.register(Token.class);
        kryo.register(String.class);
        kryo.register(Block.class);
        kryo.register(JSONObject.class);
        kryo.register(JSONArray.class);
        kryo.register(BlockEnd.class);
        kryo.register(BlockHeader.class);
        kryo.register(BlocksRequest.class);
        kryo.register(ChainUpEnd.class);
        kryo.register(ChainUpStart.class);
        kryo.register(LastAgreedStart.class);
        kryo.register(LastAgreedContinue.class);
        kryo.register(LastAgreedEnd.class);
        kryo.register(TransactionPacket.class);
        kryo.register(Transaction.class);


    }
}
