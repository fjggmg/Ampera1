import com.lifeform.main.IKi;
import com.lifeform.main.NewGUI;
import com.lifeform.main.Settings;
import com.lifeform.main.StringSettings;
import com.lifeform.main.adx.ExchangeManager;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.blockchain.IChainMan;
import com.lifeform.main.blockchain.IMinerMan;
import com.lifeform.main.blockchain.IStateManager;
import com.lifeform.main.data.IEncryptMan;
import com.lifeform.main.data.Options;
import com.lifeform.main.network.INetworkManager;
import com.lifeform.main.network.pool.PoolData;
import com.lifeform.main.transactions.IAddMan;
import com.lifeform.main.transactions.ITransMan;
import com.lifeform.main.transactions.scripting.ScriptManager;
import engine.ByteCodeEngine;
import mining_pool.Pool;
import org.apache.logging.log4j.Logger;

public class DummyBCEKi implements IKi {
    public ByteCodeEngine bce;

    @Override
    public void start() {

    }

    @Override
    public Options getOptions() {
        return null;
    }

    @Override
    public IChainMan getChainMan() {
        return null;
    }

    @Override
    public ITransMan getTransMan() {
        return null;
    }

    @Override
    public IEncryptMan getEncryptMan() {
        return null;
    }

    @Override
    public Logger getMainLog() {
        return null;
    }

    @Override
    public INetworkManager getNetMan() {
        return null;
    }

    @Override
    public void close() {

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

    @Override
    public NewGUI getGUIHook() {
        return null;
    }

    @Override
    public void setGUIHook(NewGUI guiHook) {

    }

    @Override
    public IMinerMan getMinerMan() {
        return null;
    }

    @Override
    public void restartNetwork() {

    }

    @Override
    public IStateManager getStateManager() {
        return null;
    }

    @Override
    public void resetLite() {

    }

    @Override
    public Pool getPoolManager() {
        return null;
    }

    @Override
    public boolean getSetting(Settings setting) {
        return false;
    }

    @Override
    public void setSetting(Settings setting, boolean set) {

    }

    @Override
    public PoolData getPoolData() {
        return null;
    }

    @Override
    public void newTransPool() {

    }

    @Override
    public INetworkManager getPoolNet() {
        return null;
    }

    @Override
    public String getStringSetting(StringSettings setting) {
        return null;
    }

    @Override
    public void setStringSetting(StringSettings setting, String value) {

    }

    @Override
    public ByteCodeEngine getBCE8() {
        return bce;
    }

    @Override
    public ExchangeManager getExMan() {
        return null;
    }

    @Override
    public ScriptManager getScriptMan() {
        return null;
    }
}
