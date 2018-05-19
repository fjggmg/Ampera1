package com.ampex.main.transactions.scripting.word8v1ops;

import com.ampex.amperabase.ITransAPI;
import com.ampex.main.transactions.scripting.Opcodes;
import engine.binary.Binary;
import engine.data.ConstantMemory;
import engine.data.DataElement;
import engine.data.JumpMemory;
import engine.data.WritableMemory;
import engine.operators.IOperator;
import engine.program.OPCode;
import engine.program.Program;

import java.util.Stack;

public class LHalfP1PCCSCK implements IOperator {
    @Override
    public String getKeyword() {
        return Opcodes.LHP1PCCSCK.getKeyword();
    }

    @Override
    public int getMaxExecutions() {
        return -1;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.LHP1PCCSCK.getCode();
    }

    @Override
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITransAPI transaction, byte[] executionAddress) throws Exception {
        stack.push(constantMemory.getElement((program.getProgramCounter() + 1) / 2));
    }
}
