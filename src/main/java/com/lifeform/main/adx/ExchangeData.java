package com.lifeform.main.adx;

import com.lifeform.main.GUI.PriceAmtPair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ExchangeData {
    public BigInteger open;
    public BigInteger close;
    public BigInteger high;
    public BigInteger low;
    public long timestamp;
    public long endTimestamp;
    public BigInteger avg;
    private List<PriceAmtPair> data = new ArrayList<>();
    public int minutes;

    public ExchangeData(BigInteger open, long timestamp, int minutes) {
        this.open = open;
        this.low = open;
        this.high = open;
        this.close = open;
        this.avg = open;
        this.timestamp = timestamp;
        this.endTimestamp = timestamp + (60_000L * (long) minutes);
        this.minutes = minutes;
    }

    //private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public boolean done(long timestamp) {
        //System.out.println("Timestamp: " + sdf.format(new Date(timestamp)));
        //System.out.println("EndTimestamp: " + sdf.format(new Date(endTimestamp)));
        //System.out.println("isDone: " + (timestamp > endTimestamp));
        return timestamp > endTimestamp;
    }

    public void addData(BigInteger price, BigInteger amount) {
        addData(new PriceAmtPair(price, amount));
    }

    public void addData(PriceAmtPair pair) {
        if (pair.price.compareTo(low) < 0) {
            low = new BigInteger(pair.price.toByteArray());
        }
        if (pair.price.compareTo(high) > 0) {
            high = new BigInteger(pair.price.toByteArray());
        }
        close = new BigInteger(pair.price.toByteArray());
        data.add(pair);
        //TODO VWAP
    }

    public ExchangeData createNew() {
        return new ExchangeData(close, endTimestamp, minutes);
    }
}
