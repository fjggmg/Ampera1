package com.lifeform.main.transactions.scripting.word8v1ops;

import amp.ByteTools;
import com.lifeform.main.transactions.*;
import com.lifeform.main.transactions.scripting.Opcodes;
import engine.binary.Binary;
import engine.data.ConstantMemory;
import engine.data.DataElement;
import engine.data.JumpMemory;
import engine.data.WritableMemory;
import engine.operators.IOperator;
import engine.program.OPCode;
import engine.program.Program;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class VerifyNetLoss implements IOperator {
    @Override
    public String getKeyword() {
        return Opcodes.VERNETLOSS.getKeyword();
    }

    @Override
    public int getMaxExecutions() {
        return -1;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.VERNETLOSS.getCode();
    }

    @Override
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITrans transaction, byte[] executionAddress) throws Exception {
        BigInteger expected = new BigInteger(stack.pop().getData());
        IAddress address = Address.fromByteArray(executionAddress);
        //System.out.println("Expected: " + expected);
        BigInteger actual = BigInteger.ZERO;
        List<Token> tokens = new ArrayList<>();
        BigInteger allIn = BigInteger.ZERO;
        for (Input i : transaction.getInputs()) {
            if (i.getAddress().encodeForChain().equals(address.encodeForChain())) {
                if (!tokens.contains(i.getToken())) {
                    tokens.add(i.getToken());
                    if (tokens.size() > 1) {
                        stack.push(new DataElement(ByteTools.deconstructInt(0)));
                        return;
                    }
                }
                allIn = allIn.add(i.getAmount());
            }
        }
        Token t = tokens.get(0);
        for (Output o : transaction.getOutputs()) {
            if (o.getToken().equals(t)) {
                if (o.getAddress().encodeForChain().equals(address.encodeForChain())) {
                    actual = actual.add(o.getAmount());
                }
            }

        }
        //System.out.println("Actual: " + actual);
        if (allIn.subtract(actual).compareTo(expected) != 0) {
            stack.push(new DataElement(ByteTools.deconstructInt(0)));
        } else {
            stack.push(new DataElement(ByteTools.deconstructInt(1)));
        }
    }
}
