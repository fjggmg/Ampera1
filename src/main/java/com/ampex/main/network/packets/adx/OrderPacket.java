package com.ampex.main.network.packets.adx;

import amp.ByteTools;
import amp.HeadlessPrefixedAmplet;
import com.ampex.main.IKi;
import com.ampex.main.adx.Order;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;
import com.ampex.main.network.packets.Packet;
import com.ampex.main.network.packets.PacketGlobal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;

public class OrderPacket implements Packet {
    public byte[] order;
    public String transaction;
    public boolean matched = false;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        //if (ki.getOptions().pDebug)
        //ki.debug("Received Order Packet");
        Order order = Order.fromByteArray(this.order);
        if (order == null) {
            ki.getMainLog().warn("Received order packet with null order");
            return;
        }

        if (transaction != null) {
            //ki.debug("Transaction not null, ID: " + transaction);
            boolean onChain = false;
            BigInteger amountExpected;
            if (order.buy())
                amountExpected = new BigDecimal(order.unitPrice().doubleValue() / 100_000_000).multiply(new BigDecimal(order.amountOnOffer().doubleValue())).toBigInteger();
            else
                amountExpected = order.amountOnOffer();

            if (!ki.getOptions().lite && ki.getTransMan().getAmountInWallet(order.contractAdd(), (order.buy()) ? order.pair().onOffer() : order.pair().accepting()).equals(amountExpected)) {
                onChain = true;
                //ki.debug("Order already on chain");
            } else {
                //ki.debug("Order not on chain, amount of: " + ((order.buy()) ? order.pair().onOffer() : order.pair().accepting()) + " in contract is: " + ki.getTransMan().getAmountInWallet(order.contractAdd(), (order.buy()) ? order.pair().onOffer() : order.pair().accepting()));
                //ki.debug("Amount expected from this contract: " + amountExpected);
            }
            if (matched) {
                //ki.debug("Matched order, ID: " + order.getID());
                if (!onChain) {
                    //trigger not needed for matched order
                    ki.getExMan().addMatchPending(transaction, order);
                } else
                    ki.getExMan().addMatched(order);
            } else {
                if (!onChain) {

                    if (order.buy() && !ki.getExMan().getOrderBook().sells().isEmpty()) {
                        if (ki.getExMan().getOrderBook().sells().get(0).unitPrice().compareTo(order.unitPrice()) <= 0) {
                            OrderRefused or = new OrderRefused();
                            or.ID = order.getID();
                            connMan.sendPacket(or);
                            return;
                        }
                    } else if (!ki.getExMan().getOrderBook().buys().isEmpty()) {
                        if (ki.getExMan().getOrderBook().buys().get(0).unitPrice().compareTo(order.unitPrice()) >= 0) {
                            OrderRefused or = new OrderRefused();
                            or.ID = order.getID();
                            connMan.sendPacket(or);
                            return;
                        }
                    }
                    OrderAccepted oa = new OrderAccepted();
                    oa.ID = order.getID();
                    connMan.sendPacket(oa);
                    ki.getExMan().addPending(transaction, order);
                } else
                    ki.getExMan().addOrder(order);
            }
            if (ki.getNetMan().isRelay()) {
                if (ki.getOptions().pDebug)
                    ki.debug("Broadcasting order packet");
                ki.getNetMan().broadcastAllBut(connMan.getID(), this);
            }
        }
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(serialized);
            order = hpa.getNextElement();
            matched = ByteTools.buildBoolean(hpa.getNextElement()[0]);
            if (hpa.hasNextElement()) {
                transaction = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
            }
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create OrderPacket from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.addBytes(order);
        hpa.addElement(matched);
        if (transaction != null)
            hpa.addElement(transaction);
        return hpa.serializeToBytes();
    }
}
