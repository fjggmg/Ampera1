package com.ampex.main.data.buckets;

import amp.Amplet;
import amp.HeadlessPrefixedAmplet;
import amp.serialization.IAmpByteSerializable;
import com.ampex.amperabase.AddressLength;
import engine.binary.IBinary;
import engine.binary.on_ice.Binary;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;

public class BinALPreBucket implements IAmpByteSerializable {

    public BinALPreBucket(@NotNull IBinary bin, @NotNull AddressLength al, String prefix) {
        this.bin = bin;
        this.al = al;
        this.prefix = prefix;
    }

    private IBinary bin;
    private AddressLength al;
    private String prefix;

    @Override
    public byte[] serializeToBytes() {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.addBytes(bin.serializeToAmplet().serializeToBytes());
        hpa.addElement(al.getIndicator());
        if (prefix != null)
            hpa.addElement(prefix);
        return hpa.serializeToBytes();
    }

    public static BinALPreBucket fromBytes(byte[] bytes) {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(bytes);
        IBinary bin = Binary.deserializeFromAmplet(Amplet.create(hpa.getNextElement()));
        AddressLength al = AddressLength.byIndicator(hpa.getNextElement()[0]);
        if (al == null) return null;
        String prefix = null;
        if (hpa.hasNextElement()) {
            try {
                prefix = new String(hpa.getNextElement(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return new BinALPreBucket(bin, al, prefix);
    }

    public IBinary getBin() {
        return bin;
    }

    public AddressLength getAl() {
        return al;
    }

    public String getPrefix() {
        return prefix;
    }
}
