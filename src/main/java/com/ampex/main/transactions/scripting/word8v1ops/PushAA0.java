package com.ampex.main.transactions.scripting.word8v1ops;

import com.ampex.amperabase.ITransAPI;
import com.ampex.main.transactions.scripting.Opcodes;
import engine.binary.IBinary;
import engine.data.*;
import engine.operators.IOperator;
import engine.program.IOPCode;
import engine.program.IProgram;

import java.util.Stack;

public class PushAA0 implements IOperator {
    @Override
    public int getMaxExecutions() {
        return -1;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.PUSHAA0.getCode();
    }

    @Override
    public void execute(Stack<IDataElement> stack, IBinary binary, IProgram program, IConstantMemory constantMemory, IJumpMemory jumpMemory, IWritableMemory writableMemory, IOPCode opCode, ITransAPI transaction, byte[] executionAddress) throws Exception {
        stack.push(DataElement.create(new byte[1]));
    }

    @Override
    public String getKeyword() {
        return Opcodes.PUSHAA0.getKeyword();
    }
}
