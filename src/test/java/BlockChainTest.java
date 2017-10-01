import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.Utils;
import org.junit.*;

import java.math.BigInteger;

/**
 * Created by Bryan on 7/17/2017.
 */
public class BlockChainTest {

    static  IKi ki;

    @BeforeClass
    public static void firstSetup()
    {
        ki = new DummyKi();
        ki.getChainMan().clearFile();
        ki.close();
    }
    @Before
    public void setup()
    {
        ki = new DummyKi();
    }

    @After
    public void tearDown()
    {
        ki.close();
    }


    @Test
    public void testBadBlock()
    {



        Block block = new Block();
        block.timestamp = System.currentTimeMillis();
        block.prevID = "NOTPREVIOUS";
        block.ID = "0F0A0FE3F2F4";
        block.height = BigInteger.ZERO;
        block.solver = Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded());
        Assert.assertFalse(ki.getChainMan().addBlock(block));



    }



    @Test
    public void testGoodBlock()
    {


        Block block = new Block();


        block.timestamp = System.currentTimeMillis();
        block.height = ki.getChainMan().currentHeight().add(BigInteger.ONE);

        if (!(ki.getChainMan().currentHeight().compareTo(BigInteger.valueOf(-1L)) == 0))
            block.prevID = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID;
        else
            block.prevID = "0";
        block.solver = Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded());
        block.ID = EncryptionManager.sha512(block.header());
        Assert.assertTrue(ki.getChainMan().addBlock(block));
        Block block2 = new Block();
        block2.timestamp = System.currentTimeMillis();
        block2.height = BigInteger.valueOf(1L);
        block2.prevID = block.ID;
        block2.solver = Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded());

        block2.height = ki.getChainMan().currentHeight().add(BigInteger.ONE);
        block2.ID = EncryptionManager.sha512(block2.header());
        Block block2Carry = Block.fromJSON(block2.toJSON());
        Assert.assertTrue(ki.getChainMan().addBlock(block2Carry));



    }

    @Test
    public void testTransactionOfGenesis()
    {


    }

    @Test
    public void getOldHeight() {

        int i = 0;
        while (i < 100) {
            Block block = new Block();
            block.solver = Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded());

            block.timestamp = System.currentTimeMillis();
            //FUCK THIS NEEDS FIXING ANYWAY = ki.getTransMan().getPending();
            block.height = ki.getChainMan().currentHeight().add(BigInteger.ONE);
            if (!(ki.getChainMan().currentHeight().compareTo(BigInteger.valueOf(-1L)) == 0))
                block.prevID = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID;
            else
                block.prevID = "0";

            BigInteger guess = BigInteger.ZERO;
            block.ID = EncryptionManager.sha512(block.header());
            while (!ki.getChainMan().addBlock(block)) {
                block.timestamp = System.currentTimeMillis();
                block.payload = guess.toString();

                guess = guess.add(BigInteger.ONE);
                block.ID = EncryptionManager.sha512(block.header());
            }
            i++;
        }

        Assert.assertTrue(ki.getChainMan().getByHeight(BigInteger.ZERO).height.compareTo(BigInteger.ZERO) == 0);

    }


    @Deprecated
    public void testLoad()
    {


        Block b = new Block();
        b.solver = Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded());
        b.timestamp = System.currentTimeMillis();
        //b.transactions = ki.getTransMan().getPending();
        b.height = ki.getChainMan().currentHeight().add(BigInteger.ONE);
        b.prevID = "" + BigInteger.ZERO;

        ki.getChainMan().addBlock(b);



    }


    @Test
    public void testSaveLoadKeys()
    {


        String pubKey = Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded());


        ki.getEncryptMan().saveKeys();
        ki.getEncryptMan().loadKeys();

        Assert.assertTrue(pubKey.equalsIgnoreCase(Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded())));

    }



}
