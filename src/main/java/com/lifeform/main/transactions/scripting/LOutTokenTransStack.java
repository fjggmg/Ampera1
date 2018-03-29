package com.lifeform.main.transactions.scripting;

import amp.Amplet;
import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.Output;
import engine.binary.Binary;
import engine.data.ConstantMemory;
import engine.data.DataElement;
import engine.data.JumpMemory;
import engine.data.WritableMemory;
import engine.operators.IOperator;
import engine.program.OPCode;
import engine.program.Program;

import java.util.Stack;

public class LOutTokenTransStack implements IOperator {
    @Override
    public String getKeyword() {
        return Opcodes.LOTTS.getKeyword();
    }

    @Override
    public int getMaxExecutions() {
        return -1;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.LOTTS.getCode();
    }

    @Override
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITrans transaction, byte[] executionAddress) throws Exception {
        Output o = transaction.getOutputs().get(stack.pop().getDataAsInt());
        stack.push(DataElement.create(o.getToken().getName().getBytes("UTF-8")));
    }
}
