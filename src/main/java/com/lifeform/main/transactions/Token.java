package com.lifeform.main.transactions;

/**
 * Created by Bryan on 8/10/2017.
 */
public enum Token {

    ORIGIN("Ampera"),
    KI("Eko"),
    BITCOIN("Bitcoin"),
    GOLD("Gold"),
    SILVER("Silver"),
    PLATINUM("Platinum"),
    PALADIUM("Paladium"),
    COPPER("Copper"),
    USD("USD"),
    EUR("EUR"),
    JPY("JPY"),
    CNY("CNY"),
    GBP("GBP"),
    CAD("CAD"),
    AUD("AUD"),
    OIL("Oil"),
    NATURAL_GAS("Natural Gas"),
    HEATING_OIL("Heating Oil"),
    SUGAR("Sugar"),
    WHEAT("Wheat"),
    CORN("Corn"),
    SOYBEAN("Soybean"),
    COCAO("Cocao"),
    REAL_ESTATE("Real Estate"),
    TOKEN1("Bitcoin Cash"),
    TOKEN2("Litecoin"),
    TOKEN3("Dash"),
    TOKEN4("TOKEN"),
    TOKEN5("TOKEN"),
    TOKEN6("TOKEN"),
    TOKEN7("TOKEN"),
    TOKEN8("TOKEN"),
    TOKEN9("TOKEN"),
    TOKEN10("TOKEN"),
    TOKEN11("TOKEN"),
    TOKEN12("TOKEN"),
    TOKEN13("TOKEN"),
    TOKEN14("TOKEN"),
    TOKEN15("TOKEN"),
    TOKEN16("TOKEN"),
    TOKEN17("TOKEN"),
    TOKEN18("TOKEN"),
    TOKEN19("TOKEN"),
    TOKEN20("TOKEN"),
    TOKEN21("TOKEN"),
    TOKEN22("TOKEN"),
    TOKEN23("TOKEN"),
    TOKEN24("TOKEN"),
    TOKEN25("TOKEN"),
    TOKEN26("TOKEN"),
    TOKEN27("TOKEN"),
    TOKEN28("TOKEN"),
    TOKEN29("TOKEN"),
    TOKEN30("TOKEN"),
    TOKEN31("TOKEN"),
    TOKEN32("TOKEN"),
    TOKEN33("TOKEN"),
    TOKEN34("TOKEN"),
    TOKEN35("TOKEN"),
    TOKEN36("TOKEN"),
    TOKEN37("TOKEN"),
    TOKEN38("TOKEN"),
    TOKEN39("TOKEN"),
    TOKEN40("TOKEN"),
    TOKEN41("TOKEN"),
    TOKEN42("TOKEN"),
    TOKEN43("TOKEN"),
    TOKEN44("TOKEN"),
    TOKEN45("TOKEN"),
    TOKEN46("TOKEN"),
    TOKEN47("TOKEN"),
    TOKEN48("TOKEN"),
    TOKEN49("TOKEN"),
    TOKEN50("TOKEN"),
    TOKEN51("TOKEN"),
    TOKEN52("TOKEN"),
    TOKEN53("TOKEN"),
    TOKEN54("TOKEN"),
    TOKEN55("TOKEN"),
    TOKEN56("TOKEN"),
    TOKEN57("TOKEN"),
    TOKEN58("TOKEN"),
    TOKEN59("TOKEN"),
    TOKEN60("TOKEN"),
    TOKEN61("TOKEN"),
    TOKEN62("TOKEN"),
    TOKEN63("TOKEN"),
    TOKEN64("TOKEN"),
    TOKEN65("TOKEN"),
    TOKEN66("TOKEN"),
    TOKEN67("TOKEN"),
    TOKEN68("TOKEN"),
    TOKEN69("TOKEN"),
    TOKEN70("TOKEN"),
    TOKEN71("TOKEN"),
    TOKEN72("TOKEN"),
    TOKEN73("TOKEN"),
    TOKEN74("TOKEN"),
    TOKEN75("TOKEN"),
    TOKEN76("TOKEN"),
    TOKEN77("TOKEN"),
    TOKEN78("TOKEN"),
    TOKEN79("TOKEN"),
    TOKEN80("TOKEN"),
    TOKEN81("TOKEN"),
    TOKEN82("TOKEN"),
    TOKEN83("TOKEN"),
    TOKEN84("TOKEN"),
    TOKEN85("TOKEN"),
    TOKEN86("TOKEN"),
    TOKEN87("TOKEN"),
    TOKEN88("TOKEN"),
    TOKEN89("TOKEN"),
    TOKEN90("TOKEN"),
    TOKEN91("TOKEN"),
    TOKEN92("TOKEN"),
    TOKEN93("TOKEN"),
    TOKEN94("TOKEN"),
    TOKEN95("TOKEN"),
    TOKEN96("TOKEN"),
    TOKEN97("TOKEN"),
    TOKEN98("TOKEN"),
    TOKEN99("TOKEN"),
    TOKEN100("TOKEN")
    ;
    private final String name;

    Token(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Token byName(String name) {
        for (Token t : values()) {
            if (t.getName().equals(name)) return t;
        }
        return null;
    }

    public static Token byID(int id) {
        return values()[id];
    }

    public int getID() {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].equals(this)) return i;
        }
        return -1;
    }
}
