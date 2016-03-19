package ru.spbau.mit;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

public class TestThreadExpectedException {
    @Rule
    public ThreadExpectedException threadRegister = new ThreadExpectedException();

    @Test
    public void simpleOkTest() {
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                }
            }));
            threadRegister.registerThread(threads.get(i));
            threads.get(i).start();
        }

        for (int i = 0; i < 10; ++i) {
            try {
                threads.get(i).join();
            } catch (InterruptedException ignore) {
            }
        }

        threadRegister.expect(null);
    }


    @Test
    public void simpleWATest() {
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    throw new RuntimeException();
                }
            }));
            threadRegister.registerThread(threads.get(i));
            threads.get(i).start();
        }

        for (int i = 0; i < 10; ++i) {
            try {
                threads.get(i).join();
            } catch (InterruptedException ignore) {
            }
        }

        threadRegister.expect(null);
    }

    @Test
    public void twiceExpectOKTest() {
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    throw new RuntimeException();
                }
            }));
            threadRegister.registerThread(threads.get(i));
            threads.get(i).start();
        }

        for (int i = 0; i < 10; ++i) {
            try {
                threads.get(i).join();
            } catch (InterruptedException ignore) {
            }
        }

        threadRegister.expect(ArrayIndexOutOfBoundsException.class);
        threadRegister.expect(RuntimeException.class);
    }

    @Test
    public void unfinishedWATest() {
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 1000; ++i) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                }
            }));
            threadRegister.registerThread(threads.get(i));
            threads.get(i).start();
        }

        threadRegister.expect(null);
    }

    @Test
    public void otherExceptionWATest() {
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    throw new RuntimeException();
                }
            }));
            threadRegister.registerThread(threads.get(i));
            threads.get(i).start();
        }

        for (int i = 0; i < 10; ++i) {
            try {
                threads.get(i).join();
            } catch (InterruptedException ignore) {
            }
        }

        threadRegister.expect(ArrayIndexOutOfBoundsException.class);
    }
}
