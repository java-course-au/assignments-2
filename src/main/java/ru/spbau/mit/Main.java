package ru.spbau.mit;

import java.io.File;

public final class Main {
    private Main(){}

    public static void main(String[] args) {
        long timeBegin = System.currentTimeMillis();
        String sum = MD5Sum.countMD5SumInOneThread(new File(args[0]));
        System.out.println("MD5 Sum:" + sum
                + "\ntime in one thread in mills: " + (System.currentTimeMillis() - timeBegin));

        timeBegin = System.currentTimeMillis();
        sum = MD5Sum.countMD5SumInExecutorService(new File(args[0]));
        System.out.println("MD5 Sum:" + sum
                + "\ntime in one thread in mills: " + (System.currentTimeMillis() - timeBegin));

        timeBegin = System.currentTimeMillis();
        sum = MD5Sum.countMD5SumInForkJoin(new File(args[0]));
        System.out.println("MD5 Sum:" + sum
                + "\ntime in one thread in mills: " + (System.currentTimeMillis() - timeBegin));

    }
}
