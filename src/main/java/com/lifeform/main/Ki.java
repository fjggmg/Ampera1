package com.lifeform.main;

import com.lifeform.main.blockchain.ChainManager;
import com.lifeform.main.blockchain.IChainMan;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.IEncryptMan;
import com.lifeform.main.data.Options;
import com.lifeform.main.network.NetMan;
import com.lifeform.main.transactions.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.bitbucket.backspace119.generallib.Logging.ConsoleLogger;
import org.bitbucket.backspace119.generallib.Logging.LogMan;
import org.bitbucket.backspace119.generallib.io.network.NetworkManager;

/**
 * Created by Bryan on 5/10/2017.
 *
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
public class Ki extends Thread implements IKi {

    private Options o;
    private LogMan logMan;
    private Logger main;
    private NetworkManager netMan;
    private ChainManager chainMan;
    private ITransMan transMan;
    private EncryptionManager encMan;
    private IAddMan addMan;
    private IKi ki = this;
    private MainGUI gui;
    private boolean run = true;
    public static final String VERSION = "0.4.0-BETA";
    private boolean relay = false;
    public Ki(Options o)
    {

        this.o = o;
        relay = o.relay;
        logMan = new LogMan(new ConsoleLogger());
        main = logMan.createLogger("Main","console", Level.DEBUG);
        main.info("Ki starting up");
        chainMan = new ChainManager(this, ChainManager.POW_CHAIN);
        chainMan.loadChain();
        getMainLog().info("Chain loaded. Current height: " + chainMan.currentHeight());
        transMan = new TransactionManager(this);
        encMan = new EncryptionManager(this);
        EncryptionManager.initStatic();

        gui = MainGUI.guiFactory(this);
        try {
            ki.getEncryptMan().loadKeys();
        } catch (Exception ex) {
            ki.getEncryptMan().generateKeys();
            ki.getEncryptMan().saveKeys();
        }
        addMan = new AddressManager(this);
        addMan.load();
        if(addMan.getMainAdd() == null)
        {
            addMan.setMainAdd(addMan.getNewAdd());
        }
        netMan = new NetMan(this,20,o.relay);
        netMan.start();
    }

    public void close()
    {
        chainMan.close();
        transMan.close();
        addMan.save();
    }

    @Override
    public boolean isRelay() {
        return relay;
    }

    private String relayer;
    @Override
    public String getRelayer() {
        return relayer;
    }

    @Override
    public void setRelayer(String relayer) {
        this.relayer = relayer;
    }

    private void setupFromOptions(Options o)
    {

    }


    @Override
    public void run()
    {

        while(run)
        {
            gui.tick();

        }
    }

    @Override
    public Options getOptions() {
        return o;
    }

    @Override
    public IChainMan getChainMan() {
        return chainMan;
    }

    @Override
    public ITransMan getTransMan() {
        return transMan;
    }

    @Override
    public IEncryptMan getEncryptMan() {
        return encMan;
    }

    @Override
    public LogMan getLogMan()
    {
        return logMan;
    }


    @Override
    public Logger getMainLog() { return main; }

    @Override
    public NetworkManager getNetMan()
    {
        return netMan;
    }

    @Override
    public void blockTick()
    {

    }

    @Override
    public IAddMan getAddMan()
    {
        return addMan;
    }
}
