package com.ampex.main.adx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DepthBook {

    ObservableList<BigInteger> priceList = FXCollections.observableList(new ArrayList<>());
    ObservableList<BigInteger> amountList = FXCollections.observableList(new ArrayList<>());
    ObservableList<BigInteger> totalList = FXCollections.observableList(new ArrayList<>());

    public synchronized void rebuild(List<Order> orders)
    {
        Iterator<Order> i = orders.iterator();
        priceList.clear();
        amountList.clear();
        totalList.clear();
        int index = 0;
        BigInteger total = BigInteger.ZERO;
        while(i.hasNext())
        {
            Order o = i.next();

            if(!priceList.isEmpty() && !priceList.get(index).equals(o.unitPrice()))
            {
                index++;
            }

            total = total.add(o.amountOnOffer());
            if(priceList.size() - 1 < index)
            {
                priceList.add(index,o.unitPrice());
                totalList.add(index,total);
            }else {
                priceList.set(index, o.unitPrice());
                totalList.set(index, total);
            }
            if(amountList.size() - 1 >= index)
            {
                amountList.set(index,amountList.get(index).add(o.amountOnOffer()));
            }else{
                amountList.add(index,o.amountOnOffer());
            }

        }
    }


    public ObservableList<BigInteger> getPriceList()
    {
        return priceList;
    }

    public ObservableList<BigInteger> getAmountList()
    {
        return amountList;
    }

    public ObservableList<BigInteger> getTotalList()
    {
        return totalList;
    }

    public synchronized BigInteger getTotal()
    {
        return totalList.get(totalList.size()-1);
    }


    public synchronized BigInteger getTotalAt(BigInteger unitPrice)
    {
        return totalList.get(priceList.indexOf(unitPrice));
    }

}
