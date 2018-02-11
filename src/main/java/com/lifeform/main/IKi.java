package com.lifeform.main;

import com.lifeform.main.blockchain.Block;
import com.lifeform.main.blockchain.IChainMan;
import com.lifeform.main.blockchain.IMinerMan;
import com.lifeform.main.blockchain.IStateManager;
import com.lifeform.main.data.IEncryptMan;
import com.lifeform.main.data.Options;
import com.lifeform.main.network.INetworkManager;
import com.lifeform.main.network.pool.PoolData;
import com.lifeform.main.transactions.IAddMan;
import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.ITransMan;
import mining_pool.Pool;
import org.apache.logging.log4j.Logger;
import org.bitbucket.backspace119.generallib.Logging.LogMan;

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public interface IKi {
    void start();

    Options getOptions();
    IChainMan getChainMan();
    ITransMan getTransMan();
    IEncryptMan getEncryptMan();
    LogMan getLogMan();
    Logger getMainLog();
    INetworkManager getNetMan();
    void close();
    boolean isRelay();
    String getRelayer();
    void setRelayer(String relayer);
    void blockTick(Block block);
    IAddMan getAddMan();

    void debug(String s);

    NewGUI getGUIHook();

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
}
