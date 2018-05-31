package com.ampex.main.transactions.scripting.word8v1ops;

import com.ampex.amperabase.ITransAPI;
import com.ampex.main.transactions.scripting.Opcodes;
import engine.binary.IBinary;
import engine.data.IConstantMemory;
import engine.data.IDataElement;
import engine.data.IJumpMemory;
import engine.data.IWritableMemory;
import engine.operators.IOperator;
import engine.program.IOPCode;
import engine.program.IProgram;

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
    public void execute(Stack<IDataElement> stack, IBinary binary, IProgram program, IConstantMemory constantMemory, IJumpMemory jumpMemory, IWritableMemory writableMemory, IOPCode opCode, ITransAPI transaction, byte[] executionAddress) throws Exception {
        if (stack.pop().getDataAsInt() == 0) {
            if (program.getProgramCounter() < jumpMemory.getJumpPoint(2)) {
                program.setProgramCounter(jumpMemory.getJumpPoint(2));
            }
        }
    }
}
