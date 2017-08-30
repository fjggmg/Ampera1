package com.lifeform.main.network;

/**
 * packet to send to client when we cannot handle their connection, we will send them an IP of another relay to try
 */
public class TryOtherRelay {
    String ip;
}
