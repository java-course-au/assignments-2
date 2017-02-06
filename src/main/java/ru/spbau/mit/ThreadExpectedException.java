package ru.spbau.mit;


import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ThreadExpectedException implements TestRule {
    private List<ThreadExpectedExceptionEntry> registerThreads = new ArrayList<>();
    private Class<? extends Throwable> expectedExcp = null;

    @Override
    public Statement apply(Statement statement, Description description) {
        return new ThreadExpectedExceptionStatement(statement);
    }

    public void expect(Class<? extends Throwable> e) {
        expectedExcp = e;
    }

    public void registerThread(Thread t) {
        registerThreads.add(new ThreadExpectedExceptionEntry(t));
    }

    public boolean isAllThrown() {
        return expectedExcp == null;
    }

    private class ThreadExpectedExceptionEntry {

        private Thread thread;
        private ThreadExpectedExceptionHandler handler = new ThreadExpectedExceptionHandler();

        ThreadExpectedExceptionEntry(Thread thread) {
            this.thread = thread;
            thread.setUncaughtExceptionHandler(handler);
        }

        public ThreadExpectedExceptionHandler getHandler() {
            return handler;
        }

        public Thread getThread() {
            return thread;
        }
    }

    private class ThreadExpectedExceptionHandler implements Thread.UncaughtExceptionHandler {
        private Throwable resultThrowable;

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            resultThrowable = e;
        }

        public boolean isThrowableExpected() {
            return isAllThrown() || (resultThrowable != null && resultThrowable.getClass().equals(expectedExcp));
        }
    }

    class ThreadExpectedExceptionStatement extends Statement {
        private Statement statement;

        ThreadExpectedExceptionStatement(Statement statement) {
            this.statement = statement;
        }

        private void checkThreads() {
            for (ThreadExpectedExceptionEntry entry : registerThreads) {
                assertFalse(entry.getThread().isAlive());
                assertTrue(entry.getHandler().isThrowableExpected());
            }
        }

        @Override
        public void evaluate() throws Throwable {
            statement.evaluate();
            checkThreads();
        }
    }
}

