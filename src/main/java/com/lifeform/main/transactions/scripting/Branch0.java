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

public class Branch0 implements IOperator {

    @Override
    public String getKeyword() {
        return Opcodes.BRN0.getKeyword();
    }

    @Override
    public int getMaxExecutions() {
        return -1;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.BRN0.getCode();
    }

    @Override
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITrans transaction, byte[] executionAddress) throws Exception {
        if (stack.pop().getDataAsInt() == 0) {
            if (program.getProgramCounter() < jumpMemory.getJumpPoint(0)) {
                program.setProgramCounter(jumpMemory.getJumpPoint(0));
            }
        }
    }
}
