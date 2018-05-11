package com.lifeform.main.blockchain;

import amp.Amplet;
import com.lifeform.main.IKi;
import com.lifeform.main.Settings;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.Utils;
import com.lifeform.main.network.packets.BlockEnd;
import com.lifeform.main.network.packets.BlockHeader;
import com.lifeform.main.network.packets.TransactionPacket;
import com.lifeform.main.network.packets.pool.PoolBlockHeader;
import com.lifeform.main.transactions.NewTrans;
import com.lifeform.main.transactions.TransactionFeeCalculator;
import gpuminer.miner.SHA3.SHA3Miner;
import gpuminer.miner.autotune.TimedAutotune;
import gpuminer.miner.context.ContextMaster;
import gpuminer.miner.context.DeviceContext;
import gpuminer.miner.databuckets.BlockAndSharePayloads;
import gpuminer.miner.databuckets.Payload;
import gpuminer.miner.synchron.Synchron;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class GPUMiner extends Thread implements IMiner {

    private IKi ki;
    private boolean mining = false;
    private SHA3Miner miner;
    private DeviceContext jcacq;
    private static List<SHA3Miner> gpuMiners = new ArrayList<>();
    private static List<DeviceContext> jcacqs_;
    public static volatile ContextMaster platforms;// = new ContextMaster();
    public volatile static boolean initDone = false;
    public static final BigInteger shareDiff = new BigInteger("00000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);


    public static int init(IKi ki, ContextMaster cm) throws MiningIncompatibleException {

        platforms = cm;
        ArrayList<DeviceContext> jcacqs = platforms.getContexts();

        ki.debug("Starting autotune");
        TimedAutotune.setup(jcacqs, false);

        byte[] difficulty = new byte[64];
        int p = 63;
        if (!ki.getOptions().pool)
            for (int i = ki.getChainMan().getCurrentDifficulty().toByteArray().length - 1; i >= 0; i--) {
                difficulty[p] = ki.getChainMan().getCurrentDifficulty().toByteArray()[i];
                p--;
            }

        byte[] share = new byte[64];
        int p2 = 63;
        for (int i = shareDiff.toByteArray().length - 1; i >= 0; i--) {
            share[p2] = shareDiff.toByteArray()[i];
            p2--;
        }
        int i = 0;
        for (DeviceContext jcaq : jcacqs) {
            int threadCount = TimedAutotune.getAutotuneSettingsMap().get(jcaq.getDInfo().getDeviceName()).threadFactor;
            SHA3Miner miner = new SHA3Miner(jcaq, difficulty, (ki.getOptions().pool) ? share : null, threadCount, TimedAutotune.getAutotuneSettingsMap().get(jcaq.getDInfo().getDeviceName()).kernelType);
            gpuMiners.add(miner);
            i++;
        }

        jcacqs_ = jcacqs;
        initDone = true;
        return jcacqs.size();
    }

    private long hashrate = -1;

    public GPUMiner(IKi ki, int index) {
        this.ki = ki;
        this.mining = true;
        //this.devName = devName + " #" + index;
    }

    private long lastPrint = System.currentTimeMillis();
    private DecimalFormat format = new DecimalFormat("###,###,###,###");
    private double miningIntensity = 100;
    @Override
    public void run() {
        boolean hasPrinted = false;
        if (disabled) return;
        ki.debug("Attempting to start mining on OpenCL device: " + jcacq.getDInfo().getDeviceName());
        while (mining) {

            Block b;
            if (!ki.getOptions().pool) {
                b = ki.getChainMan().formEmptyBlock(TransactionFeeCalculator.MIN_FEE);
            } else {
                b = new Block();
                b.payload = ki.getPoolData().currentWork.payload;
                b.prevID = ki.getPoolData().currentWork.prevID;
                b.timestamp = System.currentTimeMillis();
                b.height = ki.getPoolData().currentWork.height;
                b.merkleRoot = ki.getPoolData().currentWork.merkleRoot;
                b.solver = ki.getPoolData().currentWork.solver;

                b.setCoinbase(NewTrans.fromAmplet(Amplet.create(ki.getPoolData().currentWork.coinbase)));


            }

            //In theory the message can be nearly as large as Java can make an array and the miner will barely suffer any slowdown.
            //The slowdown from dealing with the message size happens every two terahashes, and it's unlikely to be longer than a second, though I have not timed it, so it is negligible.
            //There is a practical limit on the size of the message because if it's too large then the miner won't have space to postpend guess data.
            //But a message would need to be ginormous, or a difficulty would need to be obscenely hard, for that to be an issue.
            //If that ever is an issue, the miner is already built to gracefully handle the problem.
            byte[] message;

            message = b.gpuHeader();

            //threadCount = TimedAutotune.getAutotuneSettingsMap().get(jcacq.getDInfo().getDeviceName()).threadFactor;

            if (this.isInterrupted()) return;
            miner.setIntensity((miningIntensity == 0) ? 1 : miningIntensity / 100);
            byte[] difficulty = new byte[64];
            int p = 63;
            if (!ki.getOptions().pool)
                for (int i = ki.getChainMan().getCurrentDifficulty().toByteArray().length - 1; i >= 0; i--) {
                    difficulty[p] = ki.getChainMan().getCurrentDifficulty().toByteArray()[i];
                    p--;
                }
            if (!hasPrinted) {
                ki.debug("Successfully started mining on device: " + devName);
                hasPrinted = true;
            }
            ki.debug("Current difficulty is: " + Utils.toHexArray(difficulty));

            if (ki.getOptions().pool) {

                try {
                    if (ki.getPoolData().ID != null) {
                        byte[] extra = ki.getPoolData().ID.getBytes("UTF-8");
                        miner.resumeMining(message, extra);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else
                miner.resumeMining(message);

            //It will take a while to mine, so whatever thread is using the miner object will need to wait for it to finish.
            while (miner.isMining() && mining) {
                if (miner.getHashesPerSecond() != -1) {
                    hashrate = miner.getHashesPerSecond();

                    if (System.currentTimeMillis() > lastPrint + 5000) {
                        ki.debug("Current hashrate on device: " + devName + " is " + hashrate + " hashes/second");
                        lastPrint = System.currentTimeMillis();
                    }
                    if (System.currentTimeMillis() % 1000 == 0)
                        ki.getMinerMan().setHashrate(devName, hashrate);
                }
                Synchron.idle();
            }
            //System.out.println("Made it out");
            //The miner outputs the winning seeded message, not the hash itself, so if you need the hash you'll need to use bouncycastle for it.
            //If the miner hasn't found a winning seeded message this will return null, so you need to check for that.
            if (miner.getPayloads() != null) {

                ki.debug("Mining on OpenCL device: " + jcacq.getDInfo().getDeviceName() + " has stopped.");
                //ki.debug("Found a block, sending to network");

                if (miner.getHashesPerSecond() != -1) {
                    ki.debug("Current hash rate on OpenCL device: " + jcacq.getDInfo().getDeviceName() + " is: " + format.format(miner.getHashesPerSecond()) + " H/s");
                }
                BlockAndSharePayloads[] basp = miner.getPayloads();
                ki.debug("size of basp: " + basp.length);
                if (!ki.getOptions().pool) {

                    byte[] winningPayload = basp[0].getBlockPayload().getBytes();

                    try {
                        ki.debug("Payload is: " + new String(winningPayload, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    b.payload = winningPayload;
                    b.ID = EncryptionManager.sha512(b.header());

                    ki.debug("HASH is: " + Utils.toHexArray(Utils.fromBase64(b.ID)));
                    ki.debug("DIFF is: " + Utils.toHexArray(difficulty));
                    BlockState state = ki.getChainMan().softVerifyBlock(b);
                    if (state.success()) {
                        ki.getChainMan().setTemp(b);
                        sendBlock(b);
                        ki.debug("YOU FOUND A BLOCK! If it verifies you will receive it back shortly from the network.");
                        mining = false;
                        for (String t : b.getTransactionKeys()) {
                            ki.getTransMan().getPending().remove(b.getTransaction(t));
                        }
                    } else if (ki.getChainMan().getCurrentDifficulty().compareTo(new BigInteger(Utils.fromBase64(b.ID))) < 0) {
                        ki.getMainLog().fatal("FOUND AN ERROR ON OPENCL DEVICE: " + jcacq.getDInfo().getDeviceName());

                        //run();
                    } else {
                        ki.getMainLog().warn("An error was found with the block verification. Block will not be sent to the network, state: " + state);


                        //run();
                    }
                } else {

                    int i = 0;
                    for (BlockAndSharePayloads bp : miner.getPayloads()) {


                        //ki.debug("Merkle root: " + pbh.merkleRoot);

                        if (!(bp.getBlockPayload() == null)) {
                            ki.debug("Block Paylod was not null");
                        }
                        if (bp.getSharePayloads() == null) {
                            ki.debug("Share payload was null");
                            continue;
                        }
                        for (Payload pay : bp.getSharePayloads()) {
                            PoolBlockHeader pbh = new PoolBlockHeader();
                            pbh.height = ki.getPoolData().currentWork.height;
                            pbh.merkleRoot = ki.getPoolData().currentWork.merkleRoot;
                            pbh.solver = ki.getPoolData().currentWork.solver;
                            pbh.prevID = ki.getPoolData().currentWork.prevID;
                            pbh.coinbase = ki.getPoolData().currentWork.coinbase;
                            i++;
                            pbh.payload = pay.getBytes();
                            pbh.timestamp = b.timestamp;
                            b.payload = pay.getBytes();
                            pbh.ID = EncryptionManager.sha512(b.header());
                            pbh.currentHR = ki.getMinerMan().cumulativeHashrate();
                            pbh.pplns = ki.getSetting(Settings.PPLNS_CLIENT);
                            try {
                                ki.debug("Payload: " + new String(pay.getBytes(), "UTF-8"));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            ki.getNetMan().broadcast(pbh);
                        }
                    }
                    for (int q = 0; q < i; q++) {
                        ki.getGUIHook().addShare();
                    }
                    ki.debug("Found: " + i + " shares and sent to network");
                }
            }


        }
    }

    @Override
    public void interrupt() {
        mining = false;
        miner.pauseMining();
        super.interrupt();
    }

    @Override
    public void shutdown() {
        interrupt();
        miner.shutdown();
    }

    public static void clear() {
        //You have to shut these down when you're done with them.
        for (SHA3Miner m : gpuMiners) {
            //This takes the place of stopAndClear() for putting the SHA3Miner and its SHA3MinerThread object in an unrecoverable state.
            //It will completely kill the thread that interacts with the GPU.
            m.shutdown();
        }
        gpuMiners.clear();
        //Gotta shut this down, too, and after the SHA3Miner objects are shut down.
        if (platforms != null) {
            platforms.shutdown();
        }
    }

    @Override
    public long getHashrate() {
        return hashrate;
    }

    @Override
    public void setIntensity(double intensity) {
        miningIntensity = intensity;
    }

    private boolean disabled = false;
    private String devName;

    @Override
    public void setup(int index) {
        miner = gpuMiners.get(index);
        jcacq = jcacqs_.get(index);
        devName = jcacq.getDInfo().getDeviceName() + " #" + index;
        disabled = !ki.getMinerMan().getDevNames().contains(devName);
    }

    private void sendBlock(Block b) {
        if (ki.getOptions().mDebug)
            ki.debug("Sending block to network from miner");
        BlockHeader bh2 = formHeader(b);
        ki.getNetMan().broadcast(bh2);


        for (String key : b.getTransactionKeys()) {
            TransactionPacket tp = new TransactionPacket();
            tp.block = b.ID;
            tp.trans = b.getTransaction(key).serializeToAmplet().serializeToBytes();
            ki.getNetMan().broadcast(tp);
        }
        BlockEnd be = new BlockEnd();
        be.ID = b.ID;
        ki.getNetMan().broadcast(be);
        if (ki.getOptions().mDebug)
            ki.debug("Done sending block");
    }

    private BlockHeader formHeader(Block b) {
        BlockHeader bh = new BlockHeader();
        bh.timestamp = b.timestamp;
        bh.solver = b.solver;
        bh.prevID = b.prevID;
        bh.payload = b.payload;
        bh.merkleRoot = b.merkleRoot;
        bh.ID = b.ID;
        bh.height = b.height;
        bh.coinbase = b.getCoinbase().serializeToAmplet().serializeToBytes();
        return bh;
    }

}
