package com.ampex.main.transactions;

import amp.serialization.IAmpAmpletSerializable;
import com.ampex.amperabase.IAddress;
import com.ampex.amperabase.ITransAPI;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Bryan on 8/11/2017.
 */
public interface ITrans extends IAmpAmpletSerializable, ITransAPI {

    List<Integer> VALID_ID_SIZES = Collections.unmodifiableList(Arrays.asList(32));

    /**
     * Gets the transaction ID, calculates it if it hasn't been calculated yet (IDs are not transferred over the
     * network or saved to prevent ID spoofing)
     *
     * @return B64 String ID, should be 256 bit SHA-3 hash of transaction data
     */
    String getID();

    /**
     * DO NOT USE! THIS METHOD HAS A SECURITY FLAW AND IS NOT IMPLEMENTED IN THE NEW TRANSACTION SYSTEM ANYWAY!
     * THIS IS ONLY HERE TO ALLOW OLD TRANSACTIONS TO VERIFY
     *
     * @return String to sign over
     */
    @Deprecated
    String toSign();

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

    /**
     * Gets bytes to sign over for transaction. Pretty much the same as serializing to bytes but without sig
     * @return bytes to sign over for signature of P2PK address
     */
    byte[] toSignBytes();

    /**
     * Do not use. Not implemented in new trnasaction system (since 18.0)
     * @return JSON serialization of this transaction
     */
    @Deprecated
    String toJSON();

    /**
     * Adds a sig to a particular key. Also used to add writable memory to a P2SH binary. This is used after creating
     * a transaction to finish it so it can be sent.
     *
     * @param key Key (or Binary) to associate this Signature (or writable memory) with
     * @param sig Signature (or writable memory) to associate
     */
    void addSig(String key, String sig);

    /**
     * Gets signature (or writable memory) for a given key (or binary)
     * @param key Key (or binary) to get data for
     * @return sig or writable mem for key or bin
     */
    String getSig(String key);
}
