package com.lifeform.main;

import amp.AmpLogging;
import amp.exceptions.AmpException;
import com.lifeform.main.adx.ExchangeManager;
import com.lifeform.main.blockchain.*;
import com.lifeform.main.data.*;
import com.lifeform.main.network.*;
import com.lifeform.main.network.pool.KiEventHandler;
import com.lifeform.main.network.pool.PoolBlockHeader;
import com.lifeform.main.network.pool.PoolData;
import com.lifeform.main.network.pool.PoolNetMan;
import com.lifeform.main.transactions.*;
import com.lifeform.main.transactions.scripting.*;
import engine.ASELogging;
import engine.ByteCodeEngine;
import gpuminer.GPULogging;
import gpuminer.JOCL.context.JOCLContextAndCommandQueue;
import gpuminer.miner.context.ContextMaster;
import mining_pool.Pool;
import mining_pool.PoolLogging;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
    public static final String VERSION = "0.18.0-BETA";
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
    private ByteCodeEngine bce8 = new ByteCodeEngine(1);
    private ByteCodeEngine bce16 = new ByteCodeEngine(1);
    private ScriptManager scriptMan;
    private ExchangeManager exchangeMan;
    public Ki(Options o) {
        this.o = o;
        System.setProperty("log4j.configurationFile", "log4j.xml");
        AmpLogging.startLogging();
        PoolLogging.startLogging();
        GPULogging.startLogging();
        ASELogging.startLogging();
        main = LogManager.getLogger("Origin");
        main.info("Origin starting up");
        //region settings shit
        if (settings.get(VERSION) == null || !settings.get(VERSION)) {
            settings.put(VERSION, true);
            try {
                getSetting(Settings.DEBUG_MODE);
            } catch (Exception e) {

                settings.put(Settings.DEBUG_MODE.getKey(), true);
            }
            try {
                getSetting(Settings.HIGH_SECURITY);
            } catch (Exception e) {

                settings.put(Settings.HIGH_SECURITY.getKey(), false);
            }
            try {
                getSetting(Settings.REQUIRE_PASSWORD);
            } catch (Exception e) {

                settings.put(Settings.REQUIRE_PASSWORD.getKey(), false);
            }
            try {
                getSetting(Settings.DYNAMIC_FEE);
            } catch (Exception e) {

                settings.put(Settings.DYNAMIC_FEE.getKey(), true);
            }
            try {
                getSetting(Settings.AUTO_MINE);
            } catch (Exception e) {

                settings.put(Settings.AUTO_MINE.getKey(), false);
            }
            try {
                getSetting(Settings.PPLNS_CLIENT);
            } catch (Exception e) {

                settings.put(Settings.PPLNS_CLIENT.getKey(), false);
            }
            try {
                getSetting(Settings.PPLNS_SERVER);
            } catch (Exception e) {

                settings.put(Settings.PPLNS_SERVER.getKey(), false);
            }
            if (getStringSetting(StringSettings.POOL_FEE) == null)
            stringSettings.put(StringSettings.POOL_FEE.getKey(), "1");
            if (getStringSetting(StringSettings.POOL_STATIC_PPS) == null)
                stringSettings.put(StringSettings.POOL_STATIC_PPS.getKey(), "100");
            if (getStringSetting(StringSettings.PRIMARY_COLOR) == null)
                stringSettings.put(StringSettings.PRIMARY_COLOR.getKey(), "#18BC9C");
            if (getStringSetting(StringSettings.SECONDARY_COLOR) == null)
                stringSettings.put(StringSettings.SECONDARY_COLOR.getKey(), "#252830");
            if (getStringSetting(StringSettings.POOL_PAYTO) == null)
                stringSettings.put(StringSettings.POOL_PAYTO.getKey(), "");
            if (getStringSetting(StringSettings.POOL_SERVER) == null)
                stringSettings.put(StringSettings.POOL_SERVER.getKey(), "ampextech.ddns.net");
        }
        //endregion
        scriptMan = new ScriptManager(bce8, bce16, this);
        encMan = new EncryptionManager(this);
        EncryptionManager.initStatic();
        try {
            ki.getEncryptMan().loadKeys();
        } catch (Exception ex) {
            ki.getEncryptMan().generateKeys();
            ki.getEncryptMan().saveKeys();
        }
        try {
            ki.getEncryptMan().loadEDKeys();
        } catch (Exception e) {
            ki.getEncryptMan().generateEDKeys();
            ki.getEncryptMan().saveEDKeys();
        }
        addMan = new AddressManager(this);
        addMan.load();
        if (addMan.getMainAdd() == null) {
            addMan.setMainAdd(addMan.getNewAdd(KeyType.ED25519, true));
        }
        System.out.println("Main type: " + addMan.getMainAdd().getKeyType());
        exchangeMan = new ExchangeManager(this);
        if (o.relay) {
            //this is for some monitoring stuff available by the getThreads command
            ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(true);
        }
        JOCLContextAndCommandQueue.setWorkaround(true);
        //JOCLContextAndCommandQueue.noIntel = true;
        ContextMaster.disableCUDA();
        ih = new InputHandler(this);
        ih.start();

        instance = this;
        relay = o.relay;
        if (o.pool) {
            //no trans man
        } else if (o.lite) {
            transMan = new TransactionManagerLite(this);
        } else {
            transMan = new TransactionManager(this, o.dump);
        }
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


        if (o.pool || o.poolRelay) {
            pd = new PoolData();
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
        if (o.poolRelay) {
            try {
                miningPool = new Pool(null, new BigInteger("00000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16), 0, ki, new KiEventHandler(this));
                miningPool.start();
            } catch (Exception e) {
                ki.debug("Mining pool failed to start");
            }
            miningPool.setN(5000);
            miningPool.updateCurrentHeight(ki.getChainMan().currentHeight());
            BigDecimal sd = new BigDecimal(GPUMiner.shareDiff);
            ki.debug("Share diff: " + sd.toString());
            BigDecimal cd = new BigDecimal(ki.getChainMan().getCurrentDifficulty());
            ki.debug("Current diff: " + cd);
            ki.debug("cd/sd " + cd.divide(sd, 9, RoundingMode.HALF_DOWN));
            long pps = (long) (((cd.divide(sd, 9, RoundingMode.HALF_DOWN).doubleValue() * ChainManager.blockRewardForHeight(ki.getChainMan().currentHeight()).longValueExact()) * 0.99));
            ki.debug("=========================================UPDATING PPS TO: " + pps);
            ki.getPoolManager().updateCurrentPayPerShare(pps);

            Block b = getChainMan().formEmptyBlock(TransactionFeeCalculator.MIN_FEE);
            PoolBlockHeader pbh = new PoolBlockHeader();
            pbh.coinbase = b.getCoinbase().serializeToAmplet().serializeToBytes();
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
                Thread.sleep(250);
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
                Thread.sleep(250);
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

    private boolean closing = false;
    public void close()
    {
        closing = true;
        while (!canClose) {
        }
        chainMan.close();

        if (!getOptions().pool)
            transMan.close();

        addMan.close();
        netMan.close();
        if (ki.getOptions().pool || ki.getOptions().poolRelay)
            poolNet.close();
        settings.close();
        stringSettings.close();
        exchangeMan.close();
        if (!getOptions().nogui)
            guiHook.close();
        System.exit(0);
    }

    /**
     * Singleton for use with un-initializable objects, like the NewGUI, instance should be taken and stored to
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
        Block b = getChainMan().formEmptyBlock(TransactionFeeCalculator.MIN_FEE);
        PoolBlockHeader pbh = new PoolBlockHeader();
        pbh.coinbase = b.getCoinbase().serializeToAmplet().serializeToBytes();
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

    @Override
    public ByteCodeEngine getBCE8() {
        return bce8;
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
    public Logger getMainLog() { return main; }

    @Override
    public void debug(String debug)
    {
        if (closing) return;
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
            Block b = getChainMan().formEmptyBlock(TransactionFeeCalculator.MIN_FEE);
            PoolBlockHeader pbh = new PoolBlockHeader();
            pbh.coinbase = b.getCoinbase().serializeToAmplet().serializeToBytes();
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

    @Override
    public ExchangeManager getExMan() {
        return exchangeMan;
    }

    @Override
    public ScriptManager getScriptMan() {
        return scriptMan;
    }
}
