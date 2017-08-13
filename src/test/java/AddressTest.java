import com.lifeform.main.transactions.Address;
import com.lifeform.main.transactions.AddressManager;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by Bryan on 8/10/2017.
 */
public class AddressTest {
    DummyKi ki = new DummyKi();

    @Test
    public void test()
    {
        Address a = new AddressManager(ki).getNewAdd();

        String s = a.encodeForChain();
        System.out.println(s);
        Address b = Address.decodeFromChain(s);
        Assert.assertTrue(b.isValid());
        System.out.println(b.encodeForChain());


    }
}
