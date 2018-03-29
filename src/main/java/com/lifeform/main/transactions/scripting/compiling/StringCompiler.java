package com.lifeform.main.transactions.scripting.compiling;

import amp.ByteTools;
import com.lifeform.main.transactions.scripting.Opcodes;

import java.util.List;

public class StringCompiler {

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
}
