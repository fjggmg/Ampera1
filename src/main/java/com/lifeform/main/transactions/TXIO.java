package com.lifeform.main.transactions;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Bryan on 8/8/2017.
 */
public interface TXIO {
    List<Integer> VALID_ID_SIZES = Collections.unmodifiableList(Arrays.asList(28, 32));

    /**
     * Gets {@link IAddress} that this TXIO is assigned to
     *
     * @return address for this TXIO
     */
    IAddress getAddress();

    /**
     * Gets amount of this TXIO in a BigInteger format (1 token == 100,000,000)
     *
     * @return Amount of this TXIO
     */
    BigInteger getAmount();

    /**
     * Gets the {@link Token} that this TXIO is in
     * @return Token that this TXIO is transferring
     */
    Token getToken();

    /**
     * Gets the index of this TXIO, this is not very important and rarely used. It was created to mimic what
     * BTC does but it was used improperly at first and because of that it's not used much at all now. We
     * may remove this at a later date
     * @return Index for this TXIO
     */
    int getIndex();

    /**
     * DO NOT USE! THIS IS NOT IMPLEMENTED IN THE NEW TRANSACTION SYSTEM!
     * @return JSON serialized string of this TXIO
     */
    String toJSON();

    /**
     * Gets B64 SHA-3 256 (new system) or SHA-2 224 (old system) ID for this TXIO
     * @return String format ID
     */
    String getID();

    /**
     * Gets timestamp of the creation of this TXIO
     * @return long ms Unix time
     */
    long getTimestamp();
    //HeadlessAmplet serializeToAmplet();
}
