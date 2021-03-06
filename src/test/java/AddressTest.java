
import com.ampex.amperabase.AddressLength;
import com.ampex.amperabase.IAddress;
import com.ampex.amperabase.KeyType;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.transactions.addresses.Address;
import com.ampex.main.transactions.addresses.InvalidAddressException;
import com.ampex.main.transactions.addresses.NewAdd;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

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
        em.generateEDKeys();
        IAddress na = null;
        try {
            na = NewAdd.createNew(em.getPublicKeyString(KeyType.ED25519), "Some entropy", AddressLength.SHA224, true, KeyType.ED25519);
        } catch (InvalidAddressException e) {
            e.printStackTrace();
        }
        assertNotNull(na);
        assertTrue(na.isValid());
        assertTrue(na.canSpend(em.getPublicKeyString(KeyType.ED25519), "Some entropy", true, KeyType.ED25519));
        System.out.println("NewAdd: " + na.encodeForChain());

        try {
            na = NewAdd.createNew(em.getPublicKeyString(KeyType.ED25519), "Some entropy", AddressLength.SHA512, "AMPEX", true, KeyType.ED25519);
        } catch (InvalidAddressException e) {
            e.printStackTrace();
        }
        System.out.println("NewAdd Prefixed: " + na.encodeForChain());
        assertNotNull(na);
        assertTrue(na.isValid());
        assertTrue(na.canSpendPrefixed(em.getPublicKeyString(KeyType.ED25519), "Some entropy", "AMPEX", true, KeyType.ED25519));
        NewAdd badAdd = null;
        try {
            badAdd = new NewAdd((byte) 125, "33NZkUcUiDLp1Bi7SOVKQUdQH7zHn7czs/4CBw", "vg7DFZd4", AddressLength.SHA224.getIndicator(), true, KeyType.ED25519);
        } catch (InvalidAddressException e) {
            e.printStackTrace();
        }
        assertNotNull(badAdd);
        assertFalse(badAdd.isValid());
        IAddress nb = NewAdd.fromByteArray(na.toByteArray());
        System.out.println("Na: " + na.encodeForChain());

        assertNotNull(nb);
        System.out.println("Nb: " + nb.encodeForChain());
        assertTrue(nb.hasPrefix());
        assertEquals(na.encodeForChain(), nb.encodeForChain());
        IAddress nc = NewAdd.decodeFromChain(nb.encodeForChain());
        assertNotNull(nc);
        assertTrue(nc.isValid());
        assertEquals(nb.encodeForChain(), nc.encodeForChain());

        try {
            na = NewAdd.createNew(em.getPublicKeyString(KeyType.ED25519), "Some entropy", AddressLength.SHA512, true, KeyType.ED25519);
        } catch (InvalidAddressException e) {
            e.printStackTrace();
        }
        assertEquals(na.encodeForChain(), Address.fromByteArray(na.toByteArray()).encodeForChain());
        //assertEquals(data.get(na.toByteArray()),"na");
        //assertEquals(data.get(nb.toByteArray()),"nb");
        //assertEquals(data.get(nc.toByteArray()),"nc");

    }


}
