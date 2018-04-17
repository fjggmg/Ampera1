package com.lifeform.main.transactions.scripting.word8v1ops;

import amp.ByteTools;
import amp.HeadlessPrefixedAmplet;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.KeyKeyTypePair;
import com.lifeform.main.data.Utils;
import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.KeyType;
import com.lifeform.main.transactions.scripting.Opcodes;
import engine.binary.Binary;
import engine.data.ConstantMemory;
import engine.data.DataElement;
import engine.data.JumpMemory;
import engine.data.WritableMemory;
import engine.operators.IOperator;
import engine.program.OPCode;
import engine.program.Program;

import java.util.Arrays;
import java.util.Stack;
//TODO possibly rename, a rework was done to more directly access sign data from transaction

/**
 * only for transactions since has been reworked, may make another that is generic
 */
public class VSigValS implements IOperator {
    @Override
    public int getMaxExecutions() {
        return 100;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.VSIGVALS.getCode();
    }

    @Override
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITrans transaction, byte[] executionAddress) throws Exception {
        byte[] signed = transaction.toSignBytes();//stack.elementAt(0).getData();
        //byte[] rdata = stack.pop().getData();
        //System.out.println("Rdata: " + Arrays.toString(rdata));
        int result = stack.pop().getDataAsInt();
        System.out.println("Result Currently: " + result);
        boolean prefail = false;
        byte[] sig = null;
        try {
            sig = stack.pop().getData();
        } catch (Exception e) {
            //fail quietly
            prefail = true;
        }
        System.out.println("Sig: " + Arrays.toString(sig));
        byte[] kktpBytes = null;
        try {
            kktpBytes = stack.pop().getData();
        } catch (Exception e) {
            prefail = true;
            //fail quietly
        }
        if (prefail) {
            stack.push(new DataElement(ByteTools.deconstructInt(result)));
            return;
        }
        System.out.println("KKTP: " + Arrays.toString(kktpBytes));
        KeyKeyTypePair kttp = KeyKeyTypePair.fromBytes(kktpBytes);
        byte[] key = kttp.getKey();
        KeyType keyType = kttp.getKeyType();
        result = (result + ((EncryptionManager.verifySig(signed, sig, Utils.toBase64(key), keyType)) ? 1 : 0));
        stack.push(DataElement.create(ByteTools.deconstructInt(result)));
    }

    @Override
    public String getKeyword() {
        return Opcodes.VSIGVALS.getKeyword();
    }
}
