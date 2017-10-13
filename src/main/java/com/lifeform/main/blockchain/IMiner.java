package com.lifeform.main.blockchain;

public interface IMiner {


    void start();

    void setName(String name);

    void interrupt();

    void setup(int index);

}
