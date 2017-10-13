package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.Utils;
import com.lifeform.main.network.BlockEnd;
import com.lifeform.main.network.BlockHeader;
import com.lifeform.main.network.TransactionPacket;
import gpuminer.JOCL.JOCLContextAndCommandQueue;
import gpuminer.JOCL.JOCLPlatforms;
import gpuminer.JOCL.miner.JOCLSHA3Miner;
import org.bouncycastle.jcajce.provider.digest.SHA3;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class GPUMiner extends Thread implements IMiner {

    private IKi ki;

    public static volatile boolean mining = false;
    private JOCLSHA3Miner miner;
    private JOCLContextAndCommandQueue jcacq;
    private static List<JOCLSHA3Miner> gpuMiners = new ArrayList<>();
    private static List<JOCLContextAndCommandQueue> jcacqs_;

    public static int init(IKi ki) {
        JOCLPlatforms platforms = new JOCLPlatforms();
        List<JOCLContextAndCommandQueue> jcacqs = platforms.getContextsAndCommandQueues();
        for (JOCLContextAndCommandQueue jcaq : jcacqs) {
            JOCLSHA3Miner miner = new JOCLSHA3Miner(jcaq);
            gpuMiners.add(miner);

        }
        jcacqs_ = jcacqs;
        return jcacqs.size();
    }


    public GPUMiner(IKi ki) {
        this.ki = ki;
    }

    @Override
    public void run() {

        if (mining) {
            Block b = ki.getChainMan().formEmptyBlock();
            //In theory the message can be nearly as large as Java can make an array and the miner will barely suffer any slowdown.
            //The slowdown from dealing with the message size happens every two terahashes, and it's unlikely to be longer than a second, though I have not timed it, so it is negligible.
            //There is a practical limit on the size of the message because if it's too large then the miner won't have space to postpend guess data.
            //But a message would need to be ginormous, or a difficulty would need to be obscenely hard, for that to be an issue.
            //If that ever is an issue, the miner is already built to gracefully handle the problem.
            byte[] message = b.gpuHeader();

            //I have found a hundred thousand works pretty well on the graphics card.
            //I've noticed if I go higher than this sometimes my GPU won't update my screen for a small, but noticeable amount of time.
            //I'd like the miner to not interrupt normal computer usage by default, but allow users to make it run hotter if they like.
            //You might play with this and see if you can do better.
            int threadCount = (int) jcacq.getDInfo().getMaxWorkGroupSize();

            //Six preceding zeroes on the difficulty is hard enough that the miner is likely to be able to time its hashrate.

            byte[] difficulty = new byte[64];
            int p = 63;
            for (int i = ki.getChainMan().getCurrentDifficulty().toByteArray().length - 1; i >= 0; i--) {
                difficulty[p] = ki.getChainMan().getCurrentDifficulty().toByteArray()[i];
                p--;
            }

            ki.debug("Attempting to start mining on OpenCL device: " + jcacq.getDInfo().getDeviceName());

            //And if you have data ready for it you can start mining right away.
            if (miner.startMining(message, threadCount, difficulty)) {
                ki.debug("Started mining on OpenCL device: " + jcacq.getDInfo().getDeviceName() + ". There will be no further output until a block is found.");

                //It will take a while to mine, so whatever thread is using the miner object will need to wait for it to finish.
                while (miner.isMining() && mining) //Any conditions on when to stop mining go in here with miner.hasMiningThread(). You can also stop mining with miner.stopAndClear() from another thread if that thread has a reference to the JOCLSHA3Miner object.
                {
                }

                ki.debug("Mining on OpenCL device: " + jcacq.getDInfo().getDeviceName() + " has stopped.");

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

                //The miner outputs the winning seeded message, not the hash itself, so if you need the hash you'll need to use bouncycastle for it.
                //If the miner hasn't found a winning seeded message this will return null, so you need to check for that.
                if (miner.queryWinningPayload() != null) {

                    ki.debug("Found a block, sending to network");

                    if (miner.queryHashesPerSecond() != -1) {
                        ki.debug("Current hash rate on OpenCL device: " + jcacq.getDInfo().getDeviceName() + " is: " + miner.queryHashesPerSecond() + " H/s");
                    }

                    byte[] winningPayload = miner.queryWinningPayload();

                    try {
                        ki.debug("Payload is: " + new String(winningPayload, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    b.payload = winningPayload;
                    b.ID = EncryptionManager.sha512(b.header());

                    ki.debug("Hash is: " + Utils.toHexArray(Utils.fromBase64(b.ID)));
                    ki.debug("ODIF is: " + Utils.toHexArray(difficulty));
                    ki.debug("DDIF is: " + Utils.toHexArray(miner.queryDiagnosticDifficulty()));
                    try {
                        ki.debug("Diag message is: " + new String(miner.queryDiagnosticMessageWithFrontPayload(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    if (ki.getChainMan().softVerifyBlock(b))
                        sendBlock(b);
                    else {
                        miner.stopAndClear();
                        ki.debug("FOUND AN ERROR ON OPENCL DEVICE: " + jcacq.getDInfo().getDeviceName());
                        //run();
                    }
                }
                miner.stopAndClear();
            }
            //If you're ever going to send the miner to the GC, you need to call this first.
        }
    }

    @Override
    public void interrupt() {
        miner.stopAndClear();
        super.interrupt();
    }

    @Override
    public void setup(int index) {
        miner = gpuMiners.get(index);
        jcacq = jcacqs_.get(index);
    }

    private void sendBlock(Block b) {
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
