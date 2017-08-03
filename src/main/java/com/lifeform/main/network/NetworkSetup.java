package com.lifeform.main.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.lifeform.main.transactions.MKiTransaction;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Bryan on 7/25/2017.
 */
public class NetworkSetup {

    public static void setup(EndPoint endPoint)
    {
        Kryo kryo = endPoint.getKryo();
        kryo.register(String.class);
        kryo.register(Handshake.class);
        kryo.register(GoAhead.class);
        kryo.register(BadBlock.class);
        kryo.register(BigInteger.class);
        kryo.register(OnFork.class);
        kryo.register(MKiTransaction.class);
        kryo.register(Map.class);
        kryo.register(NewTransactionPacket.class);
        kryo.register(BlockProp.class);
        kryo.register(HashMap.class);

    }
}
