package com.lifeform.main.transactions.scripting.compiling;

import amp.ByteTools;
import com.lifeform.main.data.files.StringFileHandler;
import com.lifeform.main.transactions.scripting.Opcodes;

import java.util.List;

public class StringFileCompiler {
    private StringFileHandler sfh;

    public StringFileCompiler(StringFileHandler sfh) {
        this.sfh = sfh;
    }

    public byte[] compile() throws CompilerException {
        List<String> lines = sfh.getLines();
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
