package ru.spbau.mit;

import java.io.File;

public final class Main {
    private Main(){}

    public static void main(String[] args) {
        System.out.println("File: " + args[0]);
        long timeBegin = System.currentTimeMillis();
        String sum = MD5Sum.countMD5SumInOneThread(new File(args[0]));
        System.out.println("In One thread MD5 Sum:" + sum
                + "\ntime in one thread in mills: " + (System.currentTimeMillis() - timeBegin));

        timeBegin = System.currentTimeMillis();
        sum = MD5Sum.countMD5SumInExecutorService(new File(args[0]));
        System.out.println("Executor Service MD5 Sum:" + sum
                + "\ntime in one thread in mills: " + (System.currentTimeMillis() - timeBegin));

        timeBegin = System.currentTimeMillis();
        sum = MD5Sum.countMD5SumInForkJoin(new File(args[0]));
        System.out.println("Fork Join MD5 Sum:" + sum
                + "\ntime in one thread in mills: " + (System.currentTimeMillis() - timeBegin));

    }
}
