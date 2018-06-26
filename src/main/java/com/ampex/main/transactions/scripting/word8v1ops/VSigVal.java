package com.ampex.main.transactions.scripting.word8v1ops;

import com.ampex.amperabase.ITransAPI;
import com.ampex.amperabase.KeyType;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.data.utils.Utils;
import com.ampex.main.transactions.scripting.Opcodes;
import engine.binary.IBinary;
import engine.data.*;
import engine.operators.IOperator;
import engine.program.IOPCode;
import engine.program.IProgram;

import java.util.Stack;

public class VSigVal implements IOperator {
    @Override
    public int getMaxExecutions() {
        return 100;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.VSIGVAL.getCode();
    }

    @Override
    public void execute(Stack<IDataElement> stack, IBinary binary, IProgram program, IConstantMemory constantMemory, IJumpMemory jumpMemory, IWritableMemory writableMemory, IOPCode opCode, ITransAPI transaction, byte[] executionAddress) throws Exception {
        byte[] signed = stack.pop().getData();
        byte[] sig = stack.pop().getData();
        byte[] key = stack.pop().getData();
        KeyType keyType = KeyType.byValue(stack.pop().getDataAsByte());
        byte[] result = new byte[1];
        result[0] = (EncryptionManager.verifySig(signed, sig, Utils.toBase64(key), keyType)) ? (byte) 1 : (byte) 0;
        stack.push(new DataElement(result));
    }

    @Override
    public String getKeyword() {
        return Opcodes.VSIGVAL.getKeyword();
    }
}
