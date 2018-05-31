package com.ampex.main.transactions.addresses;

import com.ampex.amperabase.IAddManAPI;
import com.ampex.amperabase.IAddress;
import engine.binary.Binary;
import engine.binary.IBinary;

/**
 * Created by Bryan on 8/8/2017.
 * <p>
 * Most of the methods described in this are what the implementation is meant to do. The implementation may vary
 * slightly, but most methods should have fairly uniform execution.
 */
public interface IAddMan extends IAddManAPI {

    /**
     * called when a block comes through, pretty useless now, may be deprecated later
     */
    void blockTick();

    /**
     * load addresses from DB, called once on startup, should not be called again
     */
    void load();

    /**
     * called to save current address list in memory to DB, called occasionally in address creation, use sparingly but
     * as often as you need to put data in DB.
     */
    void save();

    /**
     * Sets the current main address. Used to keep us sane when spending
     *
     * @param a {@link IAddress} to set as main address
     * @see IAddMan#getMainAdd()
     */
    void setMainAdd(IAddress a);


    /**
     * Deletes an address from our list, and saves this change to the DB
     *
     * @param address {@link IAddress} to delete
     */
    void deleteAddress(IAddress address);

    /**
     * Used to associate a particular binary with a particular address so we can use this binary when creating transactions in the future
     * This is an important step to do after creating a new P2SH address unless the address is managed by its own system (I.E. ADX)
     *
     * @param address {@link IAddress} to associate with the binary
     * @param bin     {@link Binary} to be associated
     */
    void associateBinary(IAddress address, IBinary bin);

    /**
     * Gets associated binary for this address
     *
     * @param address {@link IAddress} to get binary for (note, if this is not a P2SH address you're probably using this incorrectly)
     * @return {@link Binary} associated with this address
     * @see IAddMan#associateBinary(IAddress, IBinary)
     */
    IBinary getBinary(IAddress address);

    /**
     * Closes all threads and DBs, shuts down system
     */
    void close();
}
