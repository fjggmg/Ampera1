package com.lifeform.main.transactions;

public class InvalidAddressException extends Exception {
    public InvalidAddressException(String message) {
        super(message);
    }
}
