package com.lifeform.main.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.lifeform.main.transactions.*;

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
        kryo.register(GoAhead.class);
        kryo.register(BadBlock.class);
        kryo.register(BigInteger.class);
        kryo.register(OnFork.class);
        kryo.register(ITrans.class);
        kryo.register(Transaction.class);
        kryo.register(Map.class);
        kryo.register(NewTransactionPacket.class);
        kryo.register(BlockProp.class);
        kryo.register(HashMap.class);
        kryo.register(Address.class);
        kryo.register(Output.class);
        kryo.register(Input.class);
        kryo.register(Byte.class);
        kryo.register(Integer.class);
        kryo.register(ArrayList.class);
        kryo.register(Token.class);
        kryo.register(String.class);
        kryo.register(ChainUpdate.class);
        kryo.register(ChainRequest.class);
        kryo.register(LastAgreed.class);
        kryo.register(LastAgreedList.class);
        kryo.register(LastAgreedRequest.class);

    }
}
