package com.ampex.main.network.packets.adx;

import amp.ByteTools;
import amp.HeadlessPrefixedAmplet;
import com.ampex.amperabase.IConnectionManager;
import com.ampex.amperabase.IKiAPI;
import com.ampex.amperabase.InvalidAmpBuildException;
import com.ampex.amperanet.packets.Packet;
import com.ampex.amperanet.packets.PacketGlobal;
import com.ampex.main.IKi;
import com.ampex.main.adx.Order;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;

public class OrderPacket implements Packet {
    public byte[] order;
    public String transaction;
    public boolean matched = false;

    @Override
    public void process(IKiAPI ki, IConnectionManager connMan, PacketGlobal pg) {
        //if (ki.getOptions().pDebug)
        //ki.debug("Received Order Packet");
        Order order = Order.fromByteArray(this.order);
        if (order == null) {
            ((IKi) ki).getMainLog().warn("Received order packet with null order");
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
                    ((IKi) ki).getExMan().addMatchPending(transaction, order);
                } else
                    ((IKi) ki).getExMan().addMatched(order);
            } else {
                if (!onChain) {

                    if (order.buy() && !((IKi) ki).getExMan().getOrderBook().sells().isEmpty()) {
                        if (((IKi) ki).getExMan().getOrderBook().sells().get(0).unitPrice().compareTo(order.unitPrice()) <= 0) {
                            OrderRefused or = new OrderRefused();
                            or.ID = order.getID();
                            connMan.sendPacket(or);
                            return;
                        }
                    } else if (!((IKi) ki).getExMan().getOrderBook().buys().isEmpty()) {
                        if (((IKi) ki).getExMan().getOrderBook().buys().get(0).unitPrice().compareTo(order.unitPrice()) >= 0) {
                            OrderRefused or = new OrderRefused();
                            or.ID = order.getID();
                            connMan.sendPacket(or);
                            return;
                        }
                    }
                    OrderAccepted oa = new OrderAccepted();
                    oa.ID = order.getID();
                    connMan.sendPacket(oa);
                    ((IKi) ki).getExMan().addPending(transaction, order);
                } else
                    ((IKi) ki).getExMan().addOrder(order);
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
