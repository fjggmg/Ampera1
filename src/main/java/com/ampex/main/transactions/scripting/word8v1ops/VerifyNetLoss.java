package com.ampex.main.transactions.scripting.word8v1ops;

import amp.ByteTools;
import com.ampex.amperabase.*;
import com.ampex.main.transactions.addresses.Address;
import com.ampex.main.transactions.scripting.Opcodes;
import engine.binary.IBinary;
import engine.data.*;
import engine.operators.IOperator;
import engine.program.IOPCode;
import engine.program.IProgram;

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
    public void execute(Stack<IDataElement> stack, IBinary binary, IProgram program, IConstantMemory constantMemory, IJumpMemory jumpMemory, IWritableMemory writableMemory, IOPCode opCode, ITransAPI transaction, byte[] executionAddress) throws Exception {
        BigInteger expected = new BigInteger(stack.pop().getData());
        IAddress address = Address.fromByteArray(executionAddress);
        //System.out.println("Expected: " + expected);
        BigInteger actual = BigInteger.ZERO;
        List<Token> tokens = new ArrayList<>();
        BigInteger allIn = BigInteger.ZERO;
        for (IInput i : transaction.getInputs()) {
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
        for (IOutput o : transaction.getOutputs()) {
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
