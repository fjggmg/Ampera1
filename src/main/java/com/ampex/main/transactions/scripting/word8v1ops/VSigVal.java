package com.ampex.main.transactions.scripting.word8v1ops;

import com.ampex.amperabase.ITransAPI;
import com.ampex.amperabase.KeyType;
import com.ampex.main.data.EncryptionManager;
import com.ampex.main.data.Utils;
import com.ampex.main.transactions.scripting.Opcodes;
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
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITransAPI transaction, byte[] executionAddress) throws Exception {
        byte[] signed = stack.pop().getData();
        byte[] sig = stack.pop().getData();
        byte[] key = stack.pop().getData();
        KeyType keyType = KeyType.byValue(stack.pop().getDataAsByte());
        byte[] result = new byte[1];
        result[0] = (EncryptionManager.verifySig(signed, sig, Utils.toBase64(key), keyType)) ? (byte) 1 : (byte) 0;
        stack.push(DataElement.create(result));
    }

    @Override
    public String getKeyword() {
        return Opcodes.VSIGVAL.getKeyword();
    }
}
