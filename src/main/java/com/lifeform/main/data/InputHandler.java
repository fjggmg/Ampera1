package com.lifeform.main.data;

import com.lifeform.main.IKi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class InputHandler extends Thread {

    private IKi ki;

    public InputHandler(IKi ki) {
        this.ki = ki;
    }

    @Override
    public void run() {
        BufferedReader s = new BufferedReader(new InputStreamReader(System.in));
        while (true) {


            try {
                switch (s.readLine()) {
                    case "exit":
                        System.out.println("Exiting program");
                        ki.close();
                        System.exit(0);
                        break;
                    case "mineGPU":
                        System.out.println("starting gpu miner");
                        ki.getMinerMan().startMiners();
                        break;

                    default:
                        System.out.println("unrecognized input");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Input processed");
        }
    }
}
