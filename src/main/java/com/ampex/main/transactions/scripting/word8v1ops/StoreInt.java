package com.ampex.main.transactions.scripting.word8v1ops;

import amp.ByteTools;
import com.ampex.amperabase.ITransAPI;
import com.ampex.main.transactions.scripting.Opcodes;
import engine.binary.IBinary;
import engine.data.*;
import engine.operators.IOperator;
import engine.program.IOPCode;
import engine.program.IProgram;

import java.util.Stack;

public class StoreInt implements IOperator {
    @Override
    public int getMaxExecutions() {
        return -1;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.SINT.getCode();
    }

    @Override
    public void execute(Stack<IDataElement> stack, IBinary binary, IProgram program, IConstantMemory constantMemory, IJumpMemory jumpMemory, IWritableMemory writableMemory, IOPCode opCode, ITransAPI transaction, byte[] executionAddress) throws Exception {
        int add = stack.pop().getDataAsInt();
        int data = stack.pop().getDataAsInt();
        System.out.println("Storing: " + data + " to " + add);
        writableMemory.setElement(new DataElement(ByteTools.deconstructInt(data)), add);
    }

    @Override
    public String getKeyword() {
        return Opcodes.SINT.getKeyword();
    }
}
