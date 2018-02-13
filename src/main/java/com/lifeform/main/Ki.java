package com.lifeform.main;

import com.lifeform.main.blockchain.*;
import com.lifeform.main.data.*;
import com.lifeform.main.network.*;
import com.lifeform.main.network.pool.KiEventHandler;
import com.lifeform.main.network.pool.PoolBlockHeader;
import com.lifeform.main.network.pool.PoolData;
import com.lifeform.main.network.pool.PoolNetMan;
import com.lifeform.main.transactions.*;
import gpuminer.JOCL.context.JOCLContextAndCommandQueue;
import mining_pool.Pool;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.bitbucket.backspace119.generallib.Logging.ConsoleLogger;
import org.bitbucket.backspace119.generallib.Logging.LogMan;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bryan on 5/10/2017.
 *
 * Copyright (C) 2017 Ampex Technologies LLC.

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
    private IChainMan chainMan;
    private ITransMan transMan;
    private EncryptionManager encMan;
    private IAddMan addMan;
    private INetworkManager poolNet;
    private IKi ki = this;
    private boolean run = true;
    //TODO: need to start saving version number to file for future conversion of files
    public static final String VERSION = "0.17.2-BETA";
    private boolean relay = false;
    private NewGUI guiHook;
    public static boolean debug = true;
    private static IKi instance;
    private InputHandler ih;
    private IStateManager stateMan;
    private Pool miningPool;
    public static volatile boolean canClose = true;
    private PoolData pd;
    private XodusStringBooleanMap settings = new XodusStringBooleanMap("settings");
    private XodusStringMap stringSettings = new XodusStringMap("etc");
    public Ki(Options o) {
        if (settings.get(VERSION) == null || !settings.get(VERSION)) {
            settings.put(VERSION, true);
            settings.put(Settings.DEBUG_MODE.getKey(), false);
            settings.put(Settings.HIGH_SECURITY.getKey(), false);
            settings.put(Settings.REQUIRE_PASSWORD.getKey(), true);
            settings.put(Settings.DYNAMIC_FEE.getKey(), true);
            stringSettings.put(StringSettings.POOL_FEE.getKey(), "1");
            stringSettings.put(StringSettings.POOL_STATIC_PPS.getKey(), "100");
            stringSettings.put(StringSettings.PRIMARY_COLOR.getKey(), "#18BC9C");
            stringSettings.put(StringSettings.SECONDARY_COLOR.getKey(), "#252830");
            stringSettings.put(StringSettings.POOL_PAYTO.getKey(), "");
            stringSettings.put(StringSettings.POOL_SERVER.getKey(), "ampextech.ddns.net");
        }
        if (o.relay) {
            ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(true);
        }
        JOCLContextAndCommandQueue.setWorkaround(true);
        JOCLContextAndCommandQueue.noIntel = true;
        ih = new InputHandler(this);
        ih.start();
        this.o = o;
        instance = this;
        relay = o.relay;
        logMan = new LogMan(new ConsoleLogger());

        main = logMan.createLogger("Main", "console", Level.DEBUG);
        main.info("Origin starting up");
        if (o.pool) {
            chainMan = new PoolChainMan();
        } else if (o.lite) {
            chainMan = new ChainManagerLite(this, (o.testNet) ? ChainManager.TEST_NET : ChainManager.POW_CHAIN);
        } else {
            chainMan = new ChainManager(this, (o.testNet) ? ChainManager.TEST_NET : ChainManager.POW_CHAIN, "blocks/", "chain.state", "transaction.meta", "extra.chains", "chain.meta", o.bDebug);
        }
        Handshake.CHAIN_VER = (o.testNet) ? ChainManager.TEST_NET : ChainManager.POW_CHAIN;
        chainMan.loadChain();
        getMainLog().info("Chain loaded. Current height: " + chainMan.currentHeight());
        if (o.pool) {
            //no trans man
        } else if (o.lite) {
            transMan = new TransactionManagerLite(this);
        } else {
            transMan = new TransactionManager(this, o.dump);
        }
        encMan = new EncryptionManager(this);
        EncryptionManager.initStatic();
        debug("EncryptMan done");

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
        if (o.pool || o.poolRelay) {
            pd = new PoolData();
        }
        debug("Addman done");
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
        if (o.poolRelay) {
            try {
                miningPool = new Pool(null, new BigInteger("00000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16), 0, getEncryptMan().getPublicKeyString(), new KiEventHandler(this));
                miningPool.start();
            } catch (Exception e) {
                ki.debug("Mining pool failed to start");
            }
            miningPool.updateCurrentHeight(ki.getChainMan().currentHeight());
            BigDecimal sd = new BigDecimal(GPUMiner.shareDiff);
            ki.debug("Share diff: " + sd.toString());
            BigDecimal cd = new BigDecimal(ki.getChainMan().getCurrentDifficulty());
            ki.debug("Current diff: " + cd);
            ki.debug("cd/sd " + cd.divide(sd, 9, RoundingMode.HALF_DOWN));
            long pps = (long) (((cd.divide(sd, 9, RoundingMode.HALF_DOWN).doubleValue() * ChainManager.blockRewardForHeight(ki.getChainMan().currentHeight()).longValueExact()) * 0.99));
            ki.debug("=========================================UPDATING PPS TO: " + pps);
            ki.getPoolManager().updateCurrentPayPerShare(pps);

            Block b = getChainMan().formEmptyBlock(GPUMiner.minFee);
            PoolBlockHeader pbh = new PoolBlockHeader();
            pbh.coinbase = b.getCoinbase().toJSON();
            ki.debug("=================================CURRENT WORK HEIGHT: " + b.height);
            pbh.height = b.height;
            pbh.ID = b.ID;
            pbh.merkleRoot = b.merkleRoot();
            ki.debug("================================================Current merkle root: " + b.merkleRoot());
            pbh.prevID = b.prevID;
            pbh.solver = b.solver;
            pbh.timestamp = b.timestamp;
            getPoolData().workMap.put(b.merkleRoot(), b);
            getPoolData().currentWork = pbh;
            poolNet = new PoolNetMan(this);
            poolNet.start();

        }
        if (!o.pool)
            if (o.lite) {
                stateMan = new StateManagerLite(this);
            } else {
                stateMan = new StateManager(this);
            }
        if (stateMan != null) stateMan.start();

        if (o.pool) {
            netMan = new PoolNetMan(this);
            poolNet = netMan;
        } else {
            netMan = new NetMan(this, o.relay);
            netMan.start();
        }

        debug("Stateman done");
        if (o.lite && !o.pool) {
            while(netMan.getConnections().size() < 1){}
            netMan.broadcast(new DifficultyRequest());
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!o.relay || !o.poolRelay)
        minerMan = new MinerManager(this, o.mDebug);
        //gui = MainGUI.guiFactory(this);
        debug("Starting GUI");
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
            if (o.relay || o.poolRelay) return;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!setupDone && !o.relay) {
                if (getOptions().lite && !NetMan.DIFF_SET && !getOptions().pool)
                    continue;
                minerMan.setup();
                setupDone = true;
                break;
            }
            try {
                Thread.sleep(4500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
    @Override
    public void setGUIHook(NewGUI guiHook) {
        this.guiHook = guiHook;
    }

    @Override
    public NewGUI getGUIHook() {
        return guiHook;
    }
    public void close()
    {
        while (!canClose) {
        }
        chainMan.close();
        if (!getOptions().pool)
            transMan.close();
        addMan.save();
        netMan.close();
        settings.close();
        System.exit(0);
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

    @Override
    public IStateManager getStateManager() {
        return stateMan;
    }

    @Override
    public void resetLite() {
        if (o.lite) {

            transMan = new TransactionManagerLite(this);
            chainMan = new ChainManagerLite(this, (o.testNet) ? ChainManager.TEST_NET : ChainManager.POW_CHAIN);
            netMan.broadcast(new DifficultyRequest());
            TransactionDataRequest tdr = new TransactionDataRequest();
            netMan.broadcast(tdr);
            BlockRequest br = new BlockRequest();
            br.lite = ki.getOptions().lite;
            br.fromHeight = ki.getChainMan().currentHeight();
            netMan.broadcast(br);
        }
    }

    @Override
    public Pool getPoolManager() {
        return miningPool;
    }

    @Override
    public boolean getSetting(Settings setting) {
        return settings.get(setting.getKey());
    }

    @Override
    public void setSetting(Settings setting, boolean set) {
        ki.debug("Setting setting: " + setting + " to " + set);
        settings.put(setting.getKey(), set);
    }

    @Override
    public PoolData getPoolData() {
        return pd;
    }

    @Override
    public void newTransPool() {
        Block b = getChainMan().formEmptyBlock(GPUMiner.minFee);
        PoolBlockHeader pbh = new PoolBlockHeader();
        pbh.coinbase = b.getCoinbase().toJSON();
        pbh.height = b.height;
        pbh.ID = b.ID;
        pbh.merkleRoot = b.merkleRoot();
        pbh.prevID = b.prevID;
        pbh.solver = b.solver;
        pbh.timestamp = b.timestamp;
        getPoolData().workMap.put(b.merkleRoot(), b);
        getPoolData().currentWork = pbh;
        poolNet.broadcast(pbh);
    }

    @Override
    public INetworkManager getPoolNet() {
        return poolNet;
    }

    @Override
    public String getStringSetting(StringSettings setting) {
        return stringSettings.get(setting.getKey());
    }

    @Override
    public void setStringSetting(StringSettings setting, String value) {
        stringSettings.put(setting.getKey(), value);
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
        if (getSetting(Settings.DEBUG_MODE) || isRelay())
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
        if (getOptions().poolRelay) {
            miningPool.updateCurrentHeight(ki.getChainMan().currentHeight());
            Block b = getChainMan().formEmptyBlock(GPUMiner.minFee);
            PoolBlockHeader pbh = new PoolBlockHeader();
            pbh.coinbase = b.getCoinbase().toJSON();
            pbh.height = b.height;
            pbh.ID = b.ID;
            pbh.merkleRoot = b.merkleRoot();
            pbh.prevID = b.prevID;
            pbh.solver = b.solver;
            pbh.timestamp = b.timestamp;
            getPoolData().workMap.put(b.merkleRoot(), b);
            getPoolData().currentWork = pbh;
            poolNet.broadcast(pbh);
        }
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
