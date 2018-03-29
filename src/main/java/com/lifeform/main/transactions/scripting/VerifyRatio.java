package com.lifeform.main.transactions.scripting;

import amp.ByteTools;
import com.lifeform.main.transactions.ITrans;
import engine.binary.Binary;
import engine.data.ConstantMemory;
import engine.data.DataElement;
import engine.data.JumpMemory;
import engine.data.WritableMemory;
import engine.operators.IOperator;
import engine.program.OPCode;
import engine.program.Program;

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
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITrans transaction, byte[] executionAddress) throws Exception {
        BigInteger numerator = new BigInteger(stack.pop().getData());
        BigInteger denominator = new BigInteger(stack.pop().getData());
        BigInteger ratTo = new BigInteger(stack.pop().getData());
        BigInteger ratFrom = new BigInteger(stack.pop().getData());
        System.out.println("expected ratio:" + numerator.divide(BigInteger.valueOf(100_000_000)) + "/" + denominator.divide(BigInteger.valueOf(100_000_000)));
        System.out.println("observed ratio: " + ratTo.multiply(numerator).divide(BigInteger.valueOf(100_000_000)) + "/" + (ratFrom.multiply(denominator).divide(BigInteger.valueOf(100_000_000))));

        if (ratTo.multiply(numerator).compareTo(ratFrom.multiply(denominator)) != 0) {
            stack.push(DataElement.create(ByteTools.deconstructInt(0)));
        } else {
            stack.push(DataElement.create(ByteTools.deconstructInt(1)));
        }

    }
}
