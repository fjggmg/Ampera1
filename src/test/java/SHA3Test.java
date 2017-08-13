import com.lifeform.main.data.EncryptionManager;
import org.junit.Test;

/**
 * Created by Bryan on 8/10/2017.
 */
public class SHA3Test {

    @Test
    public void test()
    {
        String s = "abc";

        System.out.println(EncryptionManager.sha512(s));
    }
}
