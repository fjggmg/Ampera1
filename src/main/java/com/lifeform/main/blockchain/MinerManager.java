package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import gpuminer.JOCL.JOCLContextAndCommandQueue;
import gpuminer.JOCL.JOCLPlatforms;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class MinerManager implements IMinerMan{

    private IKi ki;
    private boolean mDebug;

    private static JOCLPlatforms platforms;

    public MinerManager(IKi ki, boolean mDebug)
    {
        this.ki = ki;
        this.mDebug = mDebug;
        platforms = new JOCLPlatforms();
        ocls = GPUMiner.init(ki);
    }

    private int previousCount = 0;
    private List<IMiner> miners = new ArrayList<>();
    private boolean mining = false;
    private int ocls = 0;
    @Override
    public void startMiners(double count)
    {
        int c = (int) count;
        startMiners(c);
    }

    private boolean isRestarting = false;
    /**
     * non blocking
     * @param count
     */
    @Override
    public void restartMiners(int count)
    {
        while(isRestarting)
        {
            try {
                Thread.sleep(5L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        isRestarting = true;
        new Thread() {

            public void run() {
                stopMiners();
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startMiners(count);
                isRestarting = false;
            }
        }.start();
    }
    @Override
    public List<IMiner> getMiners()
    {
        return miners;
    }

    @Override
    public boolean isMining()
    {
        return mining;
    }
    @Override
    public int getPreviousCount()
    {
        return previousCount;
    }

    private boolean useGPU = false;
    private boolean useCPU = true;

    @Override
    public void setUseGPU(boolean useGPU) {
        //TODO: check if we're on Mac, at which point we will refuse to turn on GPU mining, later on we should disable this setting in the GUI if on Mac
        this.useGPU = useGPU;
    }

    @Override
    public void setUseCPU(boolean useCPU) {
        this.useCPU = useCPU;
    }

    ArrayList<GPUMiner> gpuMiners = new ArrayList<GPUMiner>();

    @Override
    public void startMiners(int count) {
        if(ki.getOptions().mining) {
            previousCount = count;
            mining = true;
            if (useCPU) {
                CPUMiner.mining = true;
                CPUMiner.foundBlock = false;

                BigInteger guess = BigInteger.ZERO;
                for (int i = 0; i < count; i++) {
                    if (mDebug)
                        ki.getMainLog().info("Starting miner: " + i);
                    IMiner miner = new CPUMiner(ki, guess, guess.add(BigInteger.valueOf(1000000L)), mDebug);
                    miner.setName("Miner" + i);
                    guess = guess.add(BigInteger.valueOf(1000000L));
                    miners.add(miner);
                    miner.start();
                }

            }

            if (useGPU) {
                GPUMiner.mining = true;

                if (!gpuMiners.isEmpty()) {
                    for (IMiner miner : gpuMiners) {
                        miner.interrupt();
                    }
                    gpuMiners.clear();
                }

                for (int i = 0; i < ocls; i++) {
                    GPUMiner miner = new GPUMiner(ki);
                    miner.setup(i);
                    miner.setName("GPUMiner#" + i);
                    gpuMiners.add(miner);
                    miner.start();
                }

            }
        }
    }

    @Override
    public void stopMiners() {
        for(IMiner miner:miners)
        {
            miner.interrupt();
        }
        CPUMiner.mining = false;
        GPUMiner.mining = false;
        mining = false;
        miners.clear();
        gpuMiners.clear();
    }
}

