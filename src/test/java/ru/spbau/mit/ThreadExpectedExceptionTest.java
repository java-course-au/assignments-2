package ru.spbau.mit;

import org.junit.Rule;
import org.junit.Test;

public final class ThreadExpectedExceptionTest {
    private static final String MESSAGE = "Designed to fail";
    private static final int TIME_TO_SLEEP = 1000;

    private static final Runnable EMPTY_RUNNABLE = () -> {
    };
    private static final Runnable THROW_RUNNABLE = () -> {
        throw new RuntimeException(MESSAGE);
    };
    private static final Runnable SLEEP_RUNNABLE = () -> {
        try {
            Thread.sleep(TIME_TO_SLEEP);
        } catch (InterruptedException ignored) {
        }
    };

    @Rule
    public final ThreadExpectedException threadExpectedException = ThreadExpectedException.none();

    @Test
    public void testOK() {
        Thread thread1 = new Thread(EMPTY_RUNNABLE);
        Thread thread2 = new Thread(EMPTY_RUNNABLE);
        threadExpectedException.registerThread(thread1);
        threadExpectedException.registerThread(thread2);
        thread1.start();
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException ignored) {
        }
    }

    @Test
    public void testNotStopped() {
        Thread thread1 = new Thread(EMPTY_RUNNABLE);
        Thread thread2 = new Thread(SLEEP_RUNNABLE);
        threadExpectedException.registerThread(thread1);
        threadExpectedException.registerThread(thread2);
        thread1.start();
        thread2.start();
    }

    @Test
    public void testThrew() {
        Thread thread2 = new Thread(EMPTY_RUNNABLE);
        Thread thread1 = new Thread(THROW_RUNNABLE);
        threadExpectedException.registerThread(thread1);
        threadExpectedException.registerThread(thread2);
        thread1.start();
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException ignored) {
        }
    }

    @Test
    public void testMissing() {
        Thread thread1 = new Thread(THROW_RUNNABLE);
        Thread thread2 = new Thread(EMPTY_RUNNABLE);
        threadExpectedException.registerThread(thread1);
        threadExpectedException.registerThread(thread2);
        threadExpectedException.expect(RuntimeException.class);
        thread1.start();
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException ignored) {
        }
    }

    @Test
    public void testHit() {
        Thread thread = new Thread(THROW_RUNNABLE);
        threadExpectedException.registerThread(thread);
        threadExpectedException.expectMessage(MESSAGE);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
    }
}
