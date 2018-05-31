package com.ampex.main.transactions;

import com.ampex.amperabase.*;
import engine.binary.IBinary;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by Bryan on 8/11/2017.
 * Most methods in here are described by what the normal implementations of them do. Not all methods are available in all
 * implementations (the default transaction manager should have everything implemented, but the lite one does not) Some
 * methods are described in how the implementations should be.
 */
public interface ITransMan extends ITransManAPI {




    /**
     * Gets a list of un-used outputs for the address given. The "safe" option is for certain implementations that may
     * allow duplication of data and/or provide a list that is not unique to this call (can be modified by other calls)
     * This isn't used by current implementations, but was at one point to allow for less ram usage by sharing a list between
     * calls. The implementation has been improved and the boolean is not used any more. It is left here in case a future
     * implementation uses it. Generally speaking, if you're displaying data or doing some other non-critical or trivial function
     * you can set "safe" to false. For features that need to be secure and ensure no data issues set this to true.
     * @param address Address to get UTXOs for. These are received funds that have not been spent
     * @param safe Whether to protect the returned data or not (current implementations are safe either way)
     * @return a List of Outputs that are unspent and assigned to the given address
     */
    List<IOutput> getUTXOs(IAddress address, boolean safe);

    /**
     * Returns if the UTXOs for an address have changed since they were last checked. Not used in current implementations
     * but was used before to prevent rechecking UTXOs if we didn't need to. Will possibly be deprecated soon as it does
     * not work in any current implementations
     * @param address Address to check if UTXOs have changed on
     * @return true if UTXOs have changed since last check
     */
    boolean utxosChanged(IAddress address);

    /**
     * Closes DB connections for clean exit
     */
    void close();

    /**
     * clears transaction DBs. VERY DANGEROUS METHOD, WILL COMPLETELY CLEAR COLLECTED CHAIN STATE.
     */
    void clear();

    /**
     * Creates a simple multi-sig transaction. This is used for the first person initiating a transaction in a multi-sig group.
     * The transaction can then be saved and sent to the other members to sign until it has enough signatures to send.
     * @param bin Binary for the multi-sig wallet
     * @param receiver address to send to
     * @param amount Amount of the token to send
     * @param fee fee to pay to the miner (may be overridden if it is lower than the minimum required for this transaction)
     * @param token Token to send
     * @param message Message to put on transaction
     * @param multipleOuts Amount of outputs to put on transaction, larger amounts make it easier for other side to send multiple transactions at once, but you will pay a higher fee
     * @param changeAddress Address to send change back to (overage of inputs)
     * @return created transaction
     * @throws InvalidTransactionException if invalid parameters are given or if there are not funds to cover this transaction, an exception will be thrown
     */
    ITrans createSimpleMultiSig(IBinary bin, IAddress receiver, BigInteger amount, BigInteger fee, Token token, String message, int multipleOuts, IAddress changeAddress) throws InvalidTransactionException;

    /**
     * Creates a simple transaction. Calls the other createSimple transaction with multipleOuts set to 1
     * @param receiver address to send funds to
     * @param amount amount of the token to send
     * @param fee fee to pay to the miner (may be overridden if it is lower than the minimum required for this transaction)
     * @param token Token to send
     * @param message Message to put on transaction
     * @param changeAddress Address to send change back to (overage of inputs)
     * @return created transaction
     * @throws InvalidTransactionException if invalid parameters are given or if there are not funds to cover this transaction an exception will be thrown
     */
    ITrans createSimple(IAddress receiver, BigInteger amount, BigInteger fee, Token token, String message, IAddress changeAddress) throws InvalidTransactionException;

    /**
     * Creates a simple transaction. Calls the other createSimple transaction with multipleOuts set to 1
     * @param receiver address to send funds to
     * @param amount amount of the token to send
     * @param fee fee to pay to the miner (may be overridden if it is lower than the minimum required for this transaction)
     * @param token Token to send
     * @param message Message to put on transaction
     * @param multipleOuts Amount of outputs to put on transaction, larger amounts make it easier for other side to send multiple transactions at once, but you will pay a higher fee
     * @param changeAddress Address to send change back to (overage of inputs)
     * @return created transaction
     * @throws InvalidTransactionException if invalid parameters are given or if there are not funds to cover this transaction an exception will be thrown
     */
    ITrans createSimple(IAddress receiver, BigInteger amount, BigInteger fee, Token token, String message, int multipleOuts, IAddress changeAddress) throws InvalidTransactionException;


    /**
     * Built to ease creation of transactions by putting some common logic in a new method. This method is built
     * on top of getUTXOs.
     * @param address Address to build a list of Inputs from
     * @param amount amount of token that must be supplied (overage will most likely happen)
     * @param token Token to get Inputs for
     * @param used Set to true to add these to used UTXOs, false if not
     * @return a List of Inputs that are made form UTXOs if there are enough to cover the amount, else null
     */
    List<IInput> getInputsForAmountAndToken(IAddress address, BigInteger amount, Token token, boolean used);

    /**
     * Gets a list of all UTXOs built into inputs. Essentially getUTXOs but goes ahead and makes them Inputs
     * @param address Address to get the list for
     * @param token Token to get Inputs for
     * @return List of Inputs for the address
     */
    List<IInput> getInputsForToken(IAddress address, Token token);

    /**
     * Gets an amount of a token in a wallet, uses getInputsForToken then iterates over it adding all amounts together
     * @param address Address to get amount for
     * @param token Token to get amount for
     * @return Amount of token assigned to the Address
     */
    BigInteger getAmountInWallet(IAddress address, Token token);

    /**
     * un-uses a list of Inputs (used normally for undoing a transaction you just made)
     * @param inputs List of Inputs to remove from used UTXOs
     */
    void unUseUTXOs(List<IInput> inputs);

    /**
     * Primer for post block processing. This keeps PBP from having to count from -1 to current height when the first
     * block to process comes through. You should not need to call this, it's called from the chain manager during startup.
     * @param currentHeight currentHeight of chain
     */
    void setCurrentHeight(BigInteger currentHeight);

    /**
     * Wrapper for threading. This was created to separate thread creation from the constructor
     */
    void start();

    /**
     * wrapper for thread.interrupt
     */
    void interrupt();
}
