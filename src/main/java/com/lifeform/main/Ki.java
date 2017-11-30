package com.lifeform.main;

import com.lifeform.main.blockchain.*;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.IEncryptMan;
import com.lifeform.main.data.InputHandler;
import com.lifeform.main.data.Options;
import com.lifeform.main.network.Handshake;
import com.lifeform.main.network.INetworkManager;
import com.lifeform.main.network.NetMan;
import com.lifeform.main.transactions.*;
import gpuminer.JOCL.JOCLContextAndCommandQueue;
import gpuminer.JOCL.JOCLDevices;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.bitbucket.backspace119.generallib.Logging.ConsoleLogger;
import org.bitbucket.backspace119.generallib.Logging.LogMan;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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

    private IMinerMan minerMan;
    private Options o;
    private LogMan logMan;
    private Logger main;
    private INetworkManager netMan;
    private ChainManager chainMan;
    private ITransMan transMan;
    private EncryptionManager encMan;
    private IAddMan addMan;
    private IKi ki = this;
    private boolean run = true;
    //TODO: need to start saving version number to file for future conversion of files
    public static final String VERSION = "0.14.7-BETA";
    private boolean relay = false;
    private FXMLController guiHook;
    public static boolean debug = true;
    private static IKi instance;
    private InputHandler ih;

    public Ki(Options o) {
        JOCLContextAndCommandQueue.setWorkaround(true);
        ih = new InputHandler(this);
        ih.start();
        this.o = o;
        instance = this;
        relay = o.relay;
        logMan = new LogMan(new ConsoleLogger());

        main = logMan.createLogger("Main", "console", Level.DEBUG);
        main.info("Ki starting up");
        chainMan = new ChainManager(this, (o.testNet) ? ChainManager.TEST_NET : ChainManager.POW_CHAIN, "blocks/", "chain.state", "transaction.meta", "extra.chains", "chain.meta", o.bDebug);
        Handshake.CHAIN_VER = (o.testNet) ? ChainManager.TEST_NET : ChainManager.POW_CHAIN;
        chainMan.loadChain();
        getMainLog().info("Chain loaded. Current height: " + chainMan.currentHeight());
        transMan = new TransactionManager(this, o.dump);
        encMan = new EncryptionManager(this);
        EncryptionManager.initStatic();


        try {
            ki.getEncryptMan().loadKeys();
        } catch (Exception ex) {
            ki.getEncryptMan().generateKeys();
            ki.getEncryptMan().saveKeys();
        }
        addMan = new AddressManager(this);
        addMan.load();
        if (addMan.getMainAdd() == null) {
            addMan.setMainAdd(addMan.getNewAdd());
        }
        if (o.rebuild) {
            List<Block> blocksToRebuild = new ArrayList<>();
            BigInteger b = BigInteger.ONE;
            while (b.compareTo(chainMan.currentHeight()) <= 0) {
                blocksToRebuild.add(chainMan.getByHeight(b));
                b = b.add(BigInteger.ONE);
            }
            chainMan.clearFile();
            transMan.clear();
            for (Block block : blocksToRebuild) {
                chainMan.addBlock(block);
            }
        }

        minerMan = new MinerManager(this, o.mDebug);

        netMan = new NetMan(this, o.relay);
        netMan.start();
        //gui = MainGUI.guiFactory(this);
        Thread t = new Thread() {

            @Override
            public void run() {
                if (!o.nogui)
                    FXGUI.subLaunch();
            }
        };
        t.start();

    }

    boolean setupDone = false;
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!setupDone) {
                minerMan.setup();
                setupDone = true;
            }
            try {
                Thread.sleep(4500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (rn) {
                rn();
            }
        }
    }
    @Override
    public void setGUIHook(FXMLController guiHook) {
        this.guiHook = guiHook;
    }

    @Override
    public FXMLController getGUIHook() {
        return guiHook;
    }
    public void close()
    {
        chainMan.close();
        transMan.close();
        addMan.save();
    }

    /**
     * Singleton for use with un-initializable objects, like the FXMLController, instance should be taken and stored to
     * help prevent thread issues.
     * @return instance of Ki
     */
    public static IKi getInstance()
    {
        return instance;
    }
    @Override
    public boolean isRelay() {
        return relay;
    }

    private volatile boolean rn = false;

    @Override
    public void restartNetwork() {
        rn = true;
    }

    private void rn() {
        rn = false;
        netMan.interrupt();
        netMan = new NetMan(ki, o.relay);
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
    public void debug(String debug)
    {
        if(Ki.debug)
        {
            main.debug(debug);
        }
    }

    @Override
    public INetworkManager getNetMan()
    {
        return netMan;
    }

    @Override
    public void blockTick(Block block)
    {
        /* old miner
        CPUMiner.height = block.height.add(BigInteger.ONE);
        CPUMiner.prevID = block.ID;
        */

    }

    @Override
    public IMinerMan getMinerMan()
    {
        return minerMan;
    }
    @Override
    public IAddMan getAddMan()
    {
        return addMan;
    }
}
