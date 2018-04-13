package com.lifeform.main.transactions;

import amp.Amplet;
import amp.HeadlessPrefixedAmplet;
import amp.classification.classes.AC_ClassInstanceIDIsIndex;
import amp.group_primitives.UnpackedGroup;
import com.lifeform.main.data.AmpIDs;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class KeySigEntropyPair {
    public KeySigEntropyPair(String sig, String entropy, List<String> inputs, String prefix, boolean p2sh, @NotNull KeyType keyType) {
        this.sig = sig;
        this.entropy = entropy;
        this.inputs = inputs;
        this.prefix = prefix;
        this.p2sh = p2sh;
        this.keyType = keyType;
    }

    String sig;
    String entropy;
    List<String> inputs;
    String prefix;
    boolean p2sh;
    KeyType keyType;

    HeadlessPrefixedAmplet toAmplet() {
        //System.out.println("KSEP serialize 1");
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();

        hpa.addElement(sig);
        hpa.addElement(entropy);
        //System.out.println("KSEP serialize 2");
        AC_ClassInstanceIDIsIndex inputs = AC_ClassInstanceIDIsIndex.create(AmpIDs.INPUTS_CID, "Inputs");
        //System.out.println("KSEP serialize 2a");
        for (String s : this.inputs) {
            //System.out.println("KSEP serialize 2b input: " + s);

            inputs.addElement(s);
                // System.out.println("KSEP serialize 2c");

            //System.out.println("KSEP serialize 2e");
        }
        //System.out.println("KSEP serialize 3");
        hpa.addBytes(inputs.serializeToAmplet().serializeToBytes());
        hpa.addElement((p2sh) ? (byte) 1 : (byte) 0);
        hpa.addElement(keyType.getValue());
        if (prefix != null)
            hpa.addElement(prefix);

        //System.out.println("KSEP serialize 4");
        return hpa;
    }

    static KeySigEntropyPair fromAmplet(HeadlessPrefixedAmplet amplet) {
        try {
            String sig = new String(amplet.getNextElement(), "UTF-8");
            String entropy = new String(amplet.getNextElement(), "UTF-8");
            List<UnpackedGroup> inputsAmp = Amplet.create(amplet.getNextElement()).unpackClass(AmpIDs.INPUTS_CID);
            List<String> inputs = new ArrayList<>();
            for (UnpackedGroup up : inputsAmp) {
                inputs.add(up.getElementAsString(0));

            }
            boolean p2sh = (amplet.getNextElement()[0] == 1);
            KeyType keyType = KeyType.byValue(amplet.getNextElement()[0]);
            byte[] preBytes = amplet.getNextElement();
            String prefix = null;
            if (preBytes != null) {
                prefix = new String(preBytes, "UTF-8");
            }
            return new KeySigEntropyPair(sig, entropy, inputs, prefix, p2sh, keyType);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
