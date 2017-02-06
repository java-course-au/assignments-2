package ru.spbau.mit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

public class TestThreadExpectedExceptionSmall {
    private class WrappedThreadExpectedExceptionRule extends ThreadExpectedException {
        private ThreadExpectedException threadExpectedExceptionRule;
        private boolean isThrown = false;

        private class WrrapedStatement extends Statement {
            private Statement statement;

            WrrapedStatement(Statement statement) {
                this.statement = statement;
            }

            @Override
            public void evaluate() throws Throwable {
                try {
                    statement.evaluate();
                } catch (AssertionError e) {
                    if (!isThrown) {
                        throw e;
                    }
                    return;
                }
                if(isThrown){
                    throw new RuntimeException();
                }
            }
        }

        @Override
        public Statement apply(Statement statement, Description description) {
            statement = super.apply(statement, description);
            return new WrrapedStatement(statement);
        }

        public void setThrown(boolean thrown) {
            isThrown = thrown;
        }
    }

    @Rule
    public WrappedThreadExpectedExceptionRule threadExpectedExceptionRule = new
            WrappedThreadExpectedExceptionRule();
    public static final int SLEEPING_TIMEOUT = 1000;

    public static final Runnable INFINITE_LOOP_RUNNABLE = () -> {
        while (true) {
            try {
                Thread.sleep(SLEEPING_TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public static final Runnable EMPTY_RUNNABLE = () -> {
    };
    public static final Runnable THROWN_RUNTIME_EXCEPTION_RUNNABLE = () -> {
        throw new RuntimeException();
    };
    public static final Runnable THROWN_NEW_ILLEGAL_ARGUMENT_RUNNABLE = () -> {
        throw new
                IllegalArgumentException();
    };

    public static final int THREADS_NUM = 10;

    public void doCreate(List<Thread> list, Runnable runnable) {
        for (int i = 0; i < THREADS_NUM; i++) {
            Thread newThread = new Thread(runnable);
            list.add(newThread);
            threadExpectedExceptionRule.registerThread(newThread);
            newThread.start();

        }
    }

    public void doJoin(List<Thread> list) throws InterruptedException {
        for (Thread thread : list) {
            thread.join();
        }
    }

    @Test
    public void testEmpty() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        doCreate(threads, EMPTY_RUNNABLE);
        doJoin(threads);
        threadExpectedExceptionRule.expect(null);
        threadExpectedExceptionRule.setThrown(false);

    }

    @Test
    public void testExpected() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        doCreate(threads, THROWN_RUNTIME_EXCEPTION_RUNNABLE);
        doJoin(threads);
        threadExpectedExceptionRule.expect(RuntimeException.class);
        threadExpectedExceptionRule.setThrown(false);
    }

    @Test
    public void testNotExpected() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        doCreate(threads, THROWN_NEW_ILLEGAL_ARGUMENT_RUNNABLE);
        doJoin(threads);
        threadExpectedExceptionRule.expect(RuntimeException.class);
        threadExpectedExceptionRule.setThrown(true);
    }

    @Test
    public void testNotFinished() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        doCreate(threads, INFINITE_LOOP_RUNNABLE);
        threadExpectedExceptionRule.expect(RuntimeException.class);
        threadExpectedExceptionRule.setThrown(true);
    }
}
