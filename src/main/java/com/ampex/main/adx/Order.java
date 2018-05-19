package com.ampex.main.adx;

import amp.Amplet;
import amp.HeadlessPrefixedAmplet;
import amp.serialization.IAmpByteSerializable;
import com.ampex.amperabase.IAddress;
import com.ampex.main.data.EncryptionManager;
import com.ampex.main.data.Utils;
import com.ampex.main.transactions.addresses.Address;
import engine.binary.Binary;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class Order implements IAmpByteSerializable {
    private Pairs pair;
    private BigInteger unitPrice;
    private IAddress address;
    private IAddress contractAdd;
    private BigInteger amountOnOffer;
    private boolean buy;
    private BigInteger timestamp;
    private Binary bin;
    private OrderMeta om = new OrderMeta();
    private String txid;

    public Order(Pairs pair, BigInteger unitPrice, IAddress address, IAddress contractAdd, BigInteger amountOnOffer, Binary bin, boolean buy, String txid) throws InvalidOrderException {
        this(pair, unitPrice, address, contractAdd, amountOnOffer, bin, buy, BigInteger.valueOf(System.currentTimeMillis()), txid);
    }

    public Order(Pairs pair, BigInteger unitPrice, IAddress address, IAddress contractAdd, BigInteger amountOnOffer, Binary bin, boolean buy, BigInteger timestamp, String txid) throws InvalidOrderException {
        this.timestamp = timestamp;
        if (!contractAdd.isP2SH()) throw new InvalidOrderException("contract address is not P2SH");
        if (unitPrice.compareTo(BigInteger.ZERO) <= 0) throw new InvalidOrderException("Unit price <= 0");
        if (amountOnOffer.compareTo(BigInteger.ZERO) <= 0) throw new InvalidOrderException("amountOnOffer <= 0");
        try {
            Utils.fromBase64(txid);
        } catch (Exception e) {
            throw new InvalidOrderException("TXID not base 64");
        }
        this.txid = txid;
        this.bin = bin;
        this.pair = pair;
        this.unitPrice = unitPrice;
        this.address = address;
        this.amountOnOffer = amountOnOffer;
        this.buy = buy;
        this.contractAdd = contractAdd;
    }

    public String getTxid() {
        return txid;
    }

    public void match(long timestamp) {
        this.timestamp = BigInteger.valueOf(timestamp);
    }

    public void reduceAmount(BigInteger amount) {
        amountOnOffer = amountOnOffer.subtract(amount);
    }

    public BigInteger amountOnOffer() {
        return amountOnOffer;
    }

    public BigInteger unitPrice() {
        return unitPrice;
    }

    public Pairs pair() {
        return pair;
    }

    public IAddress address() {
        return address;
    }

    public boolean buy() {
        return buy;
    }

    public BigInteger timestamp() {
        return timestamp;
    }

    public IAddress contractAdd() {
        return contractAdd;
    }

    public Binary bin() {
        return bin;
    }

    public String getID() {
        return EncryptionManager.sha3256(pair.name() + Utils.toBase64(bin.serializeToAmplet().serializeToBytes()) + unitPrice + address.encodeForChain() + contractAdd.encodeForChain() + buy + txid);
    }

    public OrderMeta getOm() {
        return om;
    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.addBytes(bin.serializeToAmplet().serializeToBytes());

        hpa.addElement(pair.name());

        hpa.addElement(unitPrice);
        hpa.addBytes(address.toByteArray());
        hpa.addElement(amountOnOffer);
        hpa.addElement(buy);
        hpa.addBytes(contractAdd.toByteArray());
        hpa.addBytes(timestamp.toByteArray());
        hpa.addBytes(Utils.fromBase64(txid));
        return hpa.serializeToBytes();
    }

    public static Order fromByteArray(byte[] array) {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(array);
        Binary bin = Binary.deserializeFromAmplet(Amplet.create(hpa.getNextElement()));
        Pairs pair;
        try {
            pair = Pairs.valueOf(new String(hpa.getNextElement(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        BigInteger unitPrice = new BigInteger(hpa.getNextElement());
        IAddress address = Address.fromByteArray(hpa.getNextElement());
        BigInteger amountOnOffer = new BigInteger(hpa.getNextElement());
        boolean buy = hpa.getNextElementAsHeadlessAmplet().getNextBoolean();
        IAddress contractAdd = Address.fromByteArray(hpa.getNextElement());
        BigInteger timestamp = new BigInteger(hpa.getNextElement());
        String txid = Utils.toBase64(hpa.getNextElement());
        try {
            return new Order(pair, unitPrice, address, contractAdd, amountOnOffer, bin, buy, timestamp, txid);
        } catch (InvalidOrderException e) {
            e.printStackTrace();
            return null;
        }
    }
}
