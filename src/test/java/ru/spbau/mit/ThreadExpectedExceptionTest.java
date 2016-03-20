package ru.spbau.mit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static ru.spbau.mit.TestsTester.*;

@RunWith(TestsTester.class)
public class ThreadExpectedExceptionTest {
    private static class ExceptionBase extends RuntimeException {}
    private static class ExceptionDerived extends ExceptionBase {}
    private static class ExceptionDoubleDerived extends ExceptionDerived {}
    private static class ExceptionIndependent extends RuntimeException {}

    public abstract static class AbstractTestClass {
        @Rule
        public final ThreadExpectedException e = new ThreadExpectedException();
    }


    @ShouldSucceed
    public static class TestSuppressesException extends AbstractTestClass {
        @Test
        public void test() throws Exception {
            throw new ExceptionDoubleDerived();
        }
    }

    @ShouldFail
    public static class TestThreadNotStarted extends AbstractTestClass {
        @Test
        public void test() throws Exception {
            Thread t1 = new Thread(() -> { });
            Thread t2 = new Thread(() -> { });
            e.registerThread(t1);
        }
    }

    @ShouldFail
    public static class TestThreadNotTerminated extends AbstractTestClass {
        @Test
        public void test() throws Exception {
            Thread t1 = new Thread(() -> {
                try {
                    // CHECKSTYLE.OFF: MagicNumber
                    Thread.sleep(100);
                    // CHECKSTYLE.ON: MagicNumber
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            e.registerThread(t1);
            t1.start();
        }
    }

    @ShouldSucceed
    public static class TestThreadNoExceptions extends AbstractTestClass {
        @Test
        public void test() throws Exception {
            Thread t1 = new Thread(() -> { });
            Thread t2 = new Thread(() -> { });
            e.registerThread(t1);
            e.registerThread(t2);
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        }
    }

    @ShouldSucceed
    public static class TestThreadExceptionThrownNotChecked extends AbstractTestClass {
        @Test
        public void test() throws Exception {
            Thread t1 = new Thread(() -> { throw new ExceptionIndependent(); });
            Thread t2 = new Thread(() -> { });
            e.expect(ExceptionDerived.class);
            e.expect(null);
            e.registerThread(t1);
            e.registerThread(t2);
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        }
    }

    @ShouldSucceed
    public static class TestThreadDerivedExceptionThrown extends AbstractTestClass {
        @Test
        public void test() throws Exception {
            Thread t1 = new Thread(() -> { throw new ExceptionDoubleDerived(); });
            Thread t2 = new Thread(() -> { throw new ExceptionDerived(); });
            e.expect(ExceptionDerived.class);
            e.registerThread(t1);
            e.registerThread(t2);
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        }
    }

    @ShouldFail
    public static class TestThreadBaseExceptionThrown extends AbstractTestClass {
        @Test
        public void test() throws Exception {
            Thread t1 = new Thread(() -> { throw new ExceptionBase(); });
            Thread t2 = new Thread(() -> { throw new ExceptionDerived(); });
            e.expect(ExceptionDerived.class);
            e.registerThread(t1);
            e.registerThread(t2);
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        }
    }

    @ShouldFail
    public static class TestThreadExceptionThrownInThreadOne extends AbstractTestClass {
        @Test
        public void test() throws Exception {
            Thread t1 = new Thread(() -> { throw new ExceptionDoubleDerived(); });
            Thread t2 = new Thread(() -> { });
            e.expect(ExceptionDerived.class);
            e.registerThread(t1);
            e.registerThread(t2);
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        }
    }

    @ShouldFail
    public static class TestThreadExceptionThrownInThreadTwo extends AbstractTestClass {
        @Test
        public void test() throws Exception {
            Thread t1 = new Thread(() -> { });
            Thread t2 = new Thread(() -> { throw new ExceptionDoubleDerived(); });
            e.expect(ExceptionDerived.class);
            e.registerThread(t1);
            e.registerThread(t2);
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        }
    }
}
