package com.lifeform.main;

import com.lifeform.main.GUI.NewGUI;
import com.lifeform.main.adx.ExchangeManager;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.blockchain.IChainMan;
import com.lifeform.main.blockchain.IMinerMan;
import com.lifeform.main.blockchain.IStateManager;
import com.lifeform.main.data.IEncryptMan;
import com.lifeform.main.data.Options;
import com.lifeform.main.network.INetworkManager;
import com.lifeform.main.network.pool.PoolData;
import com.lifeform.main.transactions.IAddMan;
import com.lifeform.main.transactions.ITransMan;
import com.lifeform.main.transactions.scripting.ScriptManager;
import engine.ByteCodeEngine;
import mining_pool.Pool;
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
public interface IKi {
    /**
     * wrapper for thread
     */
    void start();

    /**
     * Gets options the program was started with (i.e. -md for miner debug)
     *
     * @return Options object created from start of program
     * @see Options
     */
    Options getOptions();

    /**
     * Gets chain manager for this instance. Full version is the default ChainManager, lite version is ChainManagerLite instance
     * @return ChainManager for this instance
     * @see IChainMan
     * @see com.lifeform.main.blockchain.ChainManager
     * @see com.lifeform.main.blockchain.ChainManagerLite
     * @see com.lifeform.main.blockchain.PoolChainMan
     */
    IChainMan getChainMan();

    /**
     * Gets transaction manager for this instance. Full version is default TransactionManager, lite version is TransactionManagerLite instance
     * @return TransactionManager for this instance
     * @see ITransMan
     * @see com.lifeform.main.transactions.TransactionManager
     * @see com.lifeform.main.transactions.TransactionManagerLite
     */
    ITransMan getTransMan();

    /**
     * Gets EncryptionManager for this instance. There is currently one implementation that serves all versions. This is because we have
     * to have keys for any and every version and a way of retaining those, and that is the main function of the encryption manager
     * instance
     * @return EncryptionManager for this instance
     * @see IEncryptMan
     * @see com.lifeform.main.data.EncryptionManager
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
     * @see com.lifeform.main.network.NetMan
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
     * Used for doing processing after we get a block. Used currently for pool server processing (creating new workloads).
     * This method is called from the {@link com.lifeform.main.blockchain.ChainManager} normally
     *
     * @param block Block that we received and added to the chain.
     */
    void blockTick(Block block);

    /**
     * Gets {@link IAddMan} implementation for this instance. Currently only {@link com.lifeform.main.transactions.AddressManager} implements this
     * @return IAddMan for this instance
     */
    IAddMan getAddMan();

    void debug(String s);

    com.lifeform.main.GUI.NewGUI getGUIHook();

    void setGUIHook(NewGUI guiHook);
    IMinerMan getMinerMan();

    void restartNetwork();

    IStateManager getStateManager();

    void resetLite();

    Pool getPoolManager();

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

    void setStartHeight(BigInteger startHeight);
}
