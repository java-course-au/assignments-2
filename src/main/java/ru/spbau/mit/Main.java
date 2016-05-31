package ru.spbau.mit;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            long timeBegin = System.currentTimeMillis();
            String sum = MD5Sum.countMD5SumInOneThread(new File(args[0]));
            System.out.println("MD5 Sum:" + sum +
                    "\ntime in one thread in mills: " + (System.currentTimeMillis() - timeBegin));

            timeBegin = System.currentTimeMillis();
            sum = MD5Sum.countMD5SumInExecutorService(new File(args[0]));
            System.out.println("MD5 Sum:" + sum +
                    "\ntime in one thread in mills: " + (System.currentTimeMillis() - timeBegin));

            timeBegin = System.currentTimeMillis();
            sum = MD5Sum.countMD5SumInExecutorService(new File(args[0]));
            System.out.println("MD5 Sum:" + sum +
                    "\ntime in one thread in mills: " + (System.currentTimeMillis() - timeBegin));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
