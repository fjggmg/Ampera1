package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.Utils;
import com.lifeform.main.network.BlockEnd;
import com.lifeform.main.network.BlockHeader;
import com.lifeform.main.network.TransactionPacket;
import gpuminer.JOCL.constants.JOCLConstants;
import gpuminer.JOCL.context.JOCLContextAndCommandQueue;
import gpuminer.JOCL.context.JOCLDevices;
import gpuminer.miner.SHA3.SHA3Miner;
import gpuminer.miner.autotune.Autotune;
import gpuminer.miner.context.ContextMaster;
import gpuminer.miner.context.DeviceContext;
import gpuminer.miner.databuckets.BlockAndSharePayloads;
import gpuminer.miner.synchron.Synchron;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static org.jocl.CL.CL_DEVICE_TYPE_GPU;
import static org.jocl.CL.stringFor_cl_device_address_info;

public class GPUMiner extends Thread implements IMiner {

    private IKi ki;
    private int index;
    public static volatile boolean mining = false;

    private SHA3Miner miner;
    private DeviceContext jcacq;
    private static List<SHA3Miner> gpuMiners = new ArrayList<>();
    private static List<DeviceContext> jcacqs_;
    private static volatile boolean autotuneDone = false;
    private static volatile boolean stopAutotune = false;
    private static volatile boolean triedNoCPU = false;
    public static ContextMaster platforms;// = new ContextMaster();
    public volatile static boolean initDone = false;
    public static BigInteger minFee = BigInteger.TEN;

    public static int init(IKi ki, ContextMaster cm) throws MiningIncompatibleException {
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
        platforms = cm;
        List<DeviceContext> jcacqs = platforms.getContexts();
        final Thread t = new Thread() {

            public void run() {
                ki.debug("Starting autotune");
                Autotune.setup(jcacqs, false);
                autotuneDone = true;

            }
        };
        t.start();
        while (!autotuneDone) {
            //ki.debug("Autotune not done");
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        /*ki.debug("Autotune done");
        if (stopAutotune && !triedNoCPU) {
            ki.debug("Autotune was stopped");
            if (!triedNoCPU) {
                triedNoCPU = true;
                JOCLDevices.setDeviceFilter(CL_DEVICE_TYPE_GPU);
                return init(ki);
            }
        } else */
        /*
        if (stopAutotune) {
            throw new MiningIncompatibleException("Autotune took more than 5 minutes, your device may be compatible, but is running so slowly that it would not be profitable to mine on.");
        }
        */



        byte[] difficulty = new byte[64];
        int p = 63;
        for (int i = ki.getChainMan().getCurrentDifficulty().toByteArray().length - 1; i >= 0; i--) {
            difficulty[p] = ki.getChainMan().getCurrentDifficulty().toByteArray()[i];
            p--;
        }
        int i = 0;
        for (DeviceContext jcaq : jcacqs) {
            int threadCount = Autotune.getAtSettingsMap().get(jcaq.getDInfo().getDeviceName()).threadFactor;
            SHA3Miner miner = new SHA3Miner(jcaq, difficulty, null, threadCount, Autotune.getAtSettingsMap().get(jcaq.getDInfo().getDeviceName()).kernelType);
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
        this.devName = devName + " #" + index;

    }

    private long lastPrint = System.currentTimeMillis();
    private DecimalFormat format = new DecimalFormat("###,###,###,###");
    private int threadCount;

    @Override
    public void run() {
        boolean hasPrinted = false;
        if (disabled) return;
        ki.debug("Attempting to start mining on OpenCL device: " + jcacq.getDInfo().getDeviceName());
        while (mining) {
            if (mining) {
                Block b = ki.getChainMan().formEmptyBlock(minFee);
                //In theory the message can be nearly as large as Java can make an array and the miner will barely suffer any slowdown.
                //The slowdown from dealing with the message size happens every two terahashes, and it's unlikely to be longer than a second, though I have not timed it, so it is negligible.
                //There is a practical limit on the size of the message because if it's too large then the miner won't have space to postpend guess data.
                //But a message would need to be ginormous, or a difficulty would need to be obscenely hard, for that to be an issue.
                //If that ever is an issue, the miner is already built to gracefully handle the problem.
                byte[] message = b.gpuHeader();



                //Six preceding zeroes on the difficulty is hard enough that the miner is likely to be able to time its hashrate.
                threadCount = Autotune.getAtSettingsMap().get(jcacq.getDInfo().getDeviceName()).threadFactor;
                byte[] difficulty = new byte[64];
                int p = 63;
                for (int i = ki.getChainMan().getCurrentDifficulty().toByteArray().length - 1; i >= 0; i--) {
                    difficulty[p] = ki.getChainMan().getCurrentDifficulty().toByteArray()[i];
                    p--;
                }
                if (!hasPrinted) {
                    ki.debug("Successfully started mining on device: " + devName);
                    hasPrinted = true;
                }
                ki.debug("Current difficulty is: " + Utils.toHexArray(difficulty));

                miner.resumeMining(message);

                    //It will take a while to mine, so whatever thread is using the miner object will need to wait for it to finish.
                while (miner.isMining() && mining)
                    {
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

                    //The miner outputs the winning seeded message, not the hash itself, so if you need the hash you'll need to use bouncycastle for it.
                    //If the miner hasn't found a winning seeded message this will return null, so you need to check for that.
                    if (miner.getPayloads() != null) {

                        ki.debug("Mining on OpenCL device: " + jcacq.getDInfo().getDeviceName() + " has stopped.");
                        ki.debug("Found a block, sending to network");

                        if (miner.getHashesPerSecond() != -1) {
                            ki.debug("Current hash rate on OpenCL device: " + jcacq.getDInfo().getDeviceName() + " is: " + format.format(miner.getHashesPerSecond()) + " H/s");
                        }

                        BlockAndSharePayloads[] basp = miner.getPayloads();
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
                    /* old diagnostics
                    try {
                        ki.debug("Diag message is: " + new String(miner.queryDiagnosticMessageWithFrontPayload(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                     */
                        if (ki.getChainMan().softVerifyBlock(b).success()) {
                            ki.getChainMan().setTemp(b);
                            sendBlock(b);
                            ki.debug("YOU FOUND A BLOCK! If it verifies you will receive it back shortly from the network.");
                            mining = false;
                            for (String t : b.getTransactionKeys()) {
                                ki.getTransMan().getPending().remove(b.getTransaction(t));
                            }
                        } else if (ki.getChainMan().getCurrentDifficulty().compareTo(new BigInteger(Utils.fromBase64(b.ID))) < 0) {
                            ki.debug("FOUND AN ERROR ON OPENCL DEVICE: " + jcacq.getDInfo().getDeviceName());

                            run();
                        } else {
                            ki.debug("An error was found with the block verification. Block will not be sent to the network");

                            run();
                        }
                    }

            }
                //If you're ever going to send the miner to the GC, you need to call this first.

        }
    }

    @Override
    public void interrupt() {
        miner.pauseMining();
        super.interrupt();
    }

    @Override
    public long getHashrate() {
        return hashrate;
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
            tp.trans = b.getTransaction(key).toJSON();
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
        bh.coinbase = b.getCoinbase().toJSON();
        return bh;
    }
}
