import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.ChainManager;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

/**
 * Created by Bryan on 7/31/2017.
 */
public class DiffTest {

    private IKi ki = new DummyKi();

    @Test
    public void testDiff()
    {
        BigInteger diff = ki.getChainMan().getCurrentDifficulty();

        diff = ki.getChainMan().calculateDiff(diff,3L);
        diff = ki.getChainMan().calculateDiff(diff,3L);
        diff = ki.getChainMan().calculateDiff(diff,3L);
        diff = ki.getChainMan().calculateDiff(diff,3L);
        diff = ki.getChainMan().calculateDiff(diff,3L);
        diff = ki.getChainMan().calculateDiff(diff,3L);
        diff = ki.getChainMan().calculateDiff(diff,3L);
        diff = ki.getChainMan().calculateDiff(diff,3L);
        diff = ki.getChainMan().calculateDiff(diff,3L);

        System.out.println("Current diff: " + ki.getChainMan().getCurrentDifficulty().divide(BigInteger.valueOf(3000000L)));
        System.out.println("Calcula diff: " + diff.toString());
        Assert.assertTrue(diff.compareTo(ki.getChainMan().getCurrentDifficulty().divide(BigInteger.valueOf(3000000L))) == 0);

    }
}
