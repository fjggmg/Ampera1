package com.ampex.main.blockchain;

import com.ampex.amperabase.IBlockAPI;

public interface IBlockVerificationHelper {

    boolean verifyTransactions();
    void init(IBlockAPI block);
    boolean addTransactions();
}
