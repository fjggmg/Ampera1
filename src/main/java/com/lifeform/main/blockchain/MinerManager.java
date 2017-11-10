package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import gpuminer.JOCL.JOCLConstants;
import gpuminer.JOCL.JOCLContextAndCommandQueue;
import gpuminer.JOCL.JOCLDevices;
import gpuminer.JOCL.JOCLMaster;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class MinerManager implements IMinerMan{

    private IKi ki;
    private boolean mDebug;
    private boolean miningCompatible = true;
    private List<String> disabledDevNames = new ArrayList<>();
    public MinerManager(IKi ki, boolean mDebug)
    {
        this.ki = ki;
        this.mDebug = mDebug;
        try {

            JOCLMaster platforms = new JOCLMaster();
            for (JOCLContextAndCommandQueue jcacq : platforms.getContextsAndCommandQueues()) {
                devNames.add(jcacq.getDInfo().getDeviceName());

            }
            JOCLContextAndCommandQueue.setWorkaround(false);
            JOCLDevices.setDeviceFilter(JOCLConstants.ALL_DEVICES);
            platforms = new JOCLMaster();
            ocls = GPUMiner.init(ki);
            for (JOCLContextAndCommandQueue jcacq : platforms.getContextsAndCommandQueues()) {

                if (!devNames.contains(jcacq.getDInfo().getDeviceName())) {
                    disabledDevNames.add(jcacq.getDInfo().getDeviceName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ki.debug("Previous errors are from miner startup, this system is not compatible with the mining program, disabling mining. Contact support with this error and your hardware info if you believe yours is compatible");
            miningCompatible = false;
        }
    }

    @Override
    public boolean miningCompatible() {
        return miningCompatible;
    }

    private List<String> devNames = new ArrayList<>();
    private List<IMiner> miners = new ArrayList<>();
    private boolean mining = false;
    private int ocls = 0;
    @Override
    public List<String> getDevNames()
    {
        return devNames;
    }

    @Override
    public void enableDev(String dev) {
        disabledDevNames.remove(dev);
        devNames.add(dev);
    }

    @Override
    public void disableDev(String dev) {
        devNames.remove(dev);
        disabledDevNames.add(dev);
    }

    private boolean isRestarting = false;
    /**
     * non blocking
     *
     */
    @Override
    public void restartMiners()
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
                startMiners();
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

    ArrayList<GPUMiner> gpuMiners = new ArrayList<GPUMiner>();

    @Override
    public void startMiners() {
        if (!miningCompatible) return;
        if(ki.getOptions().mining) {
            mining = true;
            /* old miner, OCL miner can use both CPU and GPU now
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
            */
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
                    miner.setName("Miner#" + i);
                    gpuMiners.add(miner);
                    miner.start();
                }
        }
    }

    @Override
    public void stopMiners() {
        for(IMiner miner:miners)
        {
            miner.interrupt();
        }
        //CPUMiner.mining = false; old miner
        GPUMiner.mining = false;
        mining = false;
        miners.clear();
        gpuMiners.clear();
    }
}

