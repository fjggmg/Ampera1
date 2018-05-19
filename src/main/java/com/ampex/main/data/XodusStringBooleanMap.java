package com.ampex.main.data;

import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.bindings.BooleanBinding;
import jetbrains.exodus.bindings.StringBinding;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.StoreConfig;

public class XodusStringBooleanMap {
    public XodusStringBooleanMap(String fileName) {
        env = Environments.newInstance(fileName);
        store = env.computeInTransaction(txn -> env.openStore(fileName + "store", StoreConfig.WITHOUT_DUPLICATES, txn));
    }

    final Environment env;

    final Store store;

    public void put(String _key, Boolean _value) {
        final ByteIterable convertedKey = StringBinding.stringToEntry(_key);
        final ByteIterable convertedValue = BooleanBinding.booleanToEntry(_value);

        env.executeInTransaction(txn -> store.put(txn, convertedKey, convertedValue));
    }

    public boolean get(String _key) {
        final ByteIterable convertedKey = StringBinding.stringToEntry(_key);

        //@Override
        ByteIterable output = env.computeInReadonlyTransaction(txn -> store.get(txn, convertedKey));
        if (output == null) return false;
        return BooleanBinding.entryToBoolean(output);
    }

    public void close() {
        env.close();
    }

    public void clear() {
        env.clear();
    }

    public void remove(String _key) {
        final ByteIterable convertedKey = StringBinding.stringToEntry(_key);

        env.executeInTransaction(txn -> store.delete(txn, convertedKey));
    }

    public void gc() {
        env.gc();
    }

    public void suspendGC() {
        env.suspendGC();
    }

    public void resumeGC() {
        env.resumeGC();
    }
}
