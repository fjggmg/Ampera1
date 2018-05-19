package com.ampex.main.transactions.scripting.word8v1ops;

import amp.ByteTools;
import com.ampex.amperabase.IAddress;
import com.ampex.amperabase.ITransAPI;
import com.ampex.main.transactions.addresses.Address;
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

public class VerAddSame implements IOperator {
    @Override
    public String getKeyword() {
        return Opcodes.VADDSAME.getKeyword();
    }

    @Override
    public int getMaxExecutions() {
        return -1;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.VADDSAME.getCode();
    }

    @Override
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITransAPI transaction, byte[] executionAddress) throws Exception {
        IAddress a = Address.fromByteArray(stack.pop().getData());
        IAddress b = Address.fromByteArray(stack.pop().getData());
        stack.push(DataElement.create(ByteTools.deconstructInt((a.encodeForChain().equals(b.encodeForChain())) ? 1 : 0)));
    }
}
