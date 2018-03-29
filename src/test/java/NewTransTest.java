import amp.Amplet;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.transactions.*;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NewTransTest {

    @Test
    public void testTrans() {
        EncryptionManager em = new EncryptionManager(null);
        em.generateKeys();
        IAddress add = null;
        try {
            add = NewAdd.createNew(em.getPublicKeyString(), "fake entropy", AddressLength.SHA256, false);
        } catch (InvalidAddressException e) {
            e.printStackTrace();
            return;
        }
        Output o = new Output(BigInteger.valueOf(10000000L), add, Token.ORIGIN, 0, System.currentTimeMillis(), (byte) 2);
        Input i = new Input(o.getID(), 0, BigInteger.valueOf(10000000L), add, Token.ORIGIN, System.currentTimeMillis());
        List<Output> outputs = new ArrayList<>();
        outputs.add(o);
        List<Input> inputs = new ArrayList<>();
        inputs.add(i);
        Map<String, KeySigEntropyPair> keySigMap = new HashMap<>();
        List<String> ins = new ArrayList<>();
        ins.add(i.getID());
        keySigMap.put(em.getPublicKeyString(), new KeySigEntropyPair("sig", "fake entropy", ins, null, false));
        try {
            ITrans ntrans = new NewTrans("hello, is it me you're looking for", outputs, inputs, keySigMap, TransactionType.NEW_TRANS);
            ITrans des = Transaction.fromAmplet(Amplet.create(ntrans.serializeToAmplet().serializeToBytes()));
            assertNotNull(des);
            assertTrue(des.getOutputs().size() == 1);
            assertTrue(des.getInputs().size() == 1);
            assertTrue(des.getMessage().equals(ntrans.getMessage()));
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
        }
    }
}
