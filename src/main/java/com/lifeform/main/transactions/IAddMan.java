package com.lifeform.main.transactions;

import engine.binary.Binary;

import java.util.List;

/**
 * Created by Bryan on 8/8/2017.
 *
 * Most of the methods described in this are what the implementation is meant to do. The implementation may vary
 * slightly, but most methods should have fairly uniform execution.
 */
public interface IAddMan {
    /**
     * This gets a new address with the currently set entropy (default if none has been set) and the keytype given. If save
     * is true it will be saved to the address list
     *
     * @param keyType Keytype of address to generate
     * @param save    pass true to save to addresses list in DB, will always be saved to current address list in memory
     * @return newly created {@link IAddress}
     */
    IAddress getNewAdd(KeyType keyType, boolean save);

    /**
     * called when a block comes through, pretty useless now, may be deprecated later
     */
    void blockTick();

    /**
     * gets the current "main" address. This is only to keep us sane on which address we spend from when we create
     * transactions
     * @return current set "main" address
     */
    IAddress getMainAdd();

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
     * Gets the entropy used to create a particular address (that is ours)
     * @param a Address to get entropy for
     * @return String entropy that was used to create the address
     */
    String getEntropyForAdd(IAddress a);

    /**
     * Sets the current main address. Used to keep us sane when spending
     * @see IAddMan#getMainAdd()
     * @param a {@link IAddress} to set as main address
     */
    void setMainAdd(IAddress a);

    /**
     * Creates a new {@link IAddress}  with all data needed to create it passed in to this method. Easier to use than {@link IAddMan#getNewAdd(KeyType, boolean)}
     * since it's a one step action rather than multiple steps. This method returns a {@link NewAdd} implementation since 18.0, as well
     * as all other address creation methods here. The old {@link Address} class is only there to serve old addresses
     * that still have funds in them, and will not verify on the chain at a later date.
     * @param binOrKey Binary or Key in B64 format to use to create address
     * @param entropy String entropy used to create address
     * @param prefix 5 character String prefix to use when creating address. Current AddressManager implementation will take null or empty as no prefix
     * @param l {@link AddressLength} to use when creating the address, longer addresses are more secure against collisions, but may cost more to spend on in the future
     *                               plus they can get incredibly long and unwieldy to copy/paste
     * @param p2sh if this address is P2SH or not, if you passed in a binary, this should be true, false if else
     * @param keyType The {@link KeyType} to use to create this address. If you passed in a binary, this should be {@link KeyType#NONE}
     *                otherwise match this with the type of key you passed in
     * @return Newly created address from all args passed in
     */
    IAddress createNew(String binOrKey, String entropy, String prefix, AddressLength l, boolean p2sh, KeyType keyType);

    /**
     * Gets all addresses currently assigned to us
     * @return List of all addresses assigned to us
     */
    List<IAddress> getAll();

    /**
     * Deletes an address from our list, and saves this change to the DB
     * @param address {@link IAddress} to delete
     */
    void deleteAddress(IAddress address);

    /**
     * Used to associate a particular binary with a particular address so we can use this binary when creating transactions in the future
     * This is an important step to do after creating a new P2SH address unless the address is managed by its own system (I.E. ADX)
     * @param address {@link IAddress} to associate with the binary
     * @param bin {@link Binary} to be associated
     */
    void associateBinary(IAddress address, Binary bin);

    /**
     * Gets associated binary for this address
     * @see IAddMan#associateBinary(IAddress, Binary)
     * @param address {@link IAddress} to get binary for (note, if this is not a P2SH address you're probably using this incorrectly)
     * @return {@link Binary} associated with this address
     */
    Binary getBinary(IAddress address);

    /**
     * Closes all threads and DBs, shuts down system
     */
    void close();
}
