package com.ampex.main.transactions.scripting.word8v1ops;

import amp.ByteTools;
import com.ampex.amperabase.ITransAPI;
import com.ampex.amperabase.KeyType;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.data.utils.Utils;
import com.ampex.main.transactions.scripting.Opcodes;
import engine.binary.Binary;
import engine.data.ConstantMemory;
import engine.data.DataElement;
import engine.data.JumpMemory;
import engine.data.WritableMemory;
import engine.operators.IOperator;
import engine.program.OPCode;
import engine.program.Program;

import java.util.Stack;

public class VSigBin implements IOperator {
    @Override
    public String getKeyword() {
        return Opcodes.VBINSIG.getKeyword();
    }

    @Override
    public int getMaxExecutions() {
        return -1;
    }

    @Override
    public int getBaseCode() {
        return Opcodes.VBINSIG.getCode();
    }

    @Override
    public void execute(Stack<DataElement> stack, Binary binary, Program program, ConstantMemory constantMemory, JumpMemory jumpMemory, WritableMemory writableMemory, OPCode opCode, ITransAPI transaction, byte[] executionAddress) throws Exception {
        byte[] sig = stack.pop().getData();
        KeyType keyType = binary.getPublicKeyType();
        byte[] key = binary.getPublicKey();
        byte[] signed = transaction.toSignBytes();
        //System.out.println("=================KEY TYPE IS: " + keyType + " =========================");
        stack.push(new DataElement(ByteTools.deconstructInt((EncryptionManager.verifySig(signed, sig, Utils.toBase64(key), keyType)) ? 1 : 0)));
    }
}
