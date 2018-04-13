import amp.Amplet;
import amp.ByteTools;
import amp.HeadlessPrefixedAmplet;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.Utils;
import com.lifeform.main.transactions.*;
import com.lifeform.main.transactions.scripting.*;
import com.lifeform.main.transactions.scripting.compiling.CompilerException;
import com.lifeform.main.transactions.scripting.compiling.StringCompiler;
import engine.ByteCodeEngine;
import engine.binary.Binary;
import engine.data.ConstantMemory;
import engine.data.DataElement;
import engine.data.JumpMemory;
import engine.data.WritableMemory;
import engine.exceptions.ASEException;
import engine.program.Program;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class ScriptTest {

    /*
    public void testScript() {
        EncryptionManager.initStatic();
        EncryptionManager em = new EncryptionManager(null);
        em.generateKeys();

        ByteCodeEngine bce = new ByteCodeEngine(1);

        bce.finalizeOperators();

        byte[] pbytes; = {51,51,51,51,51,51,51,51,51,59,
                         53,59,61,59,50,59,58,57,62,58,
                         56,59,50,53,59,61,59,50,59,58,
                         57,62,58,56,59,50,53,59,61,59,
                         50,59,58,57,62,58,56,59,50,53,
                         59,61,59,50,59,58,57,62,58,56,
                         59,50,55, 1, 60,59, 1};
        /*
        List<String> pStrings = Arrays.asList("LPCMSK", "LHP1PCCS", "LHMPCS", "LHP1PCCS", "LHMPCS", "LHP1PCCS", "LHMPCS", "LHP1PCCS", "LHMPCS", "PI0",
                "VSVS", "DUP", "PI0", "LCSK", "GTN", "PI255", "LCSK",
                "BRN", "VSVS", "DUP", "PI0", "LCSK", "GTN", "PI255", "LCSK",
                "BRN", "VSVS", "DUP", "PI0", "LCSK", "GTN", "PI255", "LCSK",
                "BRN", "VSVS", "DUP", "PI0", "LCSK", "GTN", "PI255", "LCSK",
                "BRN", "CSK", "PI1", "TERM", "CSK", "PI0", "TERM");
        try {
            pbytes = StringCompiler.compile(pStrings, bce);
        } catch (CompilerException e) {
            e.printStackTrace();
            return;
        }
        Program program = null;
        try {
            program = new Program(pbytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        WritableMemory memory = new WritableMemory();
        DataElement[] constantsData = new DataElement[256];
        constantsData[0] = DataElement.create(ByteTools.deconstructInt(2));
        constantsData[255] = DataElement.create(ByteTools.deconstructInt(45));
        ConstantMemory constants = null;
        Binary binary;

        byte[] someData = {};
        try {
            someData = "Random Data".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        */
        /*
        byte[] key = Utils.fromBase64(em.getPublicKeyString());
        byte[] sig = em.sign(someData);
        try {
            memory.setElement(someData, 0);
            //constants.setElement(key,1);
            constantsData[1] = DataElement.create(key);
            memory.setElement(sig, 1);
            em.generateKeys();
            //constants.setElement(Utils.fromBase64(em.getPublicKeyString()),2);
            constantsData[2] = DataElement.create(Utils.fromBase64(em.getPublicKeyString()));
            memory.setElement(em.sign(someData), 2);
            em.generateKeys();
            //constants.setElement(Utils.fromBase64(em.getPublicKeyString()),3);
            constantsData[3] = DataElement.create(Utils.fromBase64(em.getPublicKeyString()));
            memory.setElement(sig, 3);
            em.generateKeys();
            //constants.setElement(Utils.fromBase64(em.getPublicKeyString()),4);
            constantsData[4] = DataElement.create(Utils.fromBase64(em.getPublicKeyString()));
            memory.setElement(em.sign(someData), 4);

            memory.setElement(DataElement.create(ByteTools.deconstructInt(60)),18);
            memory.setElement(DataElement.create(ByteTools.deconstructInt(60)),30);
            memory.setElement(DataElement.create(ByteTools.deconstructInt(60)),42);
            memory.setElement(DataElement.create(ByteTools.deconstructInt(60)),54);


        } catch (ASEException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<DataElement> result = null;
        try {
            constants = new ConstantMemory(constantsData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(constants);
        JumpMemory jMem = new JumpMemory();
        try {
            byte[] entropy = "Some entropy".getBytes("UTF-8");
            //binary = new Binary(program, constants, jMem, true, 1, entropy, System.currentTimeMillis(), Utils.fromBase64(em.getPublicKeyString()));
            //result = bce.executeProgram(binary, memory, null, null, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(result);
        System.out.println("Result: " + result.get(0).getDataAsInt());
        assertTrue(result.get(0).getDataAsInt() == 0);

    }

    /*
    public void testScriptTrade() {
        EncryptionManager.initStatic();
        EncryptionManager em = new EncryptionManager(null);
        em.generateEDkeys();

        ByteCodeEngine bce = new ByteCodeEngine(1);

        bce.finalizeOperators();


        List<String> programShit = Arrays.asList("POSS", "PI5", "IEQ", "BRN0", "PI0", "LOATS", "PI3", "LCSk", "VAS", "BRN0",
                "PI1", "LOATTS", "VNL", "BRN0",
                "PI0", "LOATTS", "PI1", "LOATTS",
                "PI0", "LCSK", "VRAT", "BRN0", "PI1", "LCSK", "PI1", "LOTTS", "SEQ", "BRN0",
                "PI2", "LCSK", "PI0", "LOTTS", "SEQ", "BRN0", "CSK", "PI0", "TERM", "CSK", "PI1", "TERM");
        IAddress add;
        byte[] pBytes;
        try {
            pBytes = StringCompiler.compile(programShit, bce);
        } catch (CompilerException e) {
            e.printStackTrace();
            return;
        }
        Program program = null;
        try {
            program = new Program(pBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String addEnt = "Some entropy";
        String otherAddEnt = "some other entropy";
        program.seal();
        try {
            add = NewAdd.createNew(em.getPublicKeyString(), addEnt, AddressLength.SHA512, true);
        } catch (InvalidAddressException e) {
            e.printStackTrace();
            return;
        }
        IAddress otherAdd;
        try {
            otherAdd = NewAdd.createNew(em.getPublicKeyString(), otherAddEnt, AddressLength.SHA256, false);
        } catch (InvalidAddressException e) {
            e.printStackTrace();
            return;
        }
        Output usdOut = new Output(BigInteger.valueOf(1_000_000_000L), add, Token.USD, 0, System.currentTimeMillis(), (byte) 2);
        Output originOut = new Output(BigInteger.valueOf(5_000_000_000L), otherAdd, Token.ORIGIN, 1, System.currentTimeMillis(), (byte) 2);
        //Output originOut2 = new Output(BigInteger.valueOf(5_000_000_000L), add, Token.ORIGIN, 2, System.currentTimeMillis(), (byte) 2);
        //Output usdOut2 = new Output(BigInteger.valueOf(1_000_000_000L), otherAdd, Token.USD, 3, System.currentTimeMillis(), (byte) 2);
        List<Output> outputs = new ArrayList<>();
        outputs.add(usdOut);
        outputs.add(originOut);
        //outputs.add(originOut2);
        //outputs.add(usdOut2);
        ITrans trans;
        Map<String, KeySigEntropyPair> keySigMap = new HashMap<>();
        WritableMemory memory = new WritableMemory();
        WritableMemory memory2 = WritableMemory.deserializeFromBytes(memory.serializeToBytes());
        assertNotNull(memory2);

        long start = System.currentTimeMillis();
        List<Integer> doneList = new CopyOnWriteArrayList<>();


        ConstantMemory constant = null;

        short[] jumps = new short[16];
        jumps[0] = 37;
        JumpMemory jMem = null;
        try {
            jMem = new JumpMemory(jumps);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        DataElement[] constantValues = new DataElement[32];
        constantValues[0] = DataElement.create(BigInteger.valueOf(500_000_000L).toByteArray());//ratio of Origin -> USD * 100_000_000
        constantValues[1] = DataElement.create(Token.ORIGIN.getName().getBytes());//token on offer
        constantValues[2] = DataElement.create(Token.USD.getName().getBytes());//token accepting
        constantValues[3] = DataElement.create(add.toByteArray());//address USD must be paid to

        try {
            constant = new ConstantMemory(constantValues);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Binary binary;
        System.out.println("pk size: " + Utils.fromBase64(em.getPublicKeyString()).length);
        Input usdIn = new Input(EncryptionManager.sha3256("other random shit"), 0, BigInteger.valueOf(2_000_000_000L), otherAdd, Token.USD, System.currentTimeMillis());
        Input originInFee = new Input(EncryptionManager.sha3256("random shit3"), 1, BigInteger.valueOf(5_000_000_000L), otherAdd, Token.ORIGIN, System.currentTimeMillis());
        List<Input> inputs = new ArrayList<>();

        inputs.add(usdIn);
        inputs.add(originInFee);
        List<String> associatedOrigin = new ArrayList<>();

        KeySigEntropyPair ksep = new KeySigEntropyPair(Utils.toBase64(memory.serializeToBytes()), addEnt, associatedOrigin, null, true);
        List<String> associatedUSD = new ArrayList<>();
        associatedUSD.add(usdIn.getID());
        associatedUSD.add(originInFee.getID());
        KeySigEntropyPair ksepUSD = new KeySigEntropyPair(null, otherAddEnt, associatedUSD, null, false);
        try {
            byte[] entropy = "Some entropy".getBytes("UTF-8");
            binary = new Binary(program, constant, jMem, true, 1, entropy, System.currentTimeMillis(), Utils.fromBase64(em.getPublicKeyString()), null, 0);

            keySigMap.put(em.getPublicKeyString(), ksepUSD);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        IAddress contractAdd;
        try {
            contractAdd = NewAdd.createNew(Utils.toBase64(binary.serializeToAmplet().serializeToBytes()), addEnt, AddressLength.SHA512, true);
        } catch (InvalidAddressException e) {
            e.printStackTrace();
            return;
        }
        Input originIn = new Input(EncryptionManager.sha3256("random shit"), 1, BigInteger.valueOf(10_000_000_000L), contractAdd, Token.ORIGIN, System.currentTimeMillis());
        inputs.add(originIn);
        associatedOrigin.add(originIn.getID());
        keySigMap.put(Utils.toBase64(binary.serializeToAmplet().serializeToBytes()), ksep);

        String bin = Utils.toBase64(binary.serializeToAmplet().serializeToBytes());
        assertNotNull(bin);
        //System.out.println(bin);
        assertNotNull(Amplet.create(Utils.fromBase64(bin)));
        try {
            binary = Binary.deserializeFromAmplet(Amplet.create(Utils.fromBase64(bin)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(Binary.deserializeFromAmplet(Amplet.create(Utils.fromBase64(bin))));
        DummyBCEKi ki = new DummyBCEKi();
        Output originOut2 = new Output(BigInteger.valueOf(5_000_000_000L), contractAdd, Token.ORIGIN, 2, System.currentTimeMillis(), (byte) 2);
        Output usdOut2 = new Output(BigInteger.valueOf(1_000_000_000L), otherAdd, Token.USD, 3, System.currentTimeMillis(), (byte) 2);
        Output originOut3 = new Output(BigInteger.valueOf(4_000_000_000L), otherAdd, Token.ORIGIN, 4, System.currentTimeMillis(), (byte) 2);
        outputs.add(originOut2);
        outputs.add(usdOut2);
        outputs.add(originOut3);
        try {
            trans = new NewTrans("", outputs, inputs, keySigMap, TransactionType.NEW_TRANS);
            //trans.makeChange(BigInteger.TEN,otherAdd);
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
            return;
        }
        //trans.addSig(em.getPublicKeyString(),Utils.toBase64(em.sign(trans.toSignBytes())));
        String sig = Utils.toBase64(em.sign(trans.toSignBytes()));
        ki.bce = bce;
        for (int i = 0; i < 1; i++) {
            int i2 = i;
            new Thread() {
                public void run() {
                    ITrans trans;
                    try {
                        trans = new NewTrans("", outputs, inputs, keySigMap, TransactionType.NEW_TRANS);
                        trans.addSig(em.getPublicKeyString(), sig);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    try {
                        //assertTrue(trans.verifySigs());
                        System.out.println("Fee is: " + trans.getFee());
                        assertTrue(trans.verifyInputToOutput());
                        assertTrue(trans.verifyCanSpend());
                        assertTrue(trans.verifySpecial(ki));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    doneList.add(i2);
                }
            }.start();
        }

        while (doneList.size() < 1) {
        }
        long stop = System.currentTimeMillis();
        System.out.println("Build/execution took " + (stop - start) + " milliseconds");

    }
    */
}
