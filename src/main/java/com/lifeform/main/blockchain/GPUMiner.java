package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.Utils;
import com.lifeform.main.network.BlockEnd;
import com.lifeform.main.network.BlockHeader;
import com.lifeform.main.network.TransactionPacket;
import gpuminer.JOCL.constants.JOCLConstants;
import gpuminer.JOCL.context.JOCLContextAndCommandQueue;
import gpuminer.JOCL.context.JOCLContextMaster;
import gpuminer.JOCL.context.JOCLDevices;
import gpuminer.miner.SHA3.SHA3Miner;
import gpuminer.miner.autotune.Autotune;
import gpuminer.miner.context.ContextMaster;
import gpuminer.miner.context.DeviceContext;
import gpuminer.miner.databuckets.BlockAndSharePayloads;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static org.jocl.CL.CL_DEVICE_TYPE_GPU;

public class GPUMiner extends Thread implements IMiner {

    private IKi ki;

    public static volatile boolean mining = false;

    private SHA3Miner miner;
    private DeviceContext jcacq;
    private static List<SHA3Miner> gpuMiners = new ArrayList<>();
    private static List<DeviceContext> jcacqs_;
    private static volatile boolean autotuneDone = false;
    private static volatile boolean stopAutotune = false;
    private static volatile boolean triedNoCPU = false;

    public static int init(IKi ki) throws MiningIncompatibleException {
        gpuMiners.clear();
        ContextMaster platforms = new ContextMaster();
        List<DeviceContext> jcacqs = platforms.getContexts();
        final Thread t = new Thread() {

            public void run() {
                Autotune.setup(jcacqs, false);
                autotuneDone = true;
            }
        };
        t.start();
        new Thread() {
            public void run() {
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() < startTime + 300000) {
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                stopAutotune = true;
                t.interrupt();
                autotuneDone = true;

            }
        }.start();
        while (!autotuneDone) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (stopAutotune) {
            if (!triedNoCPU) {
                triedNoCPU = true;
                JOCLDevices.setDeviceFilter(CL_DEVICE_TYPE_GPU);
                return init(ki);
            }
        } else
            throw new MiningIncompatibleException("Autotune took more than 5 minutes, your device may be compatible, but is running so slowly that it would not be profitable to mine on.");



        byte[] difficulty = new byte[64];
        int p = 63;
        for (int i = ki.getChainMan().getCurrentDifficulty().toByteArray().length - 1; i >= 0; i--) {
            difficulty[p] = ki.getChainMan().getCurrentDifficulty().toByteArray()[i];
            p--;
        }

        for (DeviceContext jcaq : jcacqs) {
            SHA3Miner miner = new SHA3Miner(jcaq, difficulty, null, Autotune.getAtSettingsMap().get(jcaq.getDInfo().getDeviceName()).kernelType);
            gpuMiners.add(miner);

        }

        jcacqs_ = jcacqs;
        return jcacqs.size();
    }

    private long hashrate = -1;

    public GPUMiner(IKi ki) {
        this.ki = ki;
    }

    private DecimalFormat format = new DecimalFormat("###,###,###,###");
    @Override
    public void run() {
        if (disabled) return;
        while (mining) {
            if (mining) {
                Block b = ki.getChainMan().formEmptyBlock();
                //In theory the message can be nearly as large as Java can make an array and the miner will barely suffer any slowdown.
                //The slowdown from dealing with the message size happens every two terahashes, and it's unlikely to be longer than a second, though I have not timed it, so it is negligible.
                //There is a practical limit on the size of the message because if it's too large then the miner won't have space to postpend guess data.
                //But a message would need to be ginormous, or a difficulty would need to be obscenely hard, for that to be an issue.
                //If that ever is an issue, the miner is already built to gracefully handle the problem.
                byte[] message = b.gpuHeader();


                int threadCount = Autotune.getAtSettingsMap().get(jcacq.getDInfo().getDeviceName()).threadFactor;
                //Six preceding zeroes on the difficulty is hard enough that the miner is likely to be able to time its hashrate.

                byte[] difficulty = new byte[64];
                int p = 63;
                for (int i = ki.getChainMan().getCurrentDifficulty().toByteArray().length - 1; i >= 0; i--) {
                    difficulty[p] = ki.getChainMan().getCurrentDifficulty().toByteArray()[i];
                    p--;
                }
                ki.debug("Current difficulty is: " + Utils.toHexArray(difficulty));
                ki.debug("Attempting to start mining on OpenCL device: " + jcacq.getDInfo().getDeviceName());

                //And if you have data ready for it you can start mining right away.
                if (miner.startMining(message, threadCount, difficulty)) {
                    ki.debug("Started mining on OpenCL device: " + jcacq.getDInfo().getDeviceName());

                    //It will take a while to mine, so whatever thread is using the miner object will need to wait for it to finish.
                    while (miner.isMining() && mining) //Any conditions on when to stop mining go in here with miner.hasMiningThread(). You can also stop mining with miner.stopAndClear() from another thread if that thread has a reference to the SHA3Miner object.
                    {
                        if (miner.getHashesPerSecond() != -1) {
                            hashrate = miner.getHashesPerSecond();
                            ki.getMinerMan().setHashrate(devName, hashrate);
                        }
                    }

                    ki.debug("Mining on OpenCL device: " + jcacq.getDInfo().getDeviceName() + " has stopped.");

                /*old diagnostics
                if (miner.queryDiagnosticDifficulty() != null) {
                    ki.debug("Current OpenCL device: " + jcacq.getDInfo().getDeviceName() + " diff is: " + Utils.toHexArray(miner.queryDiagnosticDifficulty()));
                }
                if (miner.queryDiagnosticMessageWithFrontPayload() != null) {
                    try {
                        ki.debug("Current OpenCL device: " + jcacq.getDInfo().getDeviceName() + " message is: " + new String(miner.queryDiagnosticMessageWithFrontPayload(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                 */

                    //The miner outputs the winning seeded message, not the hash itself, so if you need the hash you'll need to use bouncycastle for it.
                    //If the miner hasn't found a winning seeded message this will return null, so you need to check for that.
                    if (miner.getPayloads() != null) {

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
                            mining = false;
                        } else if (ki.getChainMan().getCurrentDifficulty().compareTo(new BigInteger(Utils.fromBase64(b.ID))) < 0) {
                            ki.debug("FOUND AN ERROR ON OPENCL DEVICE: " + jcacq.getDInfo().getDeviceName());
                            miner.stopAndClear();
                            run();
                        } else {
                            ki.debug("An error was found with the block verification. Block will not be sent to the network");
                            miner.stopAndClear();
                            run();
                        }
                    }
                    miner.stopAndClear();
                }
                //If you're ever going to send the miner to the GC, you need to call this first.
            }
        }
    }

    @Override
    public void interrupt() {
        miner.stopAndClear();
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
        devName = jcacq.getDInfo().getDeviceName();
        disabled = !ki.getMinerMan().getDevNames().contains(jcacq.getDInfo().getDeviceName());
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
