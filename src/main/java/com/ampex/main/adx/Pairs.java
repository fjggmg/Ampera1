package com.ampex.main.adx;

import com.ampex.amperabase.Token;

public enum Pairs {
    BTC_ORA(Token.BITCOIN, Token.ORIGIN, "AXA/BTC", "BTC", "AXA");

    private final Token onOffer;
    private final Token accepting;
    private final String name;
    private final String onOfferName;
    private final String acceptingName;

    Pairs(Token onOffer, Token accepting, String name, String onOfferName, String acceptingName) {
        this.onOffer = onOffer;
        this.accepting = accepting;
        this.name = name;
        this.onOfferName = onOfferName;
        this.acceptingName = acceptingName;
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

    public String getOnOfferName() {
        return onOfferName;
    }

    public String getAcceptingName() {
        return acceptingName;
    }
}
