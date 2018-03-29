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

public class LPCMemStack implements IOperator {
    @Override
    public int getMaxExecutions() {
        return -1;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.LPCMEMSTACK.getCode();
    }

    @Override
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITrans transaction, byte[] executionAddress) throws Exception {
        //System.out.println("PC: " + program.getProgramCounter());
        //System.out.println("Pushing: " + memory.getElement(program.getProgramCounter()));
        stack.push(writableMemory.getElement(program.getProgramCounter()));
    }

    @Override
    public String getKeyword() {
        return Opcodes.LPCMEMSTACK.getKeyword();
    }
}
