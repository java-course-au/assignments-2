package ru.spbau.mit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(Enclosed.class)
public final class TestThreadExpectedException {
    private TestThreadExpectedException() {
    }

    private static final int THREAD_COUNT = 1;
    private static final int ONE_SECOND = 1000;

    public static class TestThreadExpectedExceptionNoFail {
        @Rule
        public final ThreadExpectedException expectedException = new ThreadExpectedException();


        @Test
        public void testWithoutExceptions() throws InterruptedException {
            List<Thread> threads = getThreads(() -> {
            });
            threads.forEach(Thread::start);
            for (Thread t : threads) {
                t.join();
            }
        }

        @Test
        public void testThreadNotFinished() {
            List<Thread> threads = getThreads(() -> {
                try {
                    Thread.sleep(ONE_SECOND);
                } catch (InterruptedException ignored) {
                }
            });
            threads.forEach(expectedException::registerThread);
            threads.forEach(Thread::start);
            expectedException.expectUnfinishedThread();
        }

        @Test
        public void testNumberFormatException() throws InterruptedException {
            List<Thread> threads = getThreads(() -> Integer.parseInt("not int"));
            expectedException.expect(NumberFormatException.class);
            threads.forEach(expectedException::registerThread);
            threads.forEach(Thread::start);
            for (Thread t : threads) {
                t.join();
            }
        }
    }

    public static class TestThreadExpectedExceptionFail {
        private final ExpectedException expected = ExpectedException.none();

        private final ThreadExpectedException expectedException = new ThreadExpectedException();

        @Rule
        public final TestRule chain = RuleChain.outerRule(expected).around(expectedException);

        @Test
        public void testUnexpectedAliveThread() {
            List<Thread> threads = getThreads(() -> {
                try {
                    Thread.sleep(ONE_SECOND);
                } catch (InterruptedException ignored) {
                }
            });
            threads.add(new Thread(() -> {
            }));
            threads.forEach(expectedException::registerThread);
            threads.forEach(Thread::start);
            expected.expect(RuntimeException.class);
        }

        @Test
        public void testUnexpectedException() throws InterruptedException {
            List<Thread> threads = getThreads(() -> Integer.parseInt("not int"));
            threads.forEach(expectedException::registerThread);
            expectedException.expect(IllegalArgumentException.class);
            expected.expect(RuntimeException.class);
            threads.forEach(Thread::start);
            for (Thread t : threads) {
                t.join();
            }
        }

        @Test
        public void testNoException() throws InterruptedException {
            List<Thread> threads = getThreads(() -> {
            });
            threads.forEach(expectedException::registerThread);
            expectedException.expect(IllegalArgumentException.class);
            expected.expect(RuntimeException.class);
            threads.forEach(Thread::start);
            for (Thread t : threads) {
                t.join();
            }
        }
    }

    private static List<Thread> getThreads(Runnable r) {
        List<Thread> l = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            l.add(new Thread(r));
        }
        return l;
    }

}
