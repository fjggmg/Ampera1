import com.lifeform.main.data.Utils;
import org.junit.Test;

/**
 * Created by Bryan on 8/8/2017.
 */
public class B64Test {

    /**
     * just a dummy test to work out how Java's new Base64 util works
     */
    @Test
    public void test()
    {
        String test = "Hello";

        String encoded = Utils.toBase64(test.getBytes());
        System.out.println(encoded);
        String test2 = new String(Utils.fromBase64(encoded));
        System.out.println(test2);
    }
}
