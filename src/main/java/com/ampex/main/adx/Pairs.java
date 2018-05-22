package com.ampex.main.adx;

import com.ampex.amperabase.Token;

public enum Pairs {
    BTC_ORA(Token.BITCOIN, Token.ORIGIN, "AXA/BTC");

    private final Token onOffer;
    private final Token accepting;
    private final String name;

    Pairs(Token onOffer, Token accepting, String name) {
        this.onOffer = onOffer;
        this.accepting = accepting;
        this.name = name;
    }

    public Token onOffer() {
        return onOffer;
    }

    public Token accepting() {
        return accepting;
    }

    public String getName() {
        return name;
    }

    public static Pairs byName(String name) {
        for (Pairs pair : values()) {
            if (pair.getName().equals(name)) return pair;
        }
        return null;
    }
}
