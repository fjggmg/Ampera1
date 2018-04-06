package com.lifeform.main.transactions.scripting.word8v1ops;

import amp.ByteTools;
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
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITrans transaction, byte[] executionAddress) throws Exception {
        int add = stack.pop().getDataAsInt();
        int data = stack.pop().getDataAsInt();
        System.out.println("Storing: " + data + " to " + add);
        writableMemory.setElement(DataElement.create(ByteTools.deconstructInt(data)), add);
    }

    @Override
    public String getKeyword() {
        return Opcodes.SINT.getKeyword();
    }
}
