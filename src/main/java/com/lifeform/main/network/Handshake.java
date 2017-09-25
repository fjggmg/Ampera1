package com.lifeform.main.network;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Created by Bryan on 7/25/2017.
 */
public class Handshake implements Serializable{
    public static final String VERSION = NetMan.NET_VER;
    public static short CHAIN_VER;
    String ID;
    String version;
    BigInteger currentHeight;
    String mostRecentBlock;
    short chainVer;
    @Deprecated
    boolean isRelay;
}
