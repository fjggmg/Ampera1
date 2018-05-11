package com.lifeform.main.adx;

import java.io.Serializable;
import java.util.Comparator;

public class OrderComparator implements Comparator<Order>, Serializable {
    private boolean buy;
    private static final long serialVersionUID = 184L;
    public OrderComparator(boolean buy) {
        this.buy = buy;
    }

    @Override
    public int compare(Order a, Order b) {
        if (buy) {
            int f = b.unitPrice().compareTo(a.unitPrice());
            return (f != 0) ? f : a.timestamp().compareTo(b.timestamp());
        } else {
            int f = a.unitPrice().compareTo(b.unitPrice());
            return (f != 0) ? f : a.timestamp().compareTo(b.timestamp());
        }
    }
}
