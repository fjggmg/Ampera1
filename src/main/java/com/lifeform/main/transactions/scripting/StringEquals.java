package com.lifeform.main.transactions.scripting;

import amp.ByteTools;
import com.lifeform.main.transactions.ITrans;
import engine.binary.Binary;
import engine.data.ConstantMemory;
import engine.data.DataElement;
import engine.data.JumpMemory;
import engine.data.WritableMemory;
import engine.operators.IOperator;
import engine.program.OPCode;
import engine.program.Program;

import java.util.Stack;

public class StringEquals implements IOperator {
    @Override
    public String getKeyword() {
        return Opcodes.SEQ.getKeyword();
    }

    @Override
    public int getMaxExecutions() {
        return -1;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.SEQ.getCode();
    }

    @Override
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITrans transaction, byte[] executionAddress) throws Exception {
        String a = stack.pop().getDataAsString();
        String b = stack.pop().getDataAsString();
        stack.push(DataElement.create(ByteTools.deconstructInt((a.equals(b)) ? 1 : 0)));
    }
}
