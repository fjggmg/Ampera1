package com.lifeform.main.transactions.scripting;

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

public class LPCConstStack implements IOperator {
    @Override
    public String getKeyword() {
        return Opcodes.LPCCONSTSCK.getKeyword();
    }

    @Override
    public int getMaxExecutions() {
        return -1;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.LPCCONSTSCK.getCode();
    }

    @Override
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITrans transaction, byte[] executionAddress) throws Exception {
        stack.push(constantMemory.getElement(program.getProgramCounter()));
    }
}
