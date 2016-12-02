package ru.spbau.mit.utils;

import static org.junit.Assert.*;

public abstract class TestThread {
    private final Thread t;
    private Exception uncaughtException;

    public TestThread() {
        t = new Thread(new Runnable() {
            public void run() {
                try {
                    TestThread.this.run();
                } catch (Exception e) {
                    e.printStackTrace();
                    uncaughtException = e;
                }
            }
        });
    }

    public void start() {
        t.start();
    }

    public abstract void run() throws Exception;

    public void join() throws InterruptedException {
        t.join();
        assertNull(uncaughtException);
    }
}
