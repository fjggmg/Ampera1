package com.ampex.main.transactions.scripting.compiling;

import com.ampex.main.data.files.StringFileHandler;
import engine.ByteCodeEngine;
import engine.IByteCodeEngine;

import java.util.List;

public class StringFileCompiler {
    private StringFileHandler sfh;
    private IByteCodeEngine bce;

    public StringFileCompiler(StringFileHandler sfh, IByteCodeEngine bce) {
        this.bce = bce;
        this.sfh = sfh;
    }

    public byte[] compile() throws CompilerException {
        List<String> lines = sfh.getLines();
        return StringCompiler.compile(lines, bce);
    }
}
