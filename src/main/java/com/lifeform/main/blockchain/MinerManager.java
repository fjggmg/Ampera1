package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class MinerManager implements IMinerMan{

    private IKi ki;
    private boolean mDebug;

    public MinerManager(IKi ki, boolean mDebug)
    {
        this.ki = ki;
        this.mDebug = mDebug;
    }

    private int previousCount = 0;
    private List<IMiner> miners = new ArrayList<>();
    private boolean mining = false;

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
    @Override
    public void startMiners(int count) {
        if(ki.getOptions().mining) {
            previousCount = count;
            mining = true;
            if (mining) {
                CPUMiner.mining = true;
                CPUMiner.foundBlock = false;

                BigInteger guess = BigInteger.ZERO;
                for (int i = 0; i < count; i++) {
                    if (mDebug)
                        ki.getMainLog().info("Starting miner: " + i);
                    CPUMiner miner = new CPUMiner(ki, guess, guess.add(BigInteger.valueOf(1000000L)), mDebug);
                    miner.setName("Miner" + i);
                    guess = guess.add(BigInteger.valueOf(1000000L));
                    miners.add(miner);
                    miner.start();
                }

            }
        }
    }

    @Override
    public void stopMiners() {
        for(IMiner miner:miners)
        {
            ((CPUMiner)miner).interrupt();
        }
        CPUMiner.mining = false;
        mining = false;
        miners.clear();
    }
}
