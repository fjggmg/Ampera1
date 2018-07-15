package com.ampex.main.adx;

import amp.Amplet;
import amp.HeadlessPrefixedAmplet;
import amp.serialization.IAmpByteSerializable;
import com.ampex.amperabase.IAddress;
import com.ampex.main.Ki;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.data.utils.Utils;
import com.ampex.main.transactions.addresses.Address;
import engine.binary.IBinary;
import engine.binary.on_ice.Binary;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

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
    private IBinary bin;
    private OrderMeta om = new OrderMeta();
    private String txid;
    private SimpleObjectProperty<BigInteger> amountProp;

    public Order(Pairs pair, BigInteger unitPrice, IAddress address, IAddress contractAdd, BigInteger amountOnOffer, IBinary bin, boolean buy, String txid) throws InvalidOrderException {
        this(pair, unitPrice, address, contractAdd, amountOnOffer, bin, buy, BigInteger.valueOf(System.currentTimeMillis()), txid);
    }

    public Order(Pairs pair, BigInteger unitPrice, IAddress address, IAddress contractAdd, BigInteger amountOnOffer, IBinary bin, boolean buy, BigInteger timestamp, String txid) throws InvalidOrderException {
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
        amountProp = new SimpleObjectProperty<>(this.amountOnOffer);

        this.buy = buy;
        this.contractAdd = contractAdd;
    }


    public SimpleObjectProperty<BigInteger> getAmountProp()
    {
        return amountProp;
    }
    public String getTxid() {
        return txid;
    }

    public void match(long timestamp) {
        this.timestamp = BigInteger.valueOf(timestamp);
    }

    public void reduceAmount(BigInteger amount) {
        amountOnOffer = amountOnOffer.subtract(amount);
        //using static reference to god object, need to fix this
        if(!Ki.getInstance().getOptions().nogui)
        try {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    amountProp.setValue(amountOnOffer);
                }
            });
        } catch (RuntimeException re)
        {
            throw re;
        } catch (Exception e)
        {
            //fail silently because FX is not available
        }

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

    public IBinary bin() {
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
        IBinary bin = Binary.deserializeFromAmplet(Amplet.create(hpa.getNextElement()));
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
