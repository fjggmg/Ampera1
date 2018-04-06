package com.lifeform.main.transactions.scripting.word8v1ops;

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
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITrans transaction, byte[] executionAddress) throws Exception {
        stack.push(DataElement.create(new byte[1]));
    }

    @Override
    public String getKeyword() {
        return Opcodes.PUSHAA0.getKeyword();
    }
}
