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

public class Branch implements IOperator {
    @Override
    public int getMaxExecutions() {
        return -1;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.BRANCH.getCode();
    }

    @Override
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITrans transaction, byte[] executionAddress) throws Exception {
        int pc = stack.pop().getDataAsInt();
        byte data = stack.pop().getData()[0];
        if (data == 0 && pc > program.getProgramCounter()) {
            program.setProgramCounter(pc);
        }
    }

    @Override
    public String getKeyword() {
        return Opcodes.BRANCH.getKeyword();
    }
}
