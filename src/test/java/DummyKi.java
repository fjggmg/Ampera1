import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.blockchain.ChainManager;
import com.lifeform.main.blockchain.IChainMan;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.IEncryptMan;
import com.lifeform.main.data.Options;
import com.lifeform.main.transactions.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.bitbucket.backspace119.generallib.Logging.ConsoleLogger;
import org.bitbucket.backspace119.generallib.Logging.LogMan;
import org.bitbucket.backspace119.generallib.io.network.NetworkManager;

/**
 * Created by Bryan on 7/17/2017.
 */
public class DummyKi implements IKi {

    private IChainMan chainMan = new ChainManager(this,ChainManager.POW_CHAIN);
    private ITransMan transMan = new TransactionManager(this);
    private IEncryptMan encryptMan = new EncryptionManager(this);
    private LogMan logMan;
    private Logger main;
    public DummyKi(){logMan = new LogMan(new ConsoleLogger());
        main = logMan.createLogger("Main","console", Level.DEBUG);
        chainMan.loadChain();
        try {
            getEncryptMan().loadKeys();
        } catch (Exception ex) {
            getEncryptMan().generateKeys();
            getEncryptMan().saveKeys();
        }
    }
    public DummyKi(IChainMan chainMan, ITransMan transMan, IEncryptMan encryptMan, LogMan logMan)
    {
        this.chainMan = chainMan;

        this.transMan = transMan;
        this.encryptMan = encryptMan;
        this.logMan = logMan;
        logMan = new LogMan(new ConsoleLogger());
        main = logMan.createLogger("Main","console", Level.DEBUG);

    }
    @Override
    public void start() {

    }

    @Override
    public Options getOptions() {
        return null;
    }

    @Override
    public IChainMan getChainMan() {
        return chainMan;
    }

    @Override
    public ITransMan getTransMan() {
        return transMan;
    }

    @Override
    public IEncryptMan getEncryptMan() {
        return encryptMan;
    }

    @Override
    public LogMan getLogMan() {
        return logMan;
    }

    @Override
    public Logger getMainLog() { return main;}

    @Override
    public NetworkManager getNetMan() {
        return null;
    }

    public void close(){
        chainMan.close();
        transMan.close();
    }

    @Override
    public boolean isRelay() {
        return false;
    }

    @Override
    public String getRelayer() {
        return null;
    }

    @Override
    public void setRelayer(String relayer) {

    }

    @Override
    public void blockTick(Block block) {

    }



    @Override
    public IAddMan getAddMan() {
        return null;
    }

    @Override
    public void debug(String s) {

    }
}
