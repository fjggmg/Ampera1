package com.lifeform.main.transactions.scripting.word8v1ops;

import amp.ByteTools;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.Utils;
import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.scripting.Opcodes;
import engine.binary.Binary;
import engine.data.ConstantMemory;
import engine.data.DataElement;
import engine.data.JumpMemory;
import engine.data.WritableMemory;
import engine.operators.IOperator;
import engine.program.OPCode;
import engine.program.Program;

import java.util.Stack;

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
        byte[] signed = stack.elementAt(0).getData();
        //byte[] rdata = stack.pop().getData();
        //System.out.println("Rdata: " + Arrays.toString(rdata));
        int result = stack.pop().getDataAsInt();
        System.out.println("Result Currently: " + result);
        byte[] sig = stack.pop().getData();
        byte[] key = stack.pop().getData();
        result = (result + ((EncryptionManager.verifySig(signed, sig, Utils.toBase64(key))) ? 1 : 0));
        stack.push(DataElement.create(ByteTools.deconstructInt(result)));
    }

    @Override
    public String getKeyword() {
        return Opcodes.VSIGVALS.getKeyword();
    }
}
