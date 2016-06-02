package ru.spbau.mit;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        long beginTime = System.currentTimeMillis();
        System.out.println("SingleThread: " + CheckSumSingleThread.getSumSingleThread(args[0])
                + "; time: " + (System.currentTimeMillis() - beginTime));

        beginTime = System.currentTimeMillis();
        System.out.println("MultiThread: " + CheckSumExecutorService.getSumExecutorService(args[0])
                + "; time: " + (System.currentTimeMillis() - beginTime));

        beginTime = System.currentTimeMillis();
        System.out.println("ForkJoin: " + CheckSumForkJoin.getSumForkJoin(args[0])
                + "; time: " + (System.currentTimeMillis() - beginTime));
    }
}
