package com.lifeform.main.transactions.scripting;

import com.lifeform.main.IKi;
import com.lifeform.main.data.files.StringFileHandler;
import com.lifeform.main.transactions.scripting.compiling.CompilerException;
import com.lifeform.main.transactions.scripting.compiling.StringCompiler;
import com.lifeform.main.transactions.scripting.compiling.StringFileCompiler;
import engine.ByteCodeEngine;
import engine.program.Program;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScriptManager {

    private IKi ki;
    public static final int GEN_TRADE_FAIL_JUMP = 43;
    public static final int GEN_TRADE_CANCEL_JUMP = 46;
    public static final int GEN_TRADE_CANCEL_FAIL_JUMP = 65;
    public static final byte VERSION = 1;

    public ScriptManager(ByteCodeEngine bce8, ByteCodeEngine bce16, IKi ki) {
        this.ki = ki;
        bce8.addOperator(LPCMemStack.class);
        bce8.addOperator(VSigVal.class);
        bce8.addOperator(VSigValS.class);
        bce8.addOperator(PushAA0.class);
        bce8.addOperator(PopPopPush.class);
        bce8.addOperator(PushI0.class);
        bce8.addOperator(ClearStack.class);
        bce8.addOperator(LConstStack.class);
        bce8.addOperator(GreaterThan.class);
        bce8.addOperator(Branch.class);
        bce8.addOperator(StoreInt.class);
        bce8.addOperator(LMemStack.class);
        bce8.addOperator(PushI1.class);
        bce8.addOperator(Dup.class);
        bce8.addOperator(LHalfP1PCCSCK.class);
        bce8.addOperator(LHalfPCMemStack.class);
        bce8.addOperator(PushI255.class);
        bce8.addOperator(PushI254.class);
        bce8.addOperator(LOutAddMemStack.class);
        bce8.addOperator(LPCConstStack.class);
        bce8.addOperator(VerAddSame.class);
        bce8.addOperator(Branch0.class);
        bce8.addOperator(LOutAmountMemSck.class);
        bce8.addOperator(VerifyRatio.class);
        bce8.addOperator(StringEquals.class);
        bce8.addOperator(LOutTokenMemStack.class);
        bce8.addOperator(PushI2.class);
        bce8.addOperator(PushOutSizeStack.class);
        bce8.addOperator(PushOutStack.class);
        bce8.addOperator(IntEquals.class);
        bce8.addOperator(PushI3.class);
        bce8.addOperator(LOutAddTransStack.class);
        bce8.addOperator(LOutAmountTransSck.class);
        bce8.addOperator(LOutTokenTransStack.class);
        bce8.addOperator(PushI4.class);
        bce8.addOperator(VerifyNetLoss.class);
        bce8.addOperator(PushI5.class);
        bce8.addOperator(Branch1.class);
        bce8.addOperator(Branch2.class);
        bce8.addOperator(IntNotEquals.class);
        //bce8.addOperator(VSigBin.class);
        bce8.finalizeOperators();
        try {
            byte[] gTrade = StringCompiler.compile(Arrays.asList("POSS", "PI2", "INEQ", "BRN1", "POSS", "PI5", "IEQ", "BRN0", "PI0", "LOATS", "PI3", "LCSk", "VAS", "BRN0",
                    "PI1", "LOATTS", "VNL", "BRN0",
                    "PI0", "LOATTS", "PI1", "LOATTS",
                    "PI0", "LCSK", "PI4", "LCSK", "VRAT", "BRN0", "PI1", "LCSK", "PI1", "LOTTS", "SEQ", "BRN0",
                    "PI2", "LCSK", "PI0", "LOTTS", "SEQ", "BRN0", "CSK", "PI0", "TERM", "CSK", "PI1", "TERM",
                    "PI0", "LOATS", "PI3", "LCSK", "VAS", "BRN2", "PI1", "LOATS", "PI3", "LCSK", "VAS", "BRN2",
                    "PI0", "LMSK", "VBS", "BRN2", "CSK", "PI0", "TERM", "CSK", "PI1", "TERM"));
            genTrade = new Program(gTrade);
        } catch (Exception e) {
            e.printStackTrace();
        }
        genTrade.seal();
    }

    private Program genTrade;
    private List<Program> loadedScripts = new ArrayList<>();

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
            StringFileCompiler sfc = new StringFileCompiler(new StringFileHandler(ki, folder.getName() + File.pathSeparator + f.getName()));
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
