
import com.lifeform.main.transactions.Address;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AddressTest {

    @Test
    public void testAdd() {
        String pre = "1274AaHXvkq468t2IlxsHJlsrslv17cw3vwYFnJmA==5734";
        Address strict = Address.decodeFromChain(pre);
        assertNotNull(strict.toByteArrayStrict());
        String add = null;
        try {
            add = new String("127TPt3EbfNyQAIH1I2+bM2mfpTXIsag90x1X7tA==8228".getBytes("UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Address a = Address.decodeFromChain(add);

        System.out.println("Add1: " + a.encodeForChain());
        Address b = Address.fromByteArray(a.toByteArray());
        System.out.println("Add2: " + b.encodeForChain());


        assertEquals(b.encodeForChain(), add);
        assertEquals(a.encodeForChain(), b.encodeForChain());
    }
}
