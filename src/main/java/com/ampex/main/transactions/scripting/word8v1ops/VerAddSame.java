package com.ampex.main.transactions.scripting.word8v1ops;

import amp.ByteTools;
import com.ampex.amperabase.IAddress;
import com.ampex.amperabase.ITransAPI;
import com.ampex.main.transactions.addresses.Address;
import com.ampex.main.transactions.scripting.Opcodes;
import engine.binary.IBinary;
import engine.data.*;
import engine.operators.IOperator;
import engine.program.IOPCode;
import engine.program.IProgram;

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
    public void execute(Stack<IDataElement> stack, IBinary binary, IProgram program, IConstantMemory constantMemory, IJumpMemory jumpMemory, IWritableMemory writableMemory, IOPCode opCode, ITransAPI transaction, byte[] executionAddress) throws Exception {
        IAddress a = Address.fromByteArray(stack.pop().getData());
        IAddress b = Address.fromByteArray(stack.pop().getData());
        stack.push(new DataElement(ByteTools.deconstructInt((a.encodeForChain().equals(b.encodeForChain())) ? 1 : 0)));
    }
}
