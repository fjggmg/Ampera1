package com.lifeform.main;

import com.lifeform.main.blockchain.IChainMan;
import com.lifeform.main.data.IEncryptMan;
import com.lifeform.main.data.Options;
import com.lifeform.main.network.NetMan;
import com.lifeform.main.transactions.ITransMan;
import org.apache.logging.log4j.Logger;
import org.bitbucket.backspace119.generallib.Logging.LogMan;
import org.bitbucket.backspace119.generallib.io.network.NetworkManager;

/**
 * Created by Bryan on 5/10/2017.
 */
public interface IKi {
    void start();

    Options getOptions();
    IChainMan getChainMan();
    ITransMan getTransMan();
    IEncryptMan getEncryptMan();
    LogMan getLogMan();
    Logger getMainLog();
    NetworkManager getNetMan();
    void close();
    boolean isRelay();
    String getRelayer();
    void setRelayer(String relayer);
}
