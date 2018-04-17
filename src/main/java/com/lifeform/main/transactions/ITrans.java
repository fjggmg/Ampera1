package com.lifeform.main.transactions;

import amp.serialization.IAmpAmpletSerializable;
import com.lifeform.main.IKi;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Bryan on 8/11/2017.
 */
public interface ITrans extends IAmpAmpletSerializable {

    List<Integer> VALID_ID_SIZES = Collections.unmodifiableList(Arrays.asList(32));
    String getID();
    //TODO: we have TXIO as the interface, we should not be using the implementation, but currently this is the best way to do this
    List<Output> getOutputs();
    List<Input> getInputs();

    boolean verifySigs();

    String toSign();

    boolean verifyCanSpend();

    boolean verifyInputToOutput();

    boolean verifySpecial(IKi ki);
    /**
     *
     * @return fee calculated by taking all unassigned inputs
     */
    BigInteger getFee();

    /**
     * Utility function to make change for those who are lazy (me) while leaving a given amount unassigned (as a fee)
     * @param fee amount to leave unassigned
     * @param cAdd address to send change to
     */
    void makeChange(BigInteger fee, IAddress cAdd);

    byte[] toSignBytes();

    @Deprecated
    String toJSON();

    String getMessage();
    void addSig(String key,String sig);

    String getSig(String key);
}
