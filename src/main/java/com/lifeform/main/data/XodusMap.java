package com.lifeform.main.data;

/**
 * this is supposed to be a generics interface for the other maps but the fucking library doesn't use generics it uses strongly typed
 * objects so we can't do it that way without some pains, we'll probably switch to using this in the future but for now it's
 * simpler and faster to just do strongly typed objects ourselves
 */
public interface XodusMap {
    void put(Object k, Object v);

    Object get(Object k);

    void clear();
}
