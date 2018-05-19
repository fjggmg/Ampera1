package com.ampex.main.transactions.scripting.compiling;

import engine.ASEConstants;
import engine.ByteCodeEngine;
import engine.operators.IOperator;

import java.util.ArrayList;
import java.util.List;

public class StringCompiler {

    /*
    public static byte[] compile(List<String> lines) throws CompilerException {
        byte[] code = new byte[lines.size()];
        int i = 0;
        for (String line : lines) {
            Opcodes op = Opcodes.byKeyword(line);
            if (op == null) throw new CompilerException("invalid opcode in file");
            code[i] = ByteTools.deconstructInt(op.getCode())[3];
            i++;
        }
        return code;
    }
    */

    public static byte[] compile(List<String> lines, ByteCodeEngine bce) throws CompilerException {
        List<String> tempByteCode = new ArrayList<>();
        tempByteCode.addAll(lines);

        IOperator[] operators = bce.getOperators();

        byte[] bytes = new byte[tempByteCode.size()];

        for (int i = 0; i < tempByteCode.size(); i++) {
            String opCode = tempByteCode.get(i);

            boolean success = false;

            for (int k = 0; k < ASEConstants.MAX_OPCODES; k++) {
                IOperator operator = operators[k];

                if (operator == null) {
                    continue;
                }

                if (opCode.equalsIgnoreCase(operator.getKeyword())) {
                    bytes[i] = (byte) operator.getBaseCode();
                    success = true;
                    break;
                }
            }

            if (!success) {
                throw new CompilerException("Unknown opcode: " + opCode);
            }
        }
        return bytes;
    }
}
