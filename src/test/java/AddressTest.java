
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.transactions.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AddressTest {

    @Test
    public void testAdd() {
        String pre = "1274AaHXvkq468t2IlxsHJlsrslv17cw3vwYFnJmA==5734";
        IAddress strict = Address.decodeFromChain(pre);
        assertNotNull(((Address) strict).toByteArrayStrict());
        String add = null;
        try {
            add = new String("127TPt3EbfNyQAIH1I2+bM2mfpTXIsag90x1X7tA==8228".getBytes("UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        IAddress a = Address.decodeFromChain(add);

        System.out.println("Add1: " + a.encodeForChain());
        IAddress b = Address.fromByteArray(a.toByteArray());
        System.out.println("Add2: " + b.encodeForChain());


        assertEquals(b.encodeForChain(), add);
        assertEquals(a.encodeForChain(), b.encodeForChain());
    }

    @Test
    public void newAddTest() {
        EncryptionManager em = new EncryptionManager(null);
        em.generateEDkeys();
        IAddress na = null;
        try {
            na = NewAdd.createNew(em.getPublicKeyString(), "Some entropy", AddressLength.SHA224, true);
        } catch (InvalidAddressException e) {
            e.printStackTrace();
        }
        assertNotNull(na);
        assertTrue(na.isValid());
        assertTrue(na.canSpend(em.getPublicKeyString(), "Some entropy", true));
        System.out.println("NewAdd: " + na.encodeForChain());

        try {
            na = NewAdd.createNew(em.getPublicKeyString(), "Some entropy", AddressLength.SHA512, "AMPEX", true);
        } catch (InvalidAddressException e) {
            e.printStackTrace();
        }
        System.out.println("NewAdd Prefixed: " + na.encodeForChain());
        assertNotNull(na);
        assertTrue(na.isValid());
        assertTrue(na.canSpendPrefixed(em.getPublicKeyString(), "Some entropy", "AMPEX", true));
        NewAdd badAdd = null;
        try {
            badAdd = new NewAdd((byte) 125, "33NZkUcUiDLp1Bi7SOVKQUdQH7zHn7czs/4CBw", "vg7DFZd4", AddressLength.SHA224.getIndicator(), true);
        } catch (InvalidAddressException e) {
            e.printStackTrace();
        }
        assertNotNull(badAdd);
        assertFalse(badAdd.isValid());
        IAddress nb = NewAdd.fromByteArray(na.toByteArray());

        assertNotNull(nb);
        assertTrue(nb.hasPrefix());
        assertEquals(na.encodeForChain(), nb.encodeForChain());
        IAddress nc = NewAdd.decodeFromChain(nb.encodeForChain());
        assertNotNull(nc);
        assertTrue(nc.isValid());
        assertEquals(nb.encodeForChain(), nc.encodeForChain());

        try {
            na = NewAdd.createNew(em.getPublicKeyString(), "Some entropy", AddressLength.SHA512, true);
        } catch (InvalidAddressException e) {
            e.printStackTrace();
        }
        assertEquals(na.encodeForChain(), Address.fromByteArray(na.toByteArray()).encodeForChain());
        //assertEquals(data.get(na.toByteArray()),"na");
        //assertEquals(data.get(nb.toByteArray()),"nb");
        //assertEquals(data.get(nc.toByteArray()),"nc");

    }


}
