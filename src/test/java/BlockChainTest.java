import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.Utils;
import com.lifeform.main.transactions.MKiTransaction;
import org.bitbucket.backspace119.generallib.io.network.Packet;
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
        block.ID = EncryptionManager.sha256(block.header());
        Assert.assertTrue(ki.getChainMan().addBlock(block));
        Block block2 = new Block();
        block2.timestamp = System.currentTimeMillis();
        block2.height = BigInteger.valueOf(1L);
        block2.prevID = block.ID;
        block2.solver = Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded());

        block2.height = ki.getChainMan().currentHeight().add(BigInteger.ONE);
        block2.ID = EncryptionManager.sha256(block2.header());
        Block block2Carry = Block.fromJSON(block2.toJSON());
        Assert.assertTrue(ki.getChainMan().addBlock(block2Carry));



    }

    @Test
    public void testTransactionOfGenesis()
    {

        if(ki.getChainMan().currentHeight().compareTo(BigInteger.valueOf(-1L)) == 0) {


            Block block = new Block();


            block.solver = Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded());
            block.timestamp = System.currentTimeMillis();
            block.height = ki.getChainMan().currentHeight().add(BigInteger.ONE);
            if (!(ki.getChainMan().currentHeight().compareTo(BigInteger.valueOf(-1L)) == 0))
                block.prevID = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID;
            else
                block.prevID = "0";
            block.ID = EncryptionManager.sha256(block.header());
            Assert.assertTrue(ki.getChainMan().addBlock(block));
        }


        MKiTransaction trans = new MKiTransaction();
        trans.inputs.put(ki.getChainMan().getByHeight(BigInteger.ZERO).ID,null);
        trans.receiver = Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded());
        trans.relayFee = BigInteger.TEN;
        trans.transactionFee = BigInteger.TEN;

        trans.amount = BigInteger.valueOf(3000000000000000L);
        trans.change = BigInteger.valueOf(3800000000000000L).subtract(trans.transactionFee.add(trans.relayFee).add(trans.amount));

        trans.height = ki.getChainMan().currentHeight();
        trans.ID = EncryptionManager.sha256(trans.preSigAll());
        trans.relayer = Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded());
        trans.sender = Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded());
        trans.preSig = ki.getEncryptMan().sign(trans.preSigAll());
        trans.relaySignature = ki.getEncryptMan().sign(trans.preSigAll());
        trans.signature = ki.getEncryptMan().sign(trans.all());


        System.out.println("Block 0 id: " + ki.getChainMan().getByHeight(BigInteger.ZERO).ID);
        Assert.assertTrue(ki.getTransMan().verifyTransaction(trans));


        Block block3 = new Block();
        block3.timestamp = System.currentTimeMillis();
        block3.height = ki.getChainMan().currentHeight().add(BigInteger.ONE);
        block3.solver = Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded());
        if (!(ki.getChainMan().currentHeight().compareTo(BigInteger.valueOf(-1L)) == 0))
            block3.prevID = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID;
        else
            block3.prevID = "0";

        block3.addTransaction(trans);
        block3.ID = EncryptionManager.sha256(block3.header());

        Assert.assertTrue(ki.getChainMan().addBlock(block3));





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
            block.ID = EncryptionManager.sha256(block.header());
            while (!ki.getChainMan().addBlock(block)) {
                block.timestamp = System.currentTimeMillis();
                block.payload = guess.toString();

                guess = guess.add(BigInteger.ONE);
                block.ID = EncryptionManager.sha256(block.header());
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
