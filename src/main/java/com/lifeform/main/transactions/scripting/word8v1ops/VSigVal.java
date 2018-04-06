package com.lifeform.main.transactions.scripting.word8v1ops;

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

public class VSigVal implements IOperator {
    @Override
    public int getMaxExecutions() {
        return 100;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.VSIGVAL.getCode();
    }

    @Override
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITrans transaction, byte[] executionAddress) throws Exception {
        byte[] signed = stack.pop().getData();
        byte[] sig = stack.pop().getData();
        byte[] key = stack.pop().getData();

        byte[] result = new byte[1];
        result[0] = (EncryptionManager.verifySig(signed, sig, Utils.toBase64(key))) ? (byte) 1 : (byte) 0;
        stack.push(DataElement.create(result));
    }

    @Override
    public String getKeyword() {
        return Opcodes.VSIGVAL.getKeyword();
    }
}
