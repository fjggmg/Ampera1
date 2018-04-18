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

public class Branch2 implements IOperator {
    @Override
    public String getKeyword() {
        return Opcodes.BRANCH2.getKeyword();
    }

    @Override
    public int getMaxExecutions() {
        return -1;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.BRANCH2.getCode();
    }

    @Override
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITrans transaction, byte[] executionAddress) throws Exception {
        if (stack.pop().getDataAsInt() == 0) {
            if (program.getProgramCounter() < jumpMemory.getJumpPoint(2)) {
                program.setProgramCounter(jumpMemory.getJumpPoint(2));
            }
        }
    }
}