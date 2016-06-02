package ru.spbau.mit;

import org.junit.Rule;
import org.junit.Test;

public class ThreadExpectedExceptionTest {
    @Rule
    public ThreadExpectedException threadExpectedException = new ThreadExpectedException();

    @Test
    public void testCorrectNoExceptions() throws InterruptedException {
        Thread thread = new Thread(() -> {
        });
        Thread thread1 = new Thread(() -> {
        });

        threadExpectedException.registerThread(thread);
        threadExpectedException.registerThread(thread1);
        thread.start();
        thread1.start();
        thread.join();
        thread1.join();
        threadExpectedException.expect(null);
    }

    @Test
    public void testCorrectExceptions() throws InterruptedException {
        Thread thread = new Thread(() -> {
            throw new RuntimeException();
        });
        Thread thread1 = new Thread(() -> {
            throw new RuntimeException();
        });

        threadExpectedException.registerThread(thread);
        threadExpectedException.registerThread(thread1);
        thread.start();
        thread1.start();
        thread.join();
        thread1.join();
        threadExpectedException.expect(RuntimeException.class);
    }

    @Test
    public void testNotTerminatedThread() throws InterruptedException {
        Thread thread = new Thread(() -> {
            throw new RuntimeException();
        });
        Thread thread1 = new Thread(() -> {
            throw new RuntimeException();
        });

        threadExpectedException.registerThread(thread);
        threadExpectedException.registerThread(thread1);
        thread.start();
        thread.join();
        thread1.join();
        threadExpectedException.expect(RuntimeException.class);
    }

    @Test
    public void testWrongException() throws InterruptedException {
        Thread thread = new Thread(() -> {
            throw new RuntimeException();
        });
        Thread thread1 = new Thread(() -> {
            throw new NullPointerException();
        });

        threadExpectedException.registerThread(thread);
        threadExpectedException.registerThread(thread1);
        thread.start();
        thread1.start();
        thread.join();
        thread1.join();
        threadExpectedException.expect(RuntimeException.class);
    }

}
