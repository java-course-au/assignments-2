package ru.spbau.mit.utils;

import static org.junit.Assert.*;

public abstract class ExceptionCatchingThreadWrapper {
    private final Thread t;
    private Exception uncaughtException;

    // There is no point in passing SAM objects to constructor instead of overriding `run` method
    // because none of them have 'throws Exception` in signature of the run method,
    // therefore the user will still have to write boilerplate try-catch to re-throw it
    // he might as will simple remember it then
    public ExceptionCatchingThreadWrapper() {
        t = new Thread(new Runnable() {
            public void run() {
                try {
                    ExceptionCatchingThreadWrapper.this.run();
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
