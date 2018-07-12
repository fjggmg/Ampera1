import com.ampex.amperabase.*;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.transactions.*;
import com.ampex.main.transactions.addresses.InvalidAddressException;
import com.ampex.main.transactions.addresses.NewAdd;
import com.ampex.main.transactions.scripting.compiling.CompilerException;
import com.ampex.main.transactions.scripting.compiling.StringCompiler;
import edu.emory.mathcs.backport.java.util.Arrays;
import engine.binary.BinaryFactory;
import engine.binary.IBinary;
import engine.binary.on_ice.Binary;
import engine.data.constant_memory.ConstantMemoryFactory;
import engine.data.jump_memory.JumpMemoryFactory;
import engine.data.writable_memory.WritableMemoryFactory;
import engine.data.writable_memory.on_ice.WritableMemory;
import engine.program.IProgram;
import engine.program.Program;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SytheticTransactionBenchmark {




    //This is an arbitrarily large and difficult script. It has one of the heaviest operators repeated a lot of times to simulate heavy load but the script will always return as "succeeded".
    //This script is much harder than the ADX trading script and should be much harder than any reasonable script encountered.
    private List<String> testScriptCode = Arrays.asList(new String[]{"PI0","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL",
            "VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL",
            "VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL",
            "VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL",
            "VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","CSK","PI0","TERM"});
    @Test
    public void syntheticBench()
    {
        ExecutorService executor = Executors.newWorkStealingPool();
        DummyBCEKi ki = new DummyBCEKi();
        String message = "Synthetic test";
        List<IOutput> outputs = new ArrayList<>();
        List<IInput> inputs = new ArrayList<>();
        Map<String,IKSEP> keySigMap = new HashMap<>();
        byte[] testScriptByteCode;
        try {
            testScriptByteCode = StringCompiler.compile(testScriptCode,ki.getBCE8());
        } catch (CompilerException e) {
            e.printStackTrace();
            fail();
            return;
        }



        EncryptionManager em = new EncryptionManager(ki);
        em.generateKeys();
        em.generateEDKeys();
        IAddress p2shAdd;
        IBinary bin;
        try {
            IProgram p = new Program(testScriptByteCode);
            bin = BinaryFactory.build(p,ConstantMemoryFactory.build(),JumpMemoryFactory.build(),true,1,new byte[]{3,42,-1,23},System.currentTimeMillis(),em.getPublicKey(KeyType.ED25519).getEncoded(),KeyType.ED25519,null,0);
            p2shAdd = NewAdd.createNew(Utils.toBase64(bin.serializeToAmplet().serializeToBytes()),Utils.toBase64(bin.getEntropy()),AddressLength.SHA256, true, KeyType.NONE);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

        KeyType kt = KeyType.ED25519;
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

        IInput p2shInput = new Input(EncryptionManager.sha3256("InputSynTestP2SH1"),0,amount,p2shAdd,Token.ORIGIN,System.currentTimeMillis(),(byte)2);

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

        List<String> sInsP2SH = new ArrayList<>();
        sInsP2SH.add(p2shInput.getID());
        inputs.add(p2shInput);

        keySigMap.put(Utils.toBase64(bin.serializeToAmplet().serializeToBytes()),new KeySigEntropyPair(Utils.toBase64(WritableMemoryFactory.build().serializeToBytes()), Utils.toBase64(bin.getEntropy()),sInsP2SH,null,true, KeyType.NONE));
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
        for(int i = 0; i < 100_000; i++)
        {
            toVerify.add(NewTrans.fromAmplet(trans.serializeToAmplet()));
        }

        ITransMan transMan = new NoDiskTransactionManager(ki,false);
        System.out.println("Number of transactions to verify: " + toVerify.size());
        List<Callable<Boolean>> vts = new ArrayList<>();
        for(ITrans t:toVerify)
        {
            VerifierThread vt = new VerifierThread();
            vt.init(t,transMan);
            vts.add(vt);
        }
        long start = System.currentTimeMillis();
        try {
            executor.invokeAll(vts);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long stop = System.currentTimeMillis();

        System.out.println("Verification took: " + (stop - start) + " ms");

    }
    private static class VerifierThread implements Callable<Boolean> {
        private ITrans trans;
        private ITransMan transMan;
        public void init(ITrans trans, ITransMan transMan)
        {
            this.trans = trans;
            this.transMan = transMan;
        }
        @Override
        public Boolean call()
        {
            if(!transMan.verifyTransaction(trans))
            {
                System.out.println("Verification failed");
                fail();
                return false;
            }else{
                return true;
            }
        }
    }
}
