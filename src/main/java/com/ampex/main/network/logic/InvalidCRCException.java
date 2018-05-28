package com.ampex.main.network.logic;

public class InvalidCRCException extends Exception {
    public InvalidCRCException(String msg) {
        super(msg);
    }
}
