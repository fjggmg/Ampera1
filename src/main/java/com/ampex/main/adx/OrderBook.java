package com.ampex.main.adx;

import amp.HeadlessPrefixedAmplet;
import com.ampex.amperabase.IAddress;
import com.ampex.main.IKi;
import com.ampex.main.Ki;
import database.XodusAmpMap;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OrderBook {

    private volatile ObservableList<Order> buys = FXCollections.observableArrayList(new CopyOnWriteArrayList<>());
    private volatile ObservableList<Order> sells = FXCollections.observableArrayList(new CopyOnWriteArrayList<>());
    private volatile ObservableList<Order> matched = FXCollections.observableArrayList(new CopyOnWriteArrayList<>());
    private XodusAmpMap obMap = new XodusAmpMap("ob");
    private List<ExchangeData> data = new ArrayList<>();
    private List<String> matchedOrders = new ArrayList<>();
    private boolean firstRun = false;
    private volatile ObservableList<Order> active = FXCollections.observableArrayList(new CopyOnWriteArrayList<>());
    private IKi ki;
    private boolean sorted = false;
    public OrderBook(IKi ki) {
        this.ki = ki;
    }

    public void addOrder(Order order) {
        sorted = false;
        if(!ki.getOptions().nogui) {
            Platform.runLater(new Thread() {
                public void run() {
                    if (order.buy()) {
                        buys.add(order);
                    } else {
                        sells.add(order);
                    }
                    for (IAddress add : ki.getAddMan().getAll()) {
                        if (add.encodeForChain().equals(order.address().encodeForChain())) {
                            active.add(order);
                            break;
                        }
                    }
                }
            });
        }else{
            new Thread(() -> {
                if (order.buy()) {
                    buys.add(order);
                } else {
                    sells.add(order);
                }
                for (IAddress add : ki.getAddMan().getAll()) {
                    if (add.encodeForChain().equals(order.address().encodeForChain())) {
                        active.add(order);
                        break;
                    }
                }
            }).start();
        }
    }

    public void matchOrder(Order order) {
        sorted = false;
        if (!matchedOrders.contains(order.getID())) {
            Platform.runLater(new Thread() {
                public void run() {
                    order.match(System.currentTimeMillis());
                    if (order.buy()) {
                        buys.remove(order);

                    } else {
                        sells.remove(order);
                    }
                    matched.add(0, order);
                    for (IAddress add : ki.getAddMan().getAll()) {
                        if (add.encodeForChain().equals(order.address().encodeForChain())) {
                            active.remove(order);
                            break;
                        }
                    }
                }
            });
            addData(order.amountOnOffer(), order.unitPrice());
        }
    }

    public ObservableList<Order> buys() {
        return buys;
    }

    public ObservableList<Order> sells() {
        return sells;
    }

    public ObservableList<Order> matched() {
        return matched;
    }

    public ExchangeData getRecentData() {
        if (data.isEmpty()) return null;
        return data.get(data.size() - 1);
    }

    public boolean hasNew() {
        return hasNew;
    }

    private ExchangeData recent = null;
    private long newFrom = 0;
    public void setRecent(ExchangeData data) {
        recent = data;
    }

    public ExchangeData getRecent() {
        return recent;
    }

    public List<ExchangeData> collectRecentData(long timestamp) {
        hasNew = false;
        System.out.println("Current size of data: " + data.size());

        if (timestamp == 0) {
            List<ExchangeData> dataCopy = new ArrayList<>();
            dataCopy.addAll(data);
            return dataCopy;
        }
        for (int i = data.size() - 1; i >= 0; i--) {
            if (data.get(i).timestamp >= timestamp) {
                if (i == data.size() - 1) {
                    System.out.println("returning only one item from recent data");
                    List<ExchangeData> data = new ArrayList<>();
                    data.add(this.data.get(i));
                    return data;
                }
                System.out.println("returning: " + (data.size() - i) + " items");
                return data.subList(i, data.size() - 1);
            }
        }
        return data;
    }

    public long newFrom() {
        return newFrom;
    }

    private boolean hasNew = true;

    public boolean addData(BigInteger amount, BigInteger price, long timestamp) {
        if (data.isEmpty()) {
            hasNew = true;
            ExchangeData data = new ExchangeData(price, timestamp, 5);
            data.addData(price, amount);
            this.data.add(data);
        } else if (data.get(data.size() - 1).done(timestamp)) {


            ExchangeData ed = data.get(data.size() - 1).createNew();
            if (!hasNew) {
                hasNew = true;
                newFrom = timestamp;
            }
            data.add(ed);
            if (ed.done(timestamp)) {

                try {
                    addData(amount, price, timestamp);
                } catch (StackOverflowError e) {
                    //ki.getMainLog().warn("Stack overflow while adding data, caused by more than 2 weeks of no exchange data.: ");
                    return false;
                }
                if (ki.getGUIHook() != null)
                    ki.getGUIHook().dataAdded();
                return true;
            }
            ed.addData(price, amount);

        } else {
            data.get(data.size() - 1).addData(price, amount);
        }
        if (ki.getGUIHook() != null)
            ki.getGUIHook().dataAdded();
        return true;
    }

    public boolean isSorted() {
        return sorted;
    }
    public List<ExchangeData> getData() {
        return data;
    }

    public void run() {
        firstRun = true;
    }

    public boolean hasRun() {
        return firstRun;
    }

    public void addData(BigInteger amount, BigInteger price) {
        addData(amount, price, System.currentTimeMillis());
    }

    public void sort() {

        buys.sort(new OrderComparator(true));
        BigInteger totalB = BigInteger.ZERO;
        for (Order b : buys) {
            totalB = totalB.add(b.amountOnOffer());
            b.getOm().totalAtOrder = new BigInteger(totalB.toByteArray());
        }
        sells.sort(new OrderComparator(false));
        BigInteger totalS = BigInteger.ZERO;
        for (Order s : sells) {
            totalS = totalS.add(s.amountOnOffer());
            s.getOm().totalAtOrder = new BigInteger(totalS.toByteArray());
        }
        sorted = true;
        if (matched.size() > 10_000) {
            matched.remove(10_000, matched.size() - 1);
        }
    }

    public void close() {
        if (ki.getOptions().lite) return;
        obMap.clearSafe();
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        for (Order o : buys) {
            hpa.addBytes(o.serializeToBytes());
        }
        obMap.putBytes("buys", hpa.serializeToBytes());
        hpa = HeadlessPrefixedAmplet.create();
        for (Order o : sells) {
            hpa.addBytes(o.serializeToBytes());
        }
        obMap.putBytes("sells", hpa.serializeToBytes());
        hpa = HeadlessPrefixedAmplet.create();
        for (Order o : matched) {
            hpa.addBytes(o.serializeToBytes());
        }
        obMap.putBytes("matched", hpa.serializeToBytes());
        /*
        hpa = HeadlessPrefixedAmplet.create();
        for (Order o : active) {
            hpa.addBytes(o.serializeToBytes());
        }
        obMap.putBytes("active", hpa.serializeToBytes());
        */

        try {
            //Thread.sleep(60_000);
            obMap.close();
        } catch (Exception e) {
            Ki.getInstance().getMainLog().error("Error saving order book, order book data may still be in tact", e);
        }
    }

    public void load() {
        if (ki.getOptions().lite) return;
        if (obMap.getBytes("buys") != null) {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(obMap.getBytes("buys"));
            if (hpa != null && hpa.hasNextElement()) {
                while (hpa.hasNextElement()) {
                    Order order = Order.fromByteArray(hpa.getNextElement());
                    if (order == null) continue;
                    buys.add(order);
                    for (IAddress add : ki.getAddMan().getAll()) {
                        if (add.encodeForChain().equals(order.address().encodeForChain())) {
                            active.add(order);
                            break;
                        }
                    }
                }
            }
        }
        if (obMap.getBytes("sells") != null) {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(obMap.getBytes("sells"));
            if (hpa != null && hpa.hasNextElement()) {
                while (hpa.hasNextElement()) {
                    Order order = Order.fromByteArray(hpa.getNextElement());
                    if (order == null) continue;
                    sells.add(order);
                    for (IAddress add : ki.getAddMan().getAll()) {
                        if (add.encodeForChain().equals(order.address().encodeForChain())) {
                            active.add(order);
                            break;
                        }
                    }
                }
            }
        }
        if (obMap.getBytes("matched") != null) {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(obMap.getBytes("matched"));
            if (hpa != null && hpa.hasNextElement()) {
                while (hpa.hasNextElement()) {
                    matched.add(Order.fromByteArray(hpa.getNextElement()));
                }
                for (int i = matched.size() - 1; i >= 0; i--) {
                    Order o = matched.get(i);
                    matchedOrders.add(o.getID());
                    addData(o.amountOnOffer(), o.unitPrice(), o.timestamp().longValueExact());
                }
            }
        }
        /*
        if (obMap.getBytes("active") != null) {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(obMap.getBytes("active"));
            if (hpa != null && hpa.hasNextElement()) {
                while (hpa.hasNextElement()) {
                    active.add(Order.fromByteArray(hpa.getNextElement()));
                }
            }
        }
        */
        new Thread(() -> {
            if(!ki.getOptions().nogui) {
                if (data.size() > 0) {

                    while(ki.getGUIHook() == null){}

                    ki.getGUIHook().dataAdded();

                }
            }
        }).start();


    }

    public ObservableList<Order> active() {
        return active;
    }

    /**
     * mainly used for adding an "order" that's not really an order but a inverse of the recuction of another so we
     * can have it on recents
     *
     * @param order Order to add as matched
     */
    public void addMatched(Order order) {
        Platform.runLater(new Thread() {
            public void run() {
                matched.add(0, order);
                addData(order.amountOnOffer(), order.unitPrice());
            }
        });
    }

    public void removeOrder(Order o) {
        sorted = false;
        Platform.runLater(new Thread() {
            public void run() {
                if (o.buy()) {
                    buys.remove(o);
                } else {
                    sells.remove(o);
                }
                for (IAddress add : ki.getAddMan().getAll()) {
                    if (add.encodeForChain().equals(o.address().encodeForChain())) {
                        active.remove(o);
                        break;
                    }
                }
            }
        });

    }
}
