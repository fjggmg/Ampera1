package com.ampex.main.transactions.scripting.word8v1ops;

import amp.ByteTools;
import com.ampex.amperabase.ITransAPI;
import com.ampex.amperabase.KeyType;
import com.ampex.main.data.buckets.KeyKeyTypePair;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.data.utils.Utils;
import com.ampex.main.transactions.scripting.Opcodes;
import engine.binary.IBinary;
import engine.data.*;
import engine.operators.IOperator;
import engine.program.IOPCode;
import engine.program.IProgram;

import java.util.Arrays;
import java.util.Stack;

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
    public void execute(Stack<IDataElement> stack, IBinary binary, IProgram program, IConstantMemory constantMemory, IJumpMemory jumpMemory, IWritableMemory writableMemory, IOPCode opCode, ITransAPI transaction, byte[] executionAddress) throws Exception {
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
        stack.push(new DataElement(ByteTools.deconstructInt(result)));
    }

    @Override
    public String getKeyword() {
        return Opcodes.VSIGVALS.getKeyword();
    }
}
