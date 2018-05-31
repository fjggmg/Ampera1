package com.ampex.main;

import com.ampex.amperabase.IKiAPI;
import com.ampex.main.GUI.FXGUI;
import com.ampex.main.GUI.NewGUI;
import com.ampex.main.adx.ExchangeManager;
import com.ampex.main.blockchain.ChainManager;
import com.ampex.main.blockchain.ChainManagerLite;
import com.ampex.main.blockchain.IChainMan;
import com.ampex.main.blockchain.PoolChainMan;
import com.ampex.main.blockchain.mining.IMinerMan;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.data.encryption.IEncryptMan;
import com.ampex.main.network.INetworkManager;
import com.ampex.main.network.NetMan;
import com.ampex.main.pool.PoolData;
import com.ampex.main.transactions.ITransMan;
import com.ampex.main.transactions.TransactionManager;
import com.ampex.main.transactions.TransactionManagerLite;
import com.ampex.main.transactions.addresses.AddressManager;
import com.ampex.main.transactions.addresses.IAddMan;
import com.ampex.main.transactions.scripting.ScriptManager;
import engine.ByteCodeEngine;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;

/**
 * Created by Bryan on 5/10/2017.
 * Copyright (C) 2017  Bryan Sharpe

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see http://www.gnu.org/licenses/.
 */
public interface IKi extends IKiAPI {
    /**
     * wrapper for thread
     */
    void start();


    /**
     * Gets chain manager for this instance. Full version is the default ChainManager, lite version is ChainManagerLite instance
     * @return ChainManager for this instance
     * @see IChainMan
     * @see ChainManager
     * @see ChainManagerLite
     * @see PoolChainMan
     */
    IChainMan getChainMan();

    /**
     * Gets transaction manager for this instance. Full version is default TransactionManager, lite version is TransactionManagerLite instance
     * @return TransactionManager for this instance
     * @see ITransMan
     * @see TransactionManager
     * @see TransactionManagerLite
     */
    ITransMan getTransMan();

    /**
     * Gets EncryptionManager for this instance. There is currently one implementation that serves all versions. This is because we have
     * to have keys for any and every version and a way of retaining those, and that is the main function of the encryption manager
     * instance
     * @return EncryptionManager for this instance
     * @see IEncryptMan
     * @see EncryptionManager
     */
    IEncryptMan getEncryptMan();

    /**
     * Gets Logger for the program. Later we may make additional loggers for different events and/or for the public API,
     * so this will be considered the main program logger. This is where all debugging from Origin goes (aside from a few
     * System.out prints where we do not have access to the god object)
     * @return main Logger for the program
     */
    Logger getMainLog();

    /**
     * Gets the network manager for this instance. There used to be separate implementations for client and server but they
     * are now combined and simply initialize differently. There is an implementation for pools but there is a separate getter for
     * that so we can retain the network manager instance for connection to the main network here.
     * @return NetworkManager for this instance
     * @see INetworkManager
     * @see NetMan
     */
    INetworkManager getNetMan();

    /**
     * Closes every closable instance throughout the program and then shuts down. The implementation of this will change
     * to unwind all stacks rather than shutting down soon. Certain threads will need to be converted to daemons first.
     */
    void close();

    /**
     * Tells if this is a relay or not
     * @return true if this is a relay
     */
    boolean isRelay();

    /**
     * @return ID of our relay
     * @deprecated Never really defined as to what this did, will be removed in future versions
     */
    @Deprecated
    String getRelayer();

    /**
     * @param relayer sets the ID of our relay
     * @see IKi#getRelayer()
     * @deprecated
     */
    @Deprecated
    void setRelayer(String relayer);

    /**
     * Gets {@link IAddMan} implementation for this instance. Currently only {@link AddressManager} implements this
     * @return IAddMan for this instance
     */
    IAddMan getAddMan();

    void debug(String s);

    void setGUIHook(NewGUI guiHook);
    IMinerMan getMinerMan();

    void resetLite();

    boolean getSetting(Settings setting);

    void setSetting(Settings setting, boolean set);

    PoolData getPoolData();

    void newTransPool();

    INetworkManager getPoolNet();

    String getStringSetting(StringSettings setting);

    void setStringSetting(StringSettings setting, String value);

    ByteCodeEngine getBCE8();

    ExchangeManager getExMan();

    ScriptManager getScriptMan();


    /**
     * retrieves object to lock on to prevent close
     *
     * @return object to use as lock
     */
    Object getCloseLock();

    void downloadedTo(BigInteger height);

    BigInteger getLoadHeight();

    BigInteger getDownloadedTo();

    BigInteger getStartHeight();

    String getVersion();

    void setInnerGUIRef(FXGUI ref);
}
