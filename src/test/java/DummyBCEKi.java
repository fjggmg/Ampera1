import com.ampex.main.GUI.FXGUI;
import com.ampex.main.GUI.NewGUI;
import com.ampex.main.IKi;
import com.ampex.main.Settings;
import com.ampex.main.StringSettings;
import com.ampex.main.adx.ExchangeManager;
import com.ampex.main.blockchain.Block;
import com.ampex.main.blockchain.IChainMan;
import com.ampex.main.blockchain.IStateManager;
import com.ampex.main.blockchain.mining.IMinerMan;
import com.ampex.main.data.buckets.Options;
import com.ampex.main.data.encryption.IEncryptMan;
import com.ampex.main.network.INetworkManager;
import com.ampex.main.pool.PoolData;
import com.ampex.main.transactions.ITransMan;
import com.ampex.main.transactions.addresses.IAddMan;
import com.ampex.main.transactions.scripting.ScriptManager;
import engine.ByteCodeEngine;
import mining_pool.Pool;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;

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

    @Override
    public void setStartHeight(BigInteger startHeight) {

    }

    @Override
    public Object getCloseLock() {
        return null;
    }

    @Override
    public void downloadedTo(BigInteger height) {

    }

    @Override
    public BigInteger getLoadHeight() {
        return null;
    }

    @Override
    public BigInteger getDownloadedTo() {
        return null;
    }

    @Override
    public BigInteger getStartHeight() {
        return null;
    }

    @Override
    public String getVersion() {
        return "TESTING";
    }

    @Override
    public void setInnerGUIRef(FXGUI ref) {

    }
}
