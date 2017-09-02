package com.lifeform.main.network;

import java.math.BigInteger;

/**
 * Created by Bryan on 7/25/2017.
 */
public class Handshake {
    public static final String VERSION = NetMan.NET_VER;
    String ID;
    String version;
    BigInteger currentHeight;
    String mostRecentBlock;
    @Deprecated
    boolean isRelay;
}
