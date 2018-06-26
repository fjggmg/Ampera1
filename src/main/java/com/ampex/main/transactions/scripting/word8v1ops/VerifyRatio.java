package com.ampex.main.transactions.scripting.word8v1ops;

import amp.ByteTools;
import com.ampex.amperabase.ITransAPI;
import com.ampex.main.transactions.scripting.Opcodes;
import engine.binary.IBinary;
import engine.data.*;
import engine.operators.IOperator;
import engine.program.IOPCode;
import engine.program.IProgram;

import java.math.BigInteger;
import java.util.Stack;

public class VerifyRatio implements IOperator {
    @Override
    public String getKeyword() {
        return Opcodes.VRAT.getKeyword();
    }

    @Override
    public int getMaxExecutions() {
        return -1;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.VRAT.getCode();
    }

    @Override
    public void execute(Stack<IDataElement> stack, IBinary binary, IProgram program, IConstantMemory constantMemory, IJumpMemory jumpMemory, IWritableMemory writableMemory, IOPCode opCode, ITransAPI transaction, byte[] executionAddress) throws Exception {
        BigInteger numerator = new BigInteger(stack.pop().getData());
        BigInteger denominator = new BigInteger(stack.pop().getData());
        BigInteger ratTo = new BigInteger(stack.pop().getData());
        BigInteger ratFrom = new BigInteger(stack.pop().getData());
        //System.out.println("expected ratio:" + numerator.divide(BigInteger.valueOf(100_000_000)) + "/" + denominator.divide(BigInteger.valueOf(100_000_000)));
        //System.out.println("observed ratio: " + ratTo.multiply(numerator).divide(BigInteger.valueOf(100_000_000)) + "/" + (ratFrom.multiply(denominator).divide(BigInteger.valueOf(100_000_000))));

        if (ratTo.multiply(numerator).compareTo(ratFrom.multiply(denominator)) != 0) {
            stack.push(new DataElement(ByteTools.deconstructInt(0)));
        } else {
            stack.push(new DataElement(ByteTools.deconstructInt(1)));
        }

    }
}
