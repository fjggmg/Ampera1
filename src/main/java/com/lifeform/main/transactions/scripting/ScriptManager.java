package com.lifeform.main.transactions.scripting;

import amp.ByteTools;
import com.lifeform.main.IKi;
import com.lifeform.main.data.KeyKeyTypePair;
import com.lifeform.main.data.Utils;
import com.lifeform.main.data.files.StringFileHandler;
import com.lifeform.main.transactions.KeyType;
import com.lifeform.main.transactions.scripting.compiling.CompilerException;
import com.lifeform.main.transactions.scripting.compiling.StringCompiler;
import com.lifeform.main.transactions.scripting.compiling.StringFileCompiler;
import com.lifeform.main.transactions.scripting.word8v1ops.*;
import engine.ByteCodeEngine;
import engine.binary.Binary;
import engine.data.ConstantMemory;
import engine.data.DataElement;
import engine.data.JumpMemory;
import engine.program.Program;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ScriptManager {

    private IKi ki;
    public static final int GEN_TRADE_FAIL_JUMP = 43;
    public static final int GEN_TRADE_CANCEL_JUMP = 46;
    public static final int GEN_TRADE_CANCEL_FAIL_JUMP = 67;
    public static final byte VERSION = 1;

    public ScriptManager(ByteCodeEngine bce8, ByteCodeEngine bce16, IKi ki) {
        this.ki = ki;
        /*
        bce8.addOperator(VSigVal.class);
        bce8.addOperator(VSigValS.class);
        bce8.addOperator(PushAA0.class);
        bce8.addOperator(StoreInt.class);
        bce8.addOperator(LHalfP1PCCSCK.class);
        bce8.addOperator(LHalfPCMemStack.class);
        bce8.addOperator(LOutAddMemStack.class);
        bce8.addOperator(VerAddSame.class);
        bce8.addOperator(Branch0.class);
        bce8.addOperator(LOutAmountMemSck.class);
        bce8.addOperator(VerifyRatio.class);
        bce8.addOperator(LOutTokenMemStack.class);
        bce8.addOperator(PushOutSizeStack.class);
        bce8.addOperator(PushOutStack.class);
        bce8.addOperator(LOutAddTransStack.class);
        bce8.addOperator(LOutAmountTransSck.class);
        bce8.addOperator(LOutTokenTransStack.class);
        bce8.addOperator(VerifyNetLoss.class);
        bce8.addOperator(Branch1.class);
        bce8.addOperator(Branch2.class);
        */
        bce8.addReservedWord8Operators();
        bce8.addOperators("com.lifeform.main.transactions.scripting.word8v1ops");
        //bce8.addOperator(VSigBin.class);
        bce8.finalizeOperators();
        try {
            byte[] gTrade = StringCompiler.compile(Arrays.asList("POSS", "PI2", "INEQ", "BRN1", "POSS", "PI5", "IEQ", "BRN0", "PI0", "LOATS", "PI3", "LCSk", "VAS", "BRN0",
                    "PI1", "LOATTS", "VNL", "BRN0",
                    "PI0", "LOATTS", "PI1", "LOATTS",
                    "PI0", "LCSK", "PI4", "LCSK", "VRAT", "BRN0", "PI1", "LCSK", "PI1", "LOTTS", "SEQ", "BRN0",
                    "PI2", "LCSK", "PI0", "LOTTS", "SEQ", "BRN0", "CSK", "PI0", "TERM", "CSK", "PI1", "TERM",
                    "PI0", "LOATS", "PI3", "LCSK", "VAS", "BRN2", "PI1", "LOATS", "PI3", "LCSK", "VAS", "BRN2",
                    "PI0", "LMSK", "PI1", "LMSK", "VBS", "BRN2", "CSK", "PI0", "TERM", "CSK", "PI1", "TERM"), bce8);
            genTrade = new Program(gTrade);
        } catch (Exception e) {
            ki.getMainLog().error("Exception compiling general trade script", e);
        }
        genTrade.seal();


    }

    //load sig onto stack, then load key onto stack
    private final List<String> mSigLoad = Arrays.asList("PI#", "LCSK", "PI#", "LMSK");
    //push initial result to stack
    private final List<String> mSigMid = Arrays.asList("PI0");
    //verify sig, check against required sigs, branch to success if so
    private final List<String> mSigVerify = Arrays.asList("VSVS", "DUP", "PIR", "LCSK", "GTN", "BRN0");
    //fail or success
    private final List<String> mSigEnd = Arrays.asList("CSK", "PI1", "TERM", "CSK", "PI0", "TERM");
    private Program genTrade;
    private List<Program> loadedScripts = new ArrayList<>();

    /**
     * builds a binary for multisig wallets. Handles up to 30 key multi-sig
     *
     * @param keys         keys involved
     * @param sigsRequired sigs required
     * @param bce8         an 8 bit BCE
     * @param entropy      entropy (probably user entered through UI
     * @param publicKey    public key for main key (no extra permissions with this multisig)
     * @return a binary ready to use for a multi-sig wallet
     */
    public Binary buildMultiSig(Map<String, Byte> keys, int sigsRequired, ByteCodeEngine bce8, byte[] entropy, byte[] publicKey) {
        if (sigsRequired > keys.size()) return null;
        if (keys.size() > 30) return null;
        List<String> fullCode = new ArrayList<>();

        DataElement[] cm = new DataElement[32];
        int i = 0;
        for (String key : keys.keySet()) {
            List<String> mSigLoad = new ArrayList<>();

            for (String code : this.mSigLoad) {
                mSigLoad.add(code.replace("#", "" + i));
            }
            fullCode.addAll(mSigLoad);
            try {
                if (KeyType.byValue(keys.get(key)) == null || KeyType.byValue(keys.get(key)) == KeyType.NONE)
                    return null;
                cm[i] = new DataElement(new KeyKeyTypePair(Utils.fromBase64(key), KeyType.byValue(keys.get(key))).serializeToBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }
        try {
            cm[i] = new DataElement(ByteTools.deconstructInt(sigsRequired));
        } catch (Exception e) {
            e.printStackTrace();
        }
        fullCode.addAll(mSigMid);
        List<String> mSigVerify = new ArrayList<>();
        for (String code : this.mSigVerify) {
            mSigVerify.add(code.replaceAll("PIR", "PI" + i));
        }

        for (int k = 0; k < i; k++) {
            fullCode.addAll(mSigVerify);
        }
        short branch0 = (short) (fullCode.size() + 3);
        fullCode.addAll(mSigEnd);
        Program mSig;
        try {
            mSig = new Program(StringCompiler.compile(fullCode, bce8));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        short[] jumps = new short[16];
        jumps[0] = branch0;

        try {
            JumpMemory jm = new JumpMemory(jumps);
            return new Binary(mSig, new ConstantMemory(cm), jm, true, VERSION, entropy, System.currentTimeMillis(), publicKey, null, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * will load scripts that are in bytecode format (i.e. strings of opcodes 1 per line in BCE8, will have support
     * for BCE16 eventually)
     */
    public void loadScripts() {
        File folder = new File("/scripts");
        if (!folder.exists()) {

            if (!folder.mkdirs()) {
                ki.debug("Unable to create scripts folder");
            }

        }

        if (folder.listFiles() == null || folder.listFiles().length == 0) {
            ki.debug("No scripts to load");
            return;
        }

        for (File f : folder.listFiles()) {
            StringFileCompiler sfc = new StringFileCompiler(new StringFileHandler(ki, folder.getName() + File.pathSeparator + f.getName()), ki.getBCE8());
            Program p = null;
            try {
                p = new Program(sfc.compile());
            } catch (Exception e) {
                ki.getMainLog().error("Error loading script file: " + f.getName(), e);
            }
            loadedScripts.add(p);
        }


    }

    public Program genericTrade() {
        return genTrade;
    }
}
