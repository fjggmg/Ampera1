package com.lifeform.main.data;

import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.bindings.StringBinding;
import jetbrains.exodus.env.*;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Queue on 9/24/2017.
 */
public class XodusStringMap {
    public XodusStringMap(String fileName) {
        env = Environments.newInstance(fileName);
        store = env.computeInTransaction(txn -> env.openStore(fileName + "store", StoreConfig.WITHOUT_DUPLICATES_WITH_PREFIXING, txn));

    }

    final Environment env;
    final Store store;


    public void put(String _key, String _value) {
        final ByteIterable convertedKey = StringBinding.stringToEntry(_key);
        final ByteIterable convertedValue = StringBinding.stringToEntry(_value);

        env.executeInTransaction(txn -> store.put(txn, convertedKey, convertedValue));
    }

    public String get(String _key) {
        final ByteIterable convertedKey = StringBinding.stringToEntry(_key);

        //@Override
        ByteIterable output = env.computeInReadonlyTransaction(txn -> store.get(txn, convertedKey));
        if (output == null) return null;
        return StringBinding.entryToString(output);
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
