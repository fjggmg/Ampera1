package com.ampex.main.transactions.scripting.word8v1ops;

import amp.Amplet;
import com.ampex.amperabase.IOutput;
import com.ampex.amperabase.ITransAPI;
import com.ampex.main.transactions.Output;
import com.ampex.main.transactions.scripting.Opcodes;
import engine.binary.IBinary;
import engine.data.*;
import engine.operators.IOperator;
import engine.program.IOPCode;
import engine.program.IProgram;

import java.util.Stack;

public class LOutAmountMemSck implements IOperator {
    @Override
    public String getKeyword() {
        return Opcodes.LOAMTMEMSCK.getKeyword();
    }

    @Override
    public int getMaxExecutions() {
        return -1;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.LOAMTMEMSCK.getCode();
    }

    @Override
    public void execute(Stack<IDataElement> stack, IBinary binary, IProgram program, IConstantMemory constantMemory, IJumpMemory jumpMemory, IWritableMemory writableMemory, IOPCode opCode, ITransAPI transaction, byte[] executionAddress) throws Exception {
        IOutput o = Output.fromAmp(Amplet.create(writableMemory.getElement(stack.pop().getDataAsInt()).getData()));
        stack.push(new DataElement(o.getAmount().toByteArray()));
    }
}
