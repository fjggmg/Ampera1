package com.ampex.main.blockchain.mining;

import com.ampex.main.IKi;
import gpuminer.miner.context.ContextMaster;
import gpuminer.miner.context.DeviceContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

public class MinerManager implements IMinerMan {

    private IKi ki;
    private boolean mDebug;
    private boolean miningCompatible = true;
    private List<String> disabledDevNames = new ArrayList<>();
    private Map<String, Long> hashrates = new HashMap<>();
    private boolean setup = false;
    private ContextMaster cm;

    public MinerManager(IKi ki, boolean mDebug) {
        this.ki = ki;
        this.mDebug = mDebug;

    }

    @Override
    public void setup() {
        try {

            GPUMiner.clear();
            ContextMaster platforms = new ContextMaster();
            devNames.clear();

            cm = platforms;
            int i = 0;
            for (DeviceContext jcacq : platforms.getContexts()) {
                devNames.add(jcacq.getDInfo().getDeviceName() + " #" + i);
                i++;
            }
            //JOCLContextAndCommandQueue.setWorkaround(false);
            //JOCLDevices.setDeviceFilter(JOCLConstants.ALL_DEVICES);
            //platforms = new ContextMaster();
            ocls = GPUMiner.init(ki, platforms);
            //since we stopped using CPUs, we don't need this next part anymore
            /*for (DeviceContext jcacq : platforms.getContexts()) {

                if (!devNames.contains(jcacq.getDInfo().getDeviceName())) {
                    disabledDevNames.add(jcacq.getDInfo().getDeviceName());
                }
            }*/

        } catch (Exception e) {
            if (mDebug) {
                e.printStackTrace();
                ki.debug("Message: " + e.getMessage());
                ki.debug("Previous errors are from miner startup, this system is not compatible with the mining program, disabling mining. Contact support with this error and your hardware info if you believe yours is compatible");
            }
            miningCompatible = false;
        } finally {
            setup = true;
        }
    }

    @Override
    public boolean isSetup() {
        return setup;
    }

    @Override
    public ContextMaster getContextMaster() {
        return cm;
    }

    @Override
    public long getHashrate(String dev) {
        try {
            return hashrates.get(dev);
        } catch (NullPointerException e) {
            return 0;
        }
    }

    @Override
    public void shutdown() {
        for (IMiner miner : miners) {
            miner.shutdown();
        }
        GPUMiner.clear();
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
    public List<String> getDevNames() {
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

    @Override
    public long cumulativeHashrate() {
        long rate = 0;
        for (Map.Entry<String, Long> dev : hashrates.entrySet()) {
            if (!disabledDevNames.contains(dev.getKey()))
                rate += dev.getValue();
        }
        return rate;
    }

    @Override
    public void setHashrate(String dev, long rate) {
        hashrates.put(dev, rate);
    }

    private boolean isRestarting = false;

    /**
     * non blocking
     */
    @Override
    public void restartMiners() {
        while (isRestarting) {
            try {
                sleep(5L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        isRestarting = true;

        stopMiners();

        startMiners();
        isRestarting = false;

    }

    @Override
    public List<IMiner> getMiners() {
        return miners;
    }

    @Override
    public boolean isMining() {
        return mining;
    }

    private ArrayList<GPUMiner> gpuMiners = new ArrayList<GPUMiner>();

    @Override
    public void startMiners() {
        if (!miningCompatible) return;
        while (!GPUMiner.initDone) {
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (ki.getOptions().mining) {
            mining = true;
            if (!gpuMiners.isEmpty()) {
                for (IMiner miner : gpuMiners) {
                    miner.interrupt();
                }
                gpuMiners.clear();
            }

            for (int i = 0; i < ocls; i++) {
                GPUMiner miner = new GPUMiner(ki, i);
                miner.setup(i);
                miner.setName("Miner#" + i);
                gpuMiners.add(miner);
                miner.start();
            }
        }
    }

    @Override
    public void stopMiners() {
        //ki.debug("Stop miners called");
        for (IMiner miner : miners) {
            miner.interrupt();
        }
        for (IMiner miner : gpuMiners) {
            miner.interrupt();
        }
        mining = false;
        miners.clear();
        gpuMiners.clear();
    }
}

