package com.lifeform.main.network;

import java.math.BigInteger;

/**
 * Created by Bryan on 7/25/2017.
 */
public class Handshake {
    String ID;
    String version;
    BigInteger currentHeight;
    String mostRecentBlock;
    boolean isRelay;
}
