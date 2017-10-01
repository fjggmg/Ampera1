package com.lifeform.main.blockchain;

import com.aparapi.Kernel;
import com.lifeform.main.data.EncryptionManager;

public class GPUKernel extends Kernel {

    public GPUKernel(byte[] data) {
        this.data = data;
    }

    private byte[] data;
    private int[] counter = {0, 1};
    int i = 0;
    int i2 = 1;
    boolean run = true;

    @Override
    public void run() {
        EncryptionManager.sha512NoNew(data);


    }

    public void results() {

    }
}
