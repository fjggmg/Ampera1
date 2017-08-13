package com.lifeform.main.transactions;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by Bryan on 8/11/2017.
 */
public interface ITrans {

    String getID();
    //TODO: we have TXIO as the interface, we should not be using the implementation, but currently this is the best way to do this
    List<Output> getOutputs();
    List<Input> getInputs();

    boolean verifySigs();

    String toSign();

    boolean verifyCanSpend();

    boolean verifyInputToOutput();

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
    void makeChange(BigInteger fee, Address cAdd);

    String toJSON();

    void addSig(String key,String sig);
}
