package ru.spbau.mit;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TestThreadExpectedException {
    private static final int THREAD_NUMBER = 10;

    @Rule
    public ThreadExpectedExceptionHandler threadExpectedExceptionHandler = new ThreadExpectedExceptionHandler();

    @Test
    public void testNotAliveAndNoExpectedException() throws InterruptedException {
        List<Thread> threadList = createThreadList(() -> {
        });

        startThreads(threadList, threadList.size());
        joinThreads(threadList, threadList.size());
        assertTrue(true);
    }

    @Test
    public void testAliveAndNoExpectedException() throws InterruptedException {
        List<Thread> threadList = createThreadList(() -> {
        });

        threadExpectedExceptionHandler.setExpectedRuntimeException(true);

        Thread infinityThread = new Thread(() -> {
            while (true) {
            }
        });
        threadList.add(infinityThread);
        threadExpectedExceptionHandler.registerThread(infinityThread);

        startThreads(threadList, threadList.size());
        joinThreads(threadList, threadList.size() - 1);
    }

    @Test
    public void testNotAliveAndExpectNumberFormatException() throws InterruptedException {
        List<Thread> threadList = createThreadList(() -> {
            throw new NumberFormatException();
        });

        threadExpectedExceptionHandler.expect(NumberFormatException.class);

        startThreads(threadList, threadList.size());
        joinThreads(threadList, threadList.size());
        assertTrue(true);
    }

    @Test
    public void testNotAliveAndExpectRuntimeException() throws InterruptedException {
        List<Thread> threadList = createThreadList(() -> {
            throw new NumberFormatException();
        });

        threadExpectedExceptionHandler.setExpectedRuntimeException(true);
        threadExpectedExceptionHandler.expect(RuntimeException.class);

        startThreads(threadList, threadList.size());
        joinThreads(threadList, threadList.size());
    }

    private List<Thread> createThreadList(Runnable runnable) {
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < THREAD_NUMBER; i++) {
            Thread thread = new Thread(runnable);
            threadList.add(thread);
            threadExpectedExceptionHandler.registerThread(thread);
        }
        return threadList;
    }

    private void startThreads(List<Thread> threadList, int size) {
        for (int i = 0; i < size; i++) {
            threadList.get(i).start();
        }
    }

    private void joinThreads(List<Thread> threadList, int size) throws InterruptedException {
        for (int i = 0; i < size; i++) {
            threadList.get(i).join();
        }
    }
}
