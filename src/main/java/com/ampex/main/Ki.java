package com.ampex.main;

import amp.AmpLogging;
import com.ampex.amperabase.*;
import com.ampex.amperanet.packets.BlockRequest;
import com.ampex.amperanet.packets.DifficultyRequest;
import com.ampex.amperanet.packets.TransactionDataRequest;
import com.ampex.main.GUI.FXGUI;
import com.ampex.main.GUI.NewGUI;
import com.ampex.main.GUI.PageNames;
import com.ampex.main.adx.ExchangeManager;
import com.ampex.main.benchmarking.SyntheticTransactionBenchmark;
import com.ampex.main.blockchain.*;
import com.ampex.main.blockchain.mining.GPUMiner;
import com.ampex.main.blockchain.mining.IMinerMan;
import com.ampex.main.blockchain.mining.MinerManager;
import com.ampex.main.data.Input.InputHandler;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.data.encryption.IEncryptMan;
import com.ampex.main.data.xodus.XodusStringBooleanMap;
import com.ampex.main.data.xodus.XodusStringMap;
import com.ampex.main.network.INetworkManager;
import com.ampex.main.network.NetMan;
import com.ampex.main.network.packets.pool.PoolBlockHeader;
import com.ampex.main.network.pool.PoolNetMan;
import com.ampex.main.pool.KiEventHandler;
import com.ampex.main.pool.PoolData;
import com.ampex.main.transactions.ITransMan;
import com.ampex.main.transactions.TransactionManager;
import com.ampex.main.transactions.TransactionManagerLite;
import com.ampex.main.transactions.addresses.AddressBook;
import com.ampex.main.transactions.addresses.AddressManager;
import com.ampex.main.transactions.addresses.IAddMan;
import com.ampex.main.transactions.scripting.ScriptManager;
import engine.ASELogging;
import engine.ByteCodeEngine;
import engine.IByteCodeEngine;
import gpuminer.GPULogging;
import gpuminer.JOCL.context.JOCLContextAndCommandQueue;
import gpuminer.miner.context.ContextMaster;
import logging.AmpexLogger;
import mining_pool.Pool;
import mining_pool.PoolLogging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Bryan on 5/10/2017.
 * <p>
 * Copyright (C) 2017 Ampex Technologies LLC.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
public class Ki extends Thread implements IKi, IKiAPI {

    private final Object closeLock = new Object();
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
    private final String VERSION;// = new Properties().load(Ki.class.getResourceAsStream("proj.properties")).getProperty("version");
    private boolean relay = false;
    private NewGUI guiHook;
    //public static boolean debug = true;
    static IKi instance;
    private InputHandler ih;
    private IStateManager stateMan;
    private Pool miningPool;
    //public static volatile boolean canClose = true;
    private PoolData pd;
    private XodusStringBooleanMap settings; // = new XodusStringBooleanMap("settings");
    private XodusStringMap stringSettings;// = new XodusStringMap("etc");
    private ByteCodeEngine bce8 = new ByteCodeEngine(1);
    private ByteCodeEngine bce16 = new ByteCodeEngine(1);
    private ScriptManager scriptMan;
    private ExchangeManager exchangeMan;
    private BigInteger loadHeight;
    private IAddressBook addressBook;

    public Ki(Options o) {
        Properties p = new Properties();
        try (InputStream is = Ki.class.getResourceAsStream("/proj.properties")) {
            p.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (p.getProperty("version") != null)
                VERSION = p.getProperty("version");
            else
                VERSION = "post0.18.4";

        }

        this.o = o;
        if (o.benchmark) {
            SyntheticTransactionBenchmark stb = new SyntheticTransactionBenchmark();
            stb.numberOfTransactions = o.numberOfTransactions;
            stb.useWorstCaseScript = o.useWorstCase;
            stb.useImpossibleScript = o.useImpossible;
            stb.keyType = o.keyType;
            stb.scriptOnly = o.scriptOnly;
            stb.noDisk = o.noDisk;
            stb.syntheticBench();
            return;
        }
        settings = new XodusStringBooleanMap("settings");
        stringSettings = new XodusStringMap("etc");
        System.setProperty("log4j.configurationFile", "log4j.xml");
        AmpLogging.startLogging(new AmpexLogger("Amp"));
        PoolLogging.startLogging(new AmpexLogger("Pool"));
        GPULogging.startLogging(new AmpexLogger("GPU"));
        ASELogging.startLogging(new AmpexLogger("ASE"));
        main = LogManager.getLogger("Ampera");
        main.info("Ampera starting up");
        //region settings shit
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

            settings.put(Settings.AUTO_MINE.getKey(), true);
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
        try {
            getSetting(Settings.SHOWN_WARNING);
        } catch (Exception e) {

            settings.put(Settings.SHOWN_WARNING.getKey(), false);
        }


        //TODO moved the following out of the if statement above, may move the rest out as well, this all makes sure it's not overwriting, so no need to check if we're on the same version or not....will need to investigate further
        if (getStringSetting(StringSettings.POOL_FEE) == null)
            stringSettings.put(StringSettings.POOL_FEE.getKey(), "0.25");
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
        if (getStringSetting(StringSettings.START_PAGE) == null)
            stringSettings.put(StringSettings.START_PAGE.getKey(), PageNames.WALLET.name());
        //endregion
        scriptMan = new ScriptManager(bce8, bce16, this);
        scriptMan.loadScripts(ScriptManager.SCRIPTS_FOLDER);
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
        addressBook = new AddressBook();
        System.out.println("Main type: " + addMan.getMainAdd().getKeyType());
        exchangeMan = new ExchangeManager(this);
        if (o.relay) {
            //this is for some monitoring stuff available by the getThreads command
            ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(true);
        }
        JOCLContextAndCommandQueue.setWorkaround(true);
        ContextMaster.disableCUDA();
        ih = new InputHandler(this);

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
            chainMan = new ChainManager(this, (o.testNet) ? ChainManager.TEST_NET : ChainManager.POW_CHAIN, "blocks/", "chain.state", o.bDebug);
        }

        chainMan.loadChain();
        loadHeight = chainMan.currentHeight();
        pbpStatus = chainMan.currentHeight();
        getMainLog().info("Chain loaded. Current height: " + chainMan.currentHeight());


        pd = new PoolData();

        if (o.rebuild) {
            List<IBlockAPI> blocksToRebuild = new ArrayList<>();
            BigInteger b = BigInteger.ONE;
            while (b.compareTo(chainMan.currentHeight()) <= 0) {
                blocksToRebuild.add(chainMan.getByHeight(b));
                b = b.add(BigInteger.ONE);
            }
            chainMan.clearFile();
            transMan.clear();
            for (IBlockAPI block : blocksToRebuild) {
                chainMan.addBlock(block);
            }
        }
        if (o.poolRelay) {
            try {
                miningPool = new Pool(null, new BigInteger("00000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16), 0, ki, new KiEventHandler(this));
            } catch (RuntimeException e) {
                throw e;
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
            long pps = (long) (((cd.divide(sd, 9, RoundingMode.HALF_DOWN).doubleValue() * ChainManager.blockRewardForHeight(ki.getChainMan().currentHeight()).longValueExact()) * (1 - (Double.parseDouble(getStringSetting(StringSettings.POOL_FEE)) / 100))));

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
            List<String> roots = new ArrayList<>();
            roots.add(b.merkleRoot());
            getPoolData().tracking.put(b.height, roots);
            getPoolData().currentWork = pbh;
            poolNet = new PoolNetMan(this);


        }
        if (!o.pool)
            if (o.lite) {
                stateMan = new StateManagerLite(this);
            } else {
                stateMan = new StateManager(this);
            }


        if (o.pool) {
            netMan = new PoolNetMan(this);
            poolNet = netMan;
        } else {
            poolNet = new PoolNetMan(this);
            netMan = new NetMan(this, o.relay);

        }

        debug("Stateman done");

        if (!o.relay || !o.poolRelay)
            minerMan = new MinerManager(this, o.mDebug);
        //gui = MainGUI.guiFactory(this);
        if(!o.nogui)
        debug("Starting GUI");
        guiThread = new Thread(() -> {
            if (!o.nogui) {
                FXGUI.subLaunch(!o.lite && !o.relay);
            }

        });

        if(o.lite) syncDone = true;

    }

    private volatile FXGUI guiRef;
    private Thread guiThread;
    private boolean setupDone = false;
    private static volatile boolean syncDone = false;
    @Override
    public void run() {
        if (o.benchmark) return;
        if (!o.pool) {
            transMan.start();
        }
        if (stateMan != null) stateMan.start();
        if (!o.pool) {
            netMan.start();
        }

        ih.start();
        guiThread.start();
        if (o.lite && !o.pool) {
            while (netMan.getConnections().size() < 1) {
            }
            netMan.broadcast(new DifficultyRequest());
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (o.poolRelay) {
            miningPool.start();
        }
        if (!getOptions().relay)
            poolNet.start();
        while (true) {
            if (o.relay || o.poolRelay) return;
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!setupDone && !o.relay && syncDone) {
                if (getOptions().lite && !getNetMan().isDiffSet() && !getOptions().pool)
                    continue;
                minerMan.setup();
                setupDone = true;
                break;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
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

    public void close() {
        synchronized (closeLock) {
            closing = true;
            addressBook.close();
            minerMan.shutdown();
            chainMan.close();

            if (!getOptions().pool)
                transMan.close();

            addMan.close();
            netMan.close();
            if (!getOptions().relay)
                poolNet.close();
            if (ki.getOptions().pool || ki.getOptions().poolRelay)
                poolNet.close();
            settings.close();
            stringSettings.close();
            if (!getOptions().pool) {
                exchangeMan.close();
                stateMan.interrupt();
            }
            if (!getOptions().nogui)
                guiHook.close();
            if (getOptions().poolRelay)
                miningPool.interrupt();
            //System.exit(0);
        }
    }

    /**
     * Singleton for use with un-initializable objects, like the NewGUI, instance should be taken and stored to
     * help prevent thread issues.
     *
     * @return instance of Ki
     */
    public static IKi getInstance() {
        return instance;
    }

    @Override
    public boolean isRelay() {
        return relay;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void setInnerGUIRef(FXGUI ref) {
        guiRef = ref;
        synchronized (refNotify) {
            refNotify.notifyAll();
        }
    }

    private BigInteger pbpStatus = BigInteger.ZERO;
    @Override
    public void pbpTo(BigInteger height) {
        pbpStatus = height;
    }

    @Override
    public BigInteger getPBPStatus() {
        return pbpStatus;
    }

    @Override
    public void syncDone() {
        syncDone = true;
        killedSync = true;
        guiRef.loadMain();
    }

    @Override
    public IStateManager getStateManager() {
        return stateMan;
    }

    @Override
    public void doneDownloading() {
        if (ki.getOptions().poolRelay) {
            miningPool.updateCurrentHeight(getChainMan().currentHeight());
        }
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
        if (getPoolData().tracking.get(b.height) != null) {
            getPoolData().tracking.get(b.height).add(b.merkleRoot());
        } else {
            List<String> roots = new ArrayList<>();
            roots.add(b.merkleRoot());
            getPoolData().tracking.put(b.height, roots);
        }
        if (getPoolData().lowestHeight.equals(BigInteger.ZERO) || b.height.compareTo(getPoolData().lowestHeight) < 0) {
            getPoolData().lowestHeight = b.height;
        }

        getPoolData().currentWork = pbh;
        poolNet.broadcast(pbh);
    }

    @Override
    public IAddressBook getAddressBook() {
        return addressBook;
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
    public IByteCodeEngine getBCE8() {
        return bce8;
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
    public Logger getMainLog() {
        return main;
    }

    @Override
    public void debug(String debug) {
        if (closing) return;
        if (getSetting(Settings.DEBUG_MODE) || isRelay()) {

            main.debug(debug);
        }
    }

    @Override
    public INetworkManager getNetMan() {
        return netMan;
    }

    private BigInteger startHeight = BigInteger.ZERO;
    private final Object refNotify = new Object();

    @Override
    public void setStartHeight(BigInteger startHeight) {

        this.startHeight = startHeight;

        if (!o.nogui && !o.lite && startHeight.compareTo(getChainMan().currentHeight()) <= 0) {
            killedSync = true;
            if (guiRef == null) {
                synchronized (refNotify) {
                    try {
                        while (guiRef == null)
                            refNotify.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            guiRef.loadMain();
        }
    }

    @Override
    public Object getCloseLock() {
        return closeLock;
    }

    private BigInteger downloadedTo = BigInteger.ZERO;

    @Override
    public void downloadedTo(BigInteger height) {
        downloadedTo = height;
    }

    @Override
    public BigInteger getDownloadedTo() {
        return downloadedTo;
    }

    @Override
    public BigInteger getLoadHeight() {
        return loadHeight;
    }

    private boolean killedSync = false;

    @Override
    public void blockTick(IBlockAPI block) {
        if (getOptions().poolRelay) {

            miningPool.updateCurrentHeight(ki.getChainMan().currentHeight());
            if (block.getHeight().compareTo(startHeight) >= 0) {
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
                BigInteger height = getPoolData().lowestHeight;
                while (height.compareTo(b.height) != 0) {
                    if (getPoolData().tracking.get(height) != null)
                        for (String root : getPoolData().tracking.get(height)) {
                            getPoolData().workMap.remove(root);
                        }
                    getPoolData().tracking.remove(height);

                    height = height.add(BigInteger.ONE);
                }
                getPoolData().lowestHeight = b.height;
                getPoolData().currentWork = pbh;
                poolNet.broadcast(pbh);
            }
        }

    }

    @Override
    public IMinerMan getMinerMan() {
        return minerMan;
    }

    @Override
    public IAddMan getAddMan() {
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

    @Override
    public BigInteger getStartHeight() {
        return startHeight;
    }
}
