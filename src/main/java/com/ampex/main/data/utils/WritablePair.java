package com.ampex.main.data.utils;

public class WritablePair<K,V> {

    private K key;
    private V value;
    public WritablePair(K key,V value)
    {
        this.key = key;
        this.value = value;
    }

    public K getKey()
    {
        return key;
    }

    public V getValue()
    {
        return value;
    }


    public void setKey(K newKey)
    {
        key = newKey;
    }

    public void setValue(V newValue)
    {
        value = newValue;
    }
}
