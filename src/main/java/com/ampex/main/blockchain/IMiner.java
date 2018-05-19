package com.ampex.main.blockchain;

public interface IMiner {


    void start();

    void setName(String name);

    void interrupt();

    void setup(int index);

    long getHashrate();

    /**
     * Sets intensity of miner. Higher intensity may net higher hashrate but may also crash the miner
     *
     * @param intensity Positive intensity value, this is read as a percentage. I.E. 100 = 100%, which is normal intensity
     */
    void setIntensity(double intensity);

    void shutdown();

}
