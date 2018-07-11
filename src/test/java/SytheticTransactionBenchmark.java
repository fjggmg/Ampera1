import com.ampex.amperabase.*;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.transactions.*;
import com.ampex.main.transactions.addresses.InvalidAddressException;
import com.ampex.main.transactions.addresses.NewAdd;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SytheticTransactionBenchmark {

    @Test
    public void syntheticBench()
    {
        ExecutorService executor = Executors.newWorkStealingPool();
        DummyBCEKi ki = new DummyBCEKi();
        String message = "Synthetic test";
        List<IOutput> outputs = new ArrayList<>();
        List<IInput> inputs = new ArrayList<>();
        Map<String,IKSEP> keySigMap = new HashMap<>();

        EncryptionManager em = new EncryptionManager(ki);
        em.generateKeys();
        em.generateEDKeys();

        KeyType kt = KeyType.BRAINPOOLP512T1;
        IAddress receiver = null;
        try {
            receiver = NewAdd.createNew(em.getPublicKeyString(kt),"SynTestEnt1",AddressLength.SHA256,false,kt);
        } catch (InvalidAddressException e) {
            e.printStackTrace();
            fail();
            return;
        }
        BigInteger amount = BigInteger.valueOf(100_000_000);


        IInput input = new Input(EncryptionManager.sha3256("InputSynTest1"),0,amount,receiver,Token.ORIGIN,System.currentTimeMillis(),(byte)2);
        IInput input2 = new Input(EncryptionManager.sha3256("InputSynTest2"),0,amount,receiver,Token.ORIGIN,System.currentTimeMillis(),(byte)2);
        IInput input3 = new Input(EncryptionManager.sha3256("InputSynTest3"),0,amount,receiver,Token.ORIGIN,System.currentTimeMillis(),(byte)2);
        IInput input4 = new Input(EncryptionManager.sha3256("InputSynTest4"),0,amount,receiver,Token.ORIGIN,System.currentTimeMillis(),(byte)2);

        inputs.add(input);
        inputs.add(input2);
        inputs.add(input3);
        inputs.add(input4);

        Output output = new Output(amount,receiver,Token.ORIGIN,0,System.currentTimeMillis(),(byte)2);
        Output output2 = new Output(amount,receiver,Token.ORIGIN,1,System.currentTimeMillis(),(byte)2);

        outputs.add(output);
        outputs.add(output2);

        List<String> sInputs = new ArrayList<>();
        for(IInput i:inputs)
        {
            sInputs.add(i.getID());
        }
        keySigMap.put(em.getPublicKeyString(kt),new KeySigEntropyPair(null,"SynTestEnt1",sInputs,null,false, kt));
        ITrans trans;
        try {
            trans = new NewTrans(message,outputs,inputs,keySigMap,TransactionType.NEW_TRANS);
            trans.addSig(em.getPublicKeyString(kt), Utils.toBase64(em.sign(trans.toSignBytes(), kt)));
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
            fail();
            return;
        }

        List<ITrans> toVerify = new ArrayList<>();
        for(int i = 0; i < 50_000; i++)
        {
            toVerify.add(NewTrans.fromAmplet(trans.serializeToAmplet()));
        }

        ITransMan transMan = new NoDiskTransactionManager(ki,false);
        System.out.println("Number of transactions to verify: " + toVerify.size());
        long start = System.currentTimeMillis();
        for(ITrans t:toVerify)
        {
            VerifierThread vt = new VerifierThread();
            vt.init(t,transMan);
            //vt.run();
            executor.execute(vt);
            //vt.start();
        }
        executor.shutdown();
        while(!executor.isTerminated()){}
        //while(VerifierThread.doneList.size() < 49_900) {}
        long stop = System.currentTimeMillis();

        System.out.println("Verification took: " + (stop - start) + " ms");

    }
    private static class VerifierThread extends Thread {
        //public static volatile List<String> doneList = new ArrayList<>();
        private ITrans trans;
        private ITransMan transMan;
        public void init(ITrans trans, ITransMan transMan)
        {
            this.trans = trans;
            this.transMan = transMan;
        }
        public void run()
        {
            if(!transMan.verifyTransaction(trans))
            {
                System.out.println("Verification failed");
                fail();
            }else{
                //doneList.add(getName());
                //increment();
            }
        }
    }
}
