package com.lifeform.main.transactions.scripting.compiling;

import com.lifeform.main.data.files.StringFileHandler;
import engine.ByteCodeEngine;
import java.util.List;

public class StringFileCompiler {
    private StringFileHandler sfh;
    private ByteCodeEngine bce;

    public StringFileCompiler(StringFileHandler sfh, ByteCodeEngine bce) {
        this.bce = bce;
        this.sfh = sfh;
    }

    public byte[] compile() throws CompilerException {
        List<String> lines = sfh.getLines();
        return StringCompiler.compile(lines, bce);
    }
}
