package com.ampex.main.adx;

import amp.ByteTools;
import com.ampex.amperabase.*;
import com.ampex.amperanet.packets.TransactionPacket;
import com.ampex.main.IKi;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.data.utils.Utils;
import com.ampex.main.network.packets.adx.OrderCancelled;
import com.ampex.main.network.packets.adx.OrderMatched;
import com.ampex.main.network.packets.adx.OrderPacket;
import com.ampex.main.network.packets.adx.OrderReduced;
import com.ampex.main.transactions.ITrans;
import com.ampex.main.transactions.Input;
import com.ampex.main.transactions.NewTrans;
import com.ampex.main.transactions.Output;
import com.ampex.main.transactions.addresses.InvalidAddressException;
import com.ampex.main.transactions.addresses.NewAdd;
import com.ampex.main.transactions.scripting.ScriptManager;
import engine.binary.on_ice.Binary;
import engine.data.DataElement;
import engine.data.constant_memory.on_ice.ConstantMemory;
import engine.data.jump_memory.on_ice.JumpMemory;
import engine.data.writable_memory.on_ice.WritableMemory;

import java.math.BigInteger;
import java.util.*;

public class ExchangeManager {
    private IKi ki;
    private OrderBook orderBook;
    private Map<String, Order> pending = new HashMap<>();
    private Map<String, Order> matchPending = new HashMap<>();
    private List<String> pendingUs = new ArrayList<>();
    private Map<String, ITrans> pendingAccept = new HashMap<>();

    public ExchangeManager(IKi ki) {
        this.ki = ki;
        orderBook = new OrderBook(ki);
        orderBook.load();
        for (Order o : orderBook.buys()) {
            orders.put(o.getID(), o);
        }
        for (Order o : orderBook.matched()) {
            orders.put(o.getID(), o);
        }
        for (Order o : orderBook.sells()) {
            orders.put(o.getID(), o);
        }
    }

    public OrderBook getOrderBook() {
        return orderBook;
    }

    public Map<String, Order> getPending() {
        return pending;
    }

    private Random entRand = new Random();

    public OrderStatus placeOrder(boolean buy, BigInteger amount, BigInteger stopPrice, Pairs pair, boolean limitBuy) {
        if (amount.compareTo(BigInteger.ZERO) <= 0 || stopPrice.compareTo(BigInteger.ZERO) <= 0) {
            ki.getMainLog().warn("Zero or Negative amount or price on placeOrder");
            return OrderStatus.GENERAL_FAILURE;
        }
        OrderStatus currentStatus = OrderStatus.COMPLETE;
        if (buy) {
            List<Order> toRemove = new ArrayList<>();
            for (Order o : orderBook.sells()) {
                System.out.println("Checking order: " + o.getID() + " for match");
                if (o.unitPrice().compareTo(stopPrice) > 0) {
                    break;
                }
                if (amount.compareTo(BigInteger.ZERO) <= 0) {
                    ki.getMainLog().warn("Amount went negative or 0 in search for match on placeOrder");
                    break;
                }
                System.out.println("Matched, building  buy transaction1");
                BigInteger amountBuying;
                if (amount.compareTo(o.amountOnOffer()) > 0) {
                    amountBuying = o.amountOnOffer();//.subtract(BigInteger.ONE);
                } else {
                    amountBuying = amount;
                }
                System.out.println("Matched, building  buy transaction2");

                List<IInput> inputs;// = new ArrayList<>();
                inputs = ki.getTransMan().getInputsForAmountAndToken(ki.getAddMan().getMainAdd(), amountBuying.multiply(o.unitPrice()).divide(BigInteger.valueOf(100_000_000)), o.pair().onOffer(), true);
                if (inputs == null) {
                    currentStatus = OrderStatus.BAD_UTXOS_US;
                    break;
                }
                List<IInput> ourInputs = new ArrayList<>();
                ourInputs.addAll(inputs);
                BigInteger buyingAmountOverage = BigInteger.ZERO;
                for (IInput i : inputs) {
                    buyingAmountOverage = buyingAmountOverage.add(i.getAmount());
                }
                ki.debug("Finalized inputs:");
                for (IInput i : inputs) {
                    ki.debug(i.getToken().getName() + " " + i.getAmount());
                }
                System.out.println("Matched, building  buy transaction3 buying: " + o.pair().accepting() + " with " + o.pair().onOffer());
                buyingAmountOverage = buyingAmountOverage.subtract(amountBuying.multiply(o.unitPrice()).divide(BigInteger.valueOf(100_000_000)));
                List<IInput> toUs = ki.getTransMan().getInputsForAmountAndToken(o.contractAdd(), amountBuying, o.pair().accepting(), true);
                if (toUs == null) {
                    if (!pendingUs.contains(o.getTxid())) {
                        if( !ki.getTransMan().hasUTXOsOnDisk(o.contractAdd()))
                        toRemove.add(o);
                        ki.getTransMan().unUseUTXOs(inputs);
                        currentStatus = OrderStatus.BAD_UTXOS_THEM;
                    }
                    continue;
                }
                amount = amount.subtract(amountBuying);
                BigInteger receivingAmountOverage = BigInteger.ZERO;
                for (IInput i : toUs) {
                    receivingAmountOverage = receivingAmountOverage.add(i.getAmount());
                }
                System.out.println("Matched, building  buy transaction4");
                receivingAmountOverage = receivingAmountOverage.subtract(amountBuying);
                inputs.addAll(toUs);
                List<IInput> feeIn = ki.getTransMan().getInputsForAmountAndToken(ki.getAddMan().getMainAdd(), BigInteger.valueOf(1_000_00), Token.ORIGIN, true);
                if (feeIn == null) {
                    currentStatus = OrderStatus.NOT_ENOUGH_FOR_FEE;
                    break;
                }
                BigInteger feeOverage = BigInteger.ZERO;
                for (IInput i : feeIn) {
                    feeOverage = feeOverage.add(i.getAmount());
                }
                System.out.println("Matched, building  buy transaction5");
                feeOverage = feeOverage.subtract(BigInteger.valueOf(1_000_00));
                inputs.addAll(feeIn);
                ourInputs.addAll(feeIn);
                Output btcPaying = new Output(amountBuying.multiply(o.unitPrice()).divide(BigInteger.valueOf(100_000_000)), o.address(), o.pair().onOffer(), 0, System.currentTimeMillis(), Output.VERSION);
                Output originToUs = new Output(amountBuying, ki.getAddMan().getMainAdd(), o.pair().accepting(), 1, System.currentTimeMillis(), Output.VERSION);
                Output btcChange = new Output(buyingAmountOverage, ki.getAddMan().getMainAdd(), o.pair().onOffer(), 2, System.currentTimeMillis(), Output.VERSION);
                Output originChange = new Output(receivingAmountOverage, o.contractAdd(), o.pair().accepting(), 3, System.currentTimeMillis(), Output.VERSION);
                Output feeBack = new Output(feeOverage, ki.getAddMan().getMainAdd(), Token.ORIGIN, 4, System.currentTimeMillis(), Output.VERSION);
                Map<String, IKSEP> keySigMap = new HashMap<>();
                List<String> associatedUs = new ArrayList<>();
                for (IInput i : ourInputs) {
                    associatedUs.add(i.getID());
                }
                System.out.println("Matched, building  buy transaction6");
                IKSEP ksepUs = new KeySigEntropyPair(null, ki.getAddMan().getEntropyForAdd(ki.getAddMan().getMainAdd()), associatedUs, ki.getAddMan().getMainAdd().getPrefix(), ki.getAddMan().getMainAdd().isP2SH(), ki.getAddMan().getMainAdd().getKeyType());
                keySigMap.put(ki.getEncryptMan().getPublicKeyString(ki.getAddMan().getMainAdd().getKeyType()), ksepUs);
                List<String> associatedThem = new ArrayList<>();
                for (IInput i : toUs) {
                    associatedThem.add(i.getID());
                }
                System.out.println("Matched, building  buy transaction7");
                IKSEP ksepThem = new KeySigEntropyPair(Utils.toBase64(new WritableMemory().serializeToBytes()), Utils.toBase64(o.bin().getEntropy()), associatedThem, o.contractAdd().getPrefix(), true, KeyType.NONE);
                keySigMap.put(Utils.toBase64(o.bin().serializeToAmplet().serializeToBytes()), ksepThem);
                List<IOutput> outputs = new ArrayList<>();
                outputs.add(btcPaying);
                outputs.add(originToUs);
                outputs.add(btcChange);
                outputs.add(originChange);
                outputs.add(feeBack);
                ki.debug("Finalized outputs:");
                for (IOutput i : outputs) {
                    ki.debug(i.getToken().getName() + " " + i.getAmount());
                }
                System.out.println("Matched, building  buy transaction8");
                try {
                    ITrans trans = new NewTrans("ADX transaction", outputs, inputs, keySigMap, TransactionType.NEW_TRANS);
                    trans.addSig(ki.getEncryptMan().getPublicKeyString(ki.getAddMan().getMainAdd().getKeyType()), Utils.toBase64(ki.getEncryptMan().sign(trans.toSignBytes(), ki.getAddMan().getMainAdd().getKeyType())));
                    if (!ki.getTransMan().verifyTransaction(trans)) {
                        currentStatus = OrderStatus.TRANSACTION_FAILURE;
                        break;
                    }
                    ki.getTransMan().getPending().add(trans);
                    TransactionPacket tp = new TransactionPacket();
                    tp.trans = trans.serializeToAmplet().serializeToBytes();

                    ki.getNetMan().broadcast(tp);
                    if (amountBuying.equals(o.amountOnOffer())) {
                        OrderMatched om = new OrderMatched();
                        om.ID = o.getID();
                        ki.getNetMan().broadcast(om);
                        //matchOrder(o.getID());
                        toRemove.add(o);
                    } else {
                        OrderReduced or = new OrderReduced();
                        or.amount = amountBuying;
                        or.ID = o.getID();
                        or.transaction = trans.getID();
                        ki.getNetMan().broadcast(or);
                        reduceOrder(o.getID(), amountBuying, trans.getID());
                    }
                    System.out.println("Matched, building  buy transaction9");
                } catch (InvalidTransactionException e) {

                    ki.getMainLog().error("Error creating transaction to fund contract", e);
                    currentStatus = OrderStatus.TRANSACTION_FAILURE;
                }

            }
            for (Order o : toRemove) {
                matchOrder(o.getID());
            }
            if (!currentStatus.succeeded()) {
                return currentStatus;
            }


            if (amount.compareTo(BigInteger.ZERO) > 0 && limitBuy) {
                short[] jumps = new short[16];
                jumps[0] = ScriptManager.GEN_TRADE_FAIL_JUMP;
                jumps[1] = ScriptManager.GEN_TRADE_CANCEL_JUMP;
                jumps[2] = ScriptManager.GEN_TRADE_CANCEL_FAIL_JUMP;
                DataElement[] constants = new DataElement[32];
                ConstantMemory constMem;
                JumpMemory jMem;
                try {
                    //stopPrice = ((BigInteger.valueOf(100_000_000).multiply(BigInteger.valueOf(100_000_000))).divide(stopPrice));
                    //constants[0] = new DataElement(stopPrice.toByteArray());
                    //System.out.println("Ratio:" + new BigDecimal(stopPrice).divide(BigDecimal.valueOf(100_000_000),5, RoundingMode.HALF_DOWN));
                    constants[0] = new DataElement(stopPrice.multiply(amount).divide(BigInteger.valueOf(100_000_000)).toByteArray());
                    constants[1] = new DataElement(ByteTools.deconstructInt(pair.onOffer().getID()));
                    constants[2] = new DataElement(ByteTools.deconstructInt(pair.accepting().getID()));
                    constants[3] = new DataElement(ki.getAddMan().getMainAdd().toByteArray());
                    constants[4] = new DataElement(amount.toByteArray());
                    constMem = new ConstantMemory(constants);
                    jMem = new JumpMemory(jumps);
                } catch (Exception e) {
                    e.printStackTrace();
                    return OrderStatus.GENERAL_FAILURE;
                }
                byte[] ent = new byte[64];
                entRand.nextBytes(ent);
                String entropy = Utils.toBase64(ent);
                Binary program = null;
                try {
                    program = new Binary(ki.getScriptMan().genericTrade(), constMem, jMem, true, ScriptManager.VERSION, ent, System.currentTimeMillis(), Utils.fromBase64(ki.getEncryptMan().getPublicKeyString(ki.getAddMan().getMainAdd().getKeyType())), ki.getAddMan().getMainAdd().getKeyType(), null, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    return OrderStatus.GENERAL_FAILURE;
                }
                IAddress contractAdd;
                try {
                    contractAdd = NewAdd.createNew(Utils.toBase64(program.serializeToAmplet().serializeToBytes()), entropy, AddressLength.SHA256, true, KeyType.NONE);
                } catch (InvalidAddressException e) {
                    e.printStackTrace();
                    return OrderStatus.GENERAL_FAILURE;
                }
                ITrans funding;
                try {
                    funding = ki.getTransMan().createSimple(contractAdd, amount.multiply(stopPrice).divide(BigInteger.valueOf(100_000_000)), BigInteger.TEN, pair.onOffer(), "funding ADX Buy contract", 20, ki.getAddMan().getMainAdd());
                } catch (InvalidTransactionException e) {

                    ki.getMainLog().error("Error creating transaction to fund contract", e);
                    return OrderStatus.GENERAL_FAILURE;
                }
                submitOrder(funding, program, true, amount, stopPrice, contractAdd, ki.getAddMan().getMainAdd(), pair);
            }
            return currentStatus;
        } else {
            List<Order> toRemove = new ArrayList<>();
            for (Order o : orderBook.buys()) {
                System.out.println("Checking order: " + o.getID() + " for match");
                if (o.unitPrice().compareTo(stopPrice) < 0) {
                    break;
                }
                if (amount.compareTo(BigInteger.ZERO) <= 0) {
                    ki.getMainLog().warn("Amount went negative or 0 in search for match on placeOrder");
                    break;
                }
                System.out.println("Matched, building transaction1");
                BigInteger amountSelling;
                if (amount.compareTo(o.amountOnOffer()) > 0) {
                    amountSelling = o.amountOnOffer();//.subtract(BigInteger.ONE);
                } else {
                    amountSelling = amount;
                }
                System.out.println("Matched, building transaction2");

                List<IInput> inputs;// = new ArrayList<>();
                inputs = ki.getTransMan().getInputsForAmountAndToken(ki.getAddMan().getMainAdd(), amountSelling, o.pair().accepting(), true);
                if (inputs == null) {
                    currentStatus = OrderStatus.BAD_UTXOS_US;
                    break;
                }
                List<IInput> ourInputs = new ArrayList<>();
                ourInputs.addAll(inputs);
                BigInteger buyingAmountOverage = BigInteger.ZERO;
                System.out.println("Matched, building transaction3");
                for (IInput i : inputs) {
                    buyingAmountOverage = buyingAmountOverage.add(i.getAmount());
                }
                System.out.println("Matched, building transaction4");
                buyingAmountOverage = buyingAmountOverage.subtract(amountSelling);
                System.out.println("Searching: " + o.contractAdd().encodeForChain() + " for inputs, this should match the address that was funded");
                System.out.println("Other addresses invovled:");
                System.out.println("Your add: " + ki.getAddMan().getMainAdd().encodeForChain());
                System.out.println("Other add: " + o.address().encodeForChain());
                List<IInput> toUs = ki.getTransMan().getInputsForAmountAndToken(o.contractAdd(), amountSelling.multiply(o.unitPrice()).divide(BigInteger.valueOf(100_000_000)), o.pair().onOffer(), true);
                if (toUs == null) {
                    if (!pendingUs.contains(o.getTxid())) {
                        if( !ki.getTransMan().hasUTXOsOnDisk(o.contractAdd()))
                        toRemove.add(o);
                        ki.getTransMan().unUseUTXOs(inputs);
                        currentStatus = OrderStatus.BAD_UTXOS_THEM;
                    }

                    continue;
                }
                amount = amount.subtract(amountSelling);
                BigInteger receivingAmountOverage = BigInteger.ZERO;
                System.out.println("Matched, building transaction5");
                for (IInput i : toUs) {
                    receivingAmountOverage = receivingAmountOverage.add(i.getAmount());
                }
                System.out.println("Matched, building transaction6");
                receivingAmountOverage = receivingAmountOverage.subtract(amountSelling.multiply(o.unitPrice()).divide(BigInteger.valueOf(100_000_000)));
                inputs.addAll(toUs);
                List<IInput> feeIn = ki.getTransMan().getInputsForAmountAndToken(ki.getAddMan().getMainAdd(), BigInteger.valueOf(1_000_00), Token.ORIGIN, true);

                BigInteger feeOverage = BigInteger.ZERO;
                if(o.pair().accepting().equals(Token.ORIGIN) && buyingAmountOverage.compareTo(BigInteger.valueOf(1_000_000)) < 0) {
                    if (feeIn == null) {
                        currentStatus = OrderStatus.NOT_ENOUGH_FOR_FEE;
                        break;
                    }
                    for (IInput i : feeIn) {
                        feeOverage = feeOverage.add(i.getAmount());
                    }

                    feeOverage = feeOverage.subtract(BigInteger.valueOf(1_000_00));
                }else{
                    buyingAmountOverage = buyingAmountOverage.subtract(BigInteger.valueOf(1_000_000));
                }
                System.out.println("Matched, building transaction7");
                inputs.addAll(feeIn);
                ourInputs.addAll(feeIn);
                System.out.println("All inputs: ");
                for (IInput i : inputs) {
                    System.out.println(i.getToken().getName() + " " + i.getAmount());
                }
                Output originOut = new Output(amountSelling, o.address(), o.pair().accepting(), 0, System.currentTimeMillis(), Output.VERSION);
                Output btcOut = new Output(amountSelling.multiply(o.unitPrice()).divide(BigInteger.valueOf(100_000_000)), ki.getAddMan().getMainAdd(), o.pair().onOffer(), 1, System.currentTimeMillis(), Output.VERSION);
                Output originBack = new Output(buyingAmountOverage, ki.getAddMan().getMainAdd(), o.pair().accepting(), 2, System.currentTimeMillis(), Output.VERSION);
                Output btcBack = new Output(receivingAmountOverage, o.contractAdd(), o.pair().onOffer(), 3, System.currentTimeMillis(), Output.VERSION);
                Output feeBack = new Output(feeOverage, ki.getAddMan().getMainAdd(), Token.ORIGIN, 4, System.currentTimeMillis(), Output.VERSION);
                Map<String, IKSEP> keySigMap = new HashMap<>();
                List<String> associatedUs = new ArrayList<>();
                System.out.println("Matched, building transaction8");
                for (IInput i : ourInputs) {
                    associatedUs.add(i.getID());
                }
                IKSEP ksepUs = new KeySigEntropyPair(null, ki.getAddMan().getEntropyForAdd(ki.getAddMan().getMainAdd()), associatedUs, ki.getAddMan().getMainAdd().getPrefix(), ki.getAddMan().getMainAdd().isP2SH(), ki.getAddMan().getMainAdd().getKeyType());
                keySigMap.put(ki.getEncryptMan().getPublicKeyString(ki.getAddMan().getMainAdd().getKeyType()), ksepUs);
                List<String> associatedThem = new ArrayList<>();
                for (IInput i : toUs) {
                    associatedThem.add(i.getID());
                }
                System.out.println("Matched, building transaction9");
                IKSEP ksepThem = new KeySigEntropyPair(Utils.toBase64(new WritableMemory().serializeToBytes()), Utils.toBase64(o.bin().getEntropy()), associatedThem, o.contractAdd().getPrefix(), true, KeyType.NONE);
                keySigMap.put(Utils.toBase64(o.bin().serializeToAmplet().serializeToBytes()), ksepThem);
                List<IOutput> outputs = new ArrayList<>();
                outputs.add(originOut);
                outputs.add(btcOut);
                outputs.add(originBack);
                outputs.add(btcBack);
                outputs.add(feeBack);
                System.out.println("Final outputs: ");
                for (IOutput out : outputs) {
                    System.out.println(out.getToken() + " " + out.getAmount());
                }
                try {
                    ITrans trans = new NewTrans("ADX transaction", outputs, inputs, keySigMap, TransactionType.NEW_TRANS);
                    trans.addSig(ki.getEncryptMan().getPublicKeyString(ki.getAddMan().getMainAdd().getKeyType()), Utils.toBase64(ki.getEncryptMan().sign(trans.toSignBytes(), ki.getAddMan().getMainAdd().getKeyType())));
                    if (!ki.getTransMan().verifyTransaction(trans)) {
                        currentStatus = OrderStatus.TRANSACTION_FAILURE;
                        break;
                    }
                    TransactionPacket tp = new TransactionPacket();

                    tp.trans = trans.serializeToAmplet().serializeToBytes();
                    ki.getTransMan().getPending().add(trans);
                    ki.getNetMan().broadcast(tp);
                    if (amountSelling.equals(o.amountOnOffer())) {
                        OrderMatched om = new OrderMatched();
                        om.ID = o.getID();
                        ki.getNetMan().broadcast(om);
                        //matchOrder(o.getID());
                        toRemove.add(o);
                    } else {
                        reduceOrder(o.getID(), amountSelling, trans.getID());
                        OrderReduced or = new OrderReduced();
                        or.transaction = trans.getID();
                        or.amount = amountSelling;
                        or.ID = o.getID();
                        ki.getNetMan().broadcast(or);
                    }

                } catch (InvalidTransactionException e) {
                    ki.getMainLog().error("Error creating transaction to fund contract", e);
                    currentStatus = OrderStatus.TRANSACTION_FAILURE;
                    break;
                }
                System.out.println("Matched, building transaction10");

            }
            for (Order o : toRemove) {
                matchOrder(o.getID());
            }
            if (!currentStatus.succeeded()) {
                return currentStatus;
            }
            System.out.println("Removed: " + toRemove.size() + " orders from orderbook");
            if (amount.compareTo(BigInteger.ZERO) > 0 && limitBuy) {
                short[] jumps = new short[16];
                jumps[0] = ScriptManager.GEN_TRADE_FAIL_JUMP;
                jumps[1] = ScriptManager.GEN_TRADE_CANCEL_JUMP;
                jumps[2] = ScriptManager.GEN_TRADE_CANCEL_FAIL_JUMP;
                DataElement[] constants = new DataElement[32];
                ConstantMemory constMem;
                JumpMemory jMem;
                try {
                    constants[4] = new DataElement(stopPrice.multiply(amount).divide(BigInteger.valueOf(100_000_000)).toByteArray());
                    constants[1] = new DataElement(ByteTools.deconstructInt(pair.accepting().getID()));
                    constants[2] = new DataElement(ByteTools.deconstructInt(pair.onOffer().getID()));
                    constants[3] = new DataElement(ki.getAddMan().getMainAdd().toByteArray());
                    constants[0] = new DataElement(amount.toByteArray());
                    constMem = new ConstantMemory(constants);
                    jMem = new JumpMemory(jumps);
                } catch (Exception e) {
                    e.printStackTrace();
                    return OrderStatus.GENERAL_FAILURE;
                }
                byte[] ent = new byte[64];
                entRand.nextBytes(ent);
                String entropy = Utils.toBase64(ent);
                Binary program = null;
                try {
                    program = new Binary(ki.getScriptMan().genericTrade(), constMem, jMem, true, ScriptManager.VERSION, ent, System.currentTimeMillis(), Utils.fromBase64(ki.getEncryptMan().getPublicKeyString(ki.getAddMan().getMainAdd().getKeyType())), ki.getAddMan().getMainAdd().getKeyType(), null, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    return OrderStatus.GENERAL_FAILURE;
                }
                IAddress contractAdd;
                try {
                    contractAdd = NewAdd.createNew(Utils.toBase64(program.serializeToAmplet().serializeToBytes()), entropy, AddressLength.SHA256, true, KeyType.NONE);
                } catch (InvalidAddressException e) {
                    e.printStackTrace();
                    return OrderStatus.GENERAL_FAILURE;
                }
                ITrans funding;
                try {
                    funding = ki.getTransMan().createSimple(contractAdd, amount, BigInteger.TEN, pair.accepting(), "funding ADX Sell contract", 20, ki.getAddMan().getMainAdd());
                } catch (InvalidTransactionException e) {
                    e.printStackTrace();
                    return OrderStatus.GENERAL_FAILURE;
                }
                submitOrder(funding, program, false, amount, stopPrice, contractAdd, ki.getAddMan().getMainAdd(), pair);

            }
            return currentStatus;
        }
    }

    public void submitOrder(ITrans funding, Binary program, boolean buy, BigInteger amountOnOffer, BigInteger unitPrice, IAddress contractAdd, IAddress payTo, Pairs pair) {
        try {
            if (!ki.getTransMan().verifyTransaction(funding)) return;
            Order order = new Order(pair, unitPrice, payTo, contractAdd, amountOnOffer, program, buy, funding.getID());
            addOrder(order);

            //ki.getTransMan().getPending().add(funding);
            pendingAccept.put(order.getID(), funding);
            OrderPacket op = new OrderPacket();
            pendingUs.add(order.getTxid());
            op.order = order.serializeToBytes();
            op.transaction = funding.getID();
            ki.getNetMan().broadcast(op);
        } catch (InvalidOrderException e) {
            e.printStackTrace();
        }
    }

    Map<String, Order> orders = new HashMap<>();

    public void addOrder(Order order) {
        if (orders.keySet().contains(order.getID())) return;
        ki.debug("Adding order: " + order.getID());
        orderBook.addOrder(order);
        ki.debug("Added to Orderbook");

        orders.put(order.getID(), order);
    }

    public Order getOrder(String ID) {
        return orders.get(ID);
    }

    public Set<String> getOrderIDs() {
        return orders.keySet();
    }

    public void removeOrder(String ID) {
        orderBook.removeOrder(orders.remove(ID));
    }

    public void matchOrder(String ID) {
        Order o = orders.get(ID);
        if (o != null) {
            orderBook.matchOrder(o);
            //orderBook.addData(o.amountOnOffer(),o.unitPrice());
        }
    }

    public void rebuildOrderCache()
    {
        ki.getMainLog().warn("Rebuilding Order cache, ours became desynced");
        for(Order o:orderBook.sells())
        {
            orders.put(o.getID(),o);
        }
        for(Order o:orderBook.buys())
        {
            orders.put(o.getID(),o);
        }
        for(Order o:orderBook.matched())
        {
            orders.put(o.getID(),o);
        }
    }

    public void reduceOrder(String ID, BigInteger amount, String txid) {
        Order o = orders.get(ID);
        if(o == null) rebuildOrderCache();
        o = orders.get(ID);
        if(o == null){
            ki.getMainLog().warn("Unable to reduce order: " + ID + " unable to find in order book");
            return;
        }
        o.reduceAmount(amount);
        orderBook.sort();
        try {
            orderBook.addMatched(new Order(o.pair(), o.unitPrice(), o.address(), o.contractAdd(), amount, o.bin(), o.buy(), txid));
        } catch (InvalidOrderException e) {
            e.printStackTrace();
        }

    }

    public void close() {
        orderBook.close();
    }

    public boolean cancelOrder(Order o) {
        for (IAddress a : ki.getAddMan().getAll()) {
            ki.debug("Address for cancel: " + a.encodeForChain());
            if (a.encodeForChain().equals(o.address().encodeForChain())) {
                ki.debug("Address is one of ours");
                List<IOutput> utxos = ki.getTransMan().getUTXOs(o.contractAdd(), true);
                if (utxos != null && !utxos.isEmpty()) {
                    BigInteger amountRecovered = BigInteger.ZERO;
                    Token t = utxos.get(0).getToken();
                    List<IInput> cIns = new ArrayList<>();
                    for (IOutput out : utxos) {
                        amountRecovered = amountRecovered.add(out.getAmount());
                        cIns.add(Input.fromOutput(out));
                    }
                    Output out = new Output(amountRecovered, o.address(), t, 0, System.currentTimeMillis(), Output.VERSION);
                    List<IInput> inputs = ki.getTransMan().getInputsForAmountAndToken(o.address(), BigInteger.valueOf(1_000), Token.ORIGIN, true);
                    if (inputs == null) {
                        ki.getMainLog().warn("Unable to recover funds from cancelled trade, not enough origin to pay the transaction fee");
                        return false;
                    }
                    BigInteger feeOverage = BigInteger.ZERO;
                    for (IInput i : inputs) {
                        feeOverage = feeOverage.add(i.getAmount());
                    }
                    feeOverage = feeOverage.subtract(BigInteger.valueOf(1_000));
                    Output feeReturn = new Output(feeOverage, o.address(), Token.ORIGIN, 1, System.currentTimeMillis(), Output.VERSION);
                    List<String> associatedFee = new ArrayList<>();
                    for (IInput i : inputs) {
                        associatedFee.add(i.getID());
                    }
                    inputs.addAll(cIns);
                    List<IOutput> outputs = new ArrayList<>();
                    outputs.add(out);
                    outputs.add(feeReturn);
                    Map<String, IKSEP> keySigMap = new HashMap<>();
                    List<String> associatedContract = new ArrayList<>();
                    for (IInput i : cIns) {
                        associatedContract.add(i.getID());
                    }
                    IKSEP cKSEP = new KeySigEntropyPair(null, Utils.toBase64(o.bin().getEntropy()), associatedContract, o.contractAdd().getPrefix(), true, KeyType.NONE);
                    keySigMap.put(Utils.toBase64(o.bin().serializeToAmplet().serializeToBytes()), cKSEP);
                    IKSEP fKESP = new KeySigEntropyPair(null, ki.getAddMan().getEntropyForAdd(o.address()), associatedFee, o.address().getPrefix(), o.address().isP2SH(), ki.getAddMan().getMainAdd().getKeyType());
                    keySigMap.put(ki.getEncryptMan().getPublicKeyString(ki.getAddMan().getMainAdd().getKeyType()), fKESP);
                    try {
                        ITrans trans = new NewTrans("Cancelled ADX order", outputs, inputs, keySigMap, TransactionType.NEW_TRANS);
                        WritableMemory wm = new WritableMemory();
                        wm.setElement(ki.getEncryptMan().sign(trans.toSignBytes(), ki.getAddMan().getMainAdd().getKeyType()), 0);
                        //wm.setElement(new DataElement(new byte[]{o.address().getKeyType().getValue()}), 1);
                        trans.addSig(Utils.toBase64(o.bin().serializeToAmplet().serializeToBytes()), Utils.toBase64(wm.serializeToBytes()));
                        trans.addSig(ki.getEncryptMan().getPublicKeyString(ki.getAddMan().getMainAdd().getKeyType()), Utils.toBase64(ki.getEncryptMan().sign(trans.toSignBytes(), ki.getAddMan().getMainAdd().getKeyType())));
                        TransactionPacket tp = new TransactionPacket();
                        if (ki.getTransMan().verifyTransaction(trans)) {
                            ki.getTransMan().getPending().add(trans);
                            tp.trans = trans.serializeToAmplet().serializeToBytes();
                            ki.getNetMan().broadcast(tp);
                            OrderCancelled oc = new OrderCancelled();
                            oc.ID = o.getID();
                            oc.sig = ki.getEncryptMan().sign(o.serializeToBytes(), ki.getAddMan().getMainAdd().getKeyType());
                            ki.getNetMan().broadcast(oc);
                            orderBook.removeOrder(o);
                            return true;
                        } else {
                            ki.getMainLog().warn("Cancellation transaction failed! Funds were unable to be recovered. Report this immediately with the debug that came before this.");
                            return false;
                        }
                    } catch (InvalidTransactionException e) {
                        ki.getMainLog().error("Problem creating transaction for order cancellation", e);
                        return false;
                    } catch (Exception e) {
                        ki.getMainLog().error("Problem adding signature to writable memory in order cancellation", e);
                        return false;
                    }

                } else {
                    ki.getMainLog().warn("Unable to cancel transaction because the contract is not funded");
                    return false;
                }
            }
        }
        ki.getMainLog().warn("Order does not include one of your addresses, cannot cancel. Address on order: " + o.address().encodeForChain());
        return false;
    }

    public void cancelRequest(String ID, byte[] sig) {
        Order o = orders.get(ID);
        if (o != null) {
            if (EncryptionManager.verifySig(o.serializeToBytes(), sig, Utils.toBase64(o.bin().getPublicKey()), o.address().getKeyType())) {
                orderBook.removeOrder(o);
                orders.remove(ID);
            }
        }
    }

    public void addPending(String txID, Order order) {
        oIDtoTXID.put(order.getID(), txID);
        pending.put(txID, order);
    }

    private Map<String, String> oIDtoTXID = new HashMap<>();

    public void transactionProccessed(String txid) {
        if (pending.get(txid) != null) {
            addOrder(pending.remove(txid));
        }
        if (matchPending.get(txid) != null) {
            addMatched(matchPending.get(txid));
        }
        pendingUs.remove(txid);
    }

    public void addMatchPending(String txID, Order order) {
        matchPending.put(txID, order);
    }

    /**
     * mostly for network use, adds an order that may be an inverse of a previous reduction
     *
     * @param o order to add as matched
     */
    public void addMatched(Order o) {
        if (orders.keySet().contains(o.getID())) return;
        //keep track of ID in next line since this is mostly used on from network stuff
        orders.put(o.getID(), o);
        orderBook.addMatched(o);

    }

    public String txIDforOrderID(String oID) {
        //this is a double check because it may be slightly faster
        if (oIDtoTXID.get(oID) != null)
            return oIDtoTXID.get(oID);
        return orders.get(oID).getTxid();
    }

    public void addTXPendingAccept(String oID, ITrans t) {
        pendingAccept.put(oID, t);
    }

    public void orderRejected(String ID) {
        if (pendingAccept.get(ID) != null) {
            ITrans t = pendingAccept.remove(ID);
            ki.getTransMan().unUseUTXOs(t.getInputs());
        }
    }

    public void orderAccepted(String ID) {
        if (pendingAccept.get(ID) != null) {
            TransactionPacket tp = new TransactionPacket();
            ITrans trans = pendingAccept.remove(ID);
            tp.trans = trans.serializeToAmplet().serializeToBytes();
            ki.getTransMan().getPending().add(trans);
            ki.getNetMan().broadcast(tp);
        }
    }

}
