package com.ampex.main.benchmarking;

import com.ampex.amperabase.*;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.transactions.*;
import com.ampex.main.transactions.addresses.InvalidAddressException;
import com.ampex.main.transactions.addresses.NewAdd;
import com.ampex.main.transactions.scripting.compiling.CompilerException;
import com.ampex.main.transactions.scripting.compiling.StringCompiler;
import engine.binary.BinaryFactory;
import engine.binary.IBinary;
import engine.data.constant_memory.ConstantMemoryFactory;
import engine.data.jump_memory.JumpMemoryFactory;
import engine.data.writable_memory.WritableMemoryFactory;
import engine.program.IProgram;
import engine.program.Program;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class SyntheticTransactionBenchmark {


    public static void main(String[] args)
    {
        SyntheticTransactionBenchmark stb = new SyntheticTransactionBenchmark();
        stb.numberOfTransactions = 100_000;
        stb.useWorstCaseScript = true;
        //stb.useImpossibleScript = true;
        //stb.keyType = KeyType.BRAINPOOLP512T1;
        //stb.scriptOnly = true;
        stb.noDisk = false;
        stb.syntheticBench();
    }

    //This is an arbitrarily large and difficult script. It has one of the heaviest operators repeated a lot of times to simulate heavy load but the script will always return as "succeeded".
    //This script is much harder than the ADX trading script and should be much harder than any script encountered.
    private List<String> impossibleTestScriptCode = Arrays.asList(new String[]{"PI0","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL",
            "VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL",
            "VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL",
            "VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL",
            "VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","CSK","PI0","TERM"});
    private List<String> worstCaseTestScriptCode = Arrays.asList(new String[]{"PI0","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","VNL","CSK","PI0","TERM"});

    public KeyType keyType = KeyType.ED25519;
    public boolean useImpossibleScript = false;
    public boolean useWorstCaseScript = false;
    public boolean scriptOnly = false;
    public boolean noDisk = true;


    public int numberOfTransactions = 50_000;

    public void syntheticBench()
    {
        if(useImpossibleScript && useWorstCaseScript)
        {
            System.out.println("Cannot use impossible and worse case script in benchmarking currently");
            return;
        }
        if(scriptOnly && (!useImpossibleScript && !useWorstCaseScript))
        {
            System.out.println("Please specify which script to use with options");
            return;
        }
        System.out.println("=====Ampera Benchmarking System. Greater than 60s verification time is considered unacceptable=====");
        System.out.println("Settings:");
        System.out.println("Number of Transactions to verify: " + numberOfTransactions);
        System.out.println("Key Types for P2Pk inputs (4) in transactions: " + keyType);
        System.out.println("Add impossible script to transaction for testing: " + useImpossibleScript);
        System.out.println("Add worst case script to transaction for testing: " + useWorstCaseScript);
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("Beginning setup...");
        //ExecutorService executor = Executors.newFixedThreadPool(32);
        ExecutorService executor = Executors.newWorkStealingPool();
        DummyBCEKi ki = new DummyBCEKi();
        String message = "Synthetic test";
        List<IOutput> outputs = new ArrayList<>();
        List<IInput> inputs = new ArrayList<>();
        Map<String,IKSEP> keySigMap = new HashMap<>();
        byte[] testScriptByteCode = null;
        if(useImpossibleScript || useWorstCaseScript)
            try {
                if(useWorstCaseScript)
                    testScriptByteCode = StringCompiler.compile(worstCaseTestScriptCode,ki.getBCE8());
                if(useImpossibleScript)
                    testScriptByteCode = StringCompiler.compile(impossibleTestScriptCode,ki.getBCE8());
            } catch (CompilerException e) {
                e.printStackTrace();
                return;
            }



        EncryptionManager em = new EncryptionManager(ki);
        em.generateKeys();
        em.generateEDKeys();

        IAddress p2shAdd = null;
        IBinary bin = null;
        if(useImpossibleScript || useWorstCaseScript) {
            try {
                IProgram p = new Program(testScriptByteCode);
                bin = BinaryFactory.build(p, ConstantMemoryFactory.build(), JumpMemoryFactory.build(), true, 1, new byte[]{3, 42, -1, 23}, System.currentTimeMillis(), em.getPublicKey(KeyType.ED25519).getEncoded(), KeyType.ED25519, null, 0);
                p2shAdd = NewAdd.createNew(Utils.toBase64(bin.serializeToAmplet().serializeToBytes()), Utils.toBase64(bin.getEntropy()), AddressLength.SHA256, true, KeyType.NONE);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        BigInteger amount = BigInteger.valueOf(100_000_000);

            IAddress receiver = null;
            try {
                receiver = NewAdd.createNew(em.getPublicKeyString(keyType), "SynTestEnt1", AddressLength.SHA256, false, keyType);
            } catch (InvalidAddressException e) {
                e.printStackTrace();
                return;
            }

        if(!scriptOnly) {
            IInput input = new Input(EncryptionManager.sha3256("InputSynTest1"), 0, amount, receiver, Token.ORIGIN, System.currentTimeMillis(), (byte) 2);
            IInput input2 = new Input(EncryptionManager.sha3256("InputSynTest2"), 0, amount, receiver, Token.ORIGIN, System.currentTimeMillis(), (byte) 2);
            IInput input3 = new Input(EncryptionManager.sha3256("InputSynTest3"), 0, amount, receiver, Token.ORIGIN, System.currentTimeMillis(), (byte) 2);
            IInput input4 = new Input(EncryptionManager.sha3256("InputSynTest4"), 0, amount, receiver, Token.ORIGIN, System.currentTimeMillis(), (byte) 2);

            inputs.add(input);
            inputs.add(input2);
            inputs.add(input3);
            inputs.add(input4);
        }else{
            IInput input = new Input(EncryptionManager.sha3256("InputSynTest1"), 0, amount, p2shAdd, Token.ORIGIN, System.currentTimeMillis(), (byte) 2);
            IInput input2 = new Input(EncryptionManager.sha3256("InputSynTest2"), 0, amount, p2shAdd, Token.ORIGIN, System.currentTimeMillis(), (byte) 2);
            IInput input3 = new Input(EncryptionManager.sha3256("InputSynTest3"), 0, amount, p2shAdd, Token.ORIGIN, System.currentTimeMillis(), (byte) 2);
            IInput input4 = new Input(EncryptionManager.sha3256("InputSynTest4"), 0, amount, p2shAdd, Token.ORIGIN, System.currentTimeMillis(), (byte) 2);

            inputs.add(input);
            inputs.add(input2);
            inputs.add(input3);
            inputs.add(input4);
        }

        Output output = new Output(amount,receiver,Token.ORIGIN,0,System.currentTimeMillis(),(byte)2);
        Output output2 = new Output(amount,receiver,Token.ORIGIN,1,System.currentTimeMillis(),(byte)2);

        outputs.add(output);
        outputs.add(output2);
        if(!scriptOnly) {
            List<String> sInputs = new ArrayList<>();
            for (IInput i : inputs) {
                sInputs.add(i.getID());
            }
            keySigMap.put(em.getPublicKeyString(keyType), new KeySigEntropyPair(null, "SynTestEnt1", sInputs, null, false, keyType));
        }

        if(useWorstCaseScript || useImpossibleScript) {
            IInput p2shInput = new Input(EncryptionManager.sha3256("InputSynTestP2SH1"), 0, amount, p2shAdd, Token.ORIGIN, System.currentTimeMillis(), (byte) 2);
            List<String> sInsP2SH = new ArrayList<>();
            if(scriptOnly)
            {
                for(IInput i: inputs)
                {
                    sInsP2SH.add(i.getID());
                }
            }
            sInsP2SH.add(p2shInput.getID());
            inputs.add(p2shInput);

            keySigMap.put(Utils.toBase64(bin.serializeToAmplet().serializeToBytes()), new KeySigEntropyPair(Utils.toBase64(WritableMemoryFactory.build().serializeToBytes()), Utils.toBase64(bin.getEntropy()), sInsP2SH, null, true, KeyType.NONE));
        }
        ITrans trans;
        try {
            trans = new NewTrans(message,outputs,inputs,keySigMap,TransactionType.NEW_TRANS);
            if(!scriptOnly)
            trans.addSig(em.getPublicKeyString(keyType), Utils.toBase64(em.sign(trans.toSignBytes(), keyType)));
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
            return;
        }

        Map<String,TXIOData> prePop = new HashMap<>();
        if(!noDisk)
        {
            for(IInput i:inputs)
            {
                try {
                    prePop.put(i.getID(),new TXIOData(i));
                } catch (InvalidTXIOData invalidTXIOData) {
                    invalidTXIOData.printStackTrace();
                    return;
                }
            }
        }

        ITransMan transMan = new TestingTransactionManager(ki,noDisk,prePop);
        List<Callable<Boolean>> vts = new ArrayList<>();
        for(int i = 0; i < numberOfTransactions; i++)
        {
            VerifierThread vt = new VerifierThread();
            vt.init(NewTrans.fromAmplet(trans.serializeToAmplet()),transMan);
            vts.add(vt);
        }

        System.out.println("Setup complete, beginning benchmark. You may experience heavy CPU load during the test.");
        long start = System.currentTimeMillis();
        //List<Future<Boolean>> results = null;
        try {
            executor.invokeAll(vts);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long stop = System.currentTimeMillis();
        /*
        for(Future<Boolean> future:results)
        {
            try {
                System.out.println("Result: " + future.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        */
        System.out.println("Verification took: " + (stop - start) + " ms");


    }
    private static AtomicInteger i = new AtomicInteger(0);
    public static void increment()
    {
        i.incrementAndGet();
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
                return false;
            }else{
                //System.out.println("thread finished");
               //increment();
                return true;
            }
        }
    }
}
