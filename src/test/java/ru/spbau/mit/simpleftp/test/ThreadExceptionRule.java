package ru.spbau.mit.simpleftp.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ThreadExceptionRule implements TestRule {
    public class ThreadExceptionRuleNone extends Throwable{}

    private List<ThreadExpectedExceptionEntry> registerThreads = new ArrayList<>();
    private Class<? extends Throwable> expectedExcp = null;

    @Override
    public Statement apply(Statement statement, Description description) {
        return new ThreadExpectedExceptionStatement(statement);
    }

    public void expect(Class<? extends Throwable> e) {
        expectedExcp = e;
    }

    public void excpectNone() {
        expectedExcp = ThreadExceptionRuleNone.class;
    }

    public void registerThreadAndStart(Thread t) {
        registerThreads.add(new ThreadExpectedExceptionEntry(t));
        t.start();
    }

    public boolean isAllThrown() {
        return expectedExcp == null;
    }

    public boolean isNoThrown() {
        return expectedExcp != null && expectedExcp.equals(ThreadExceptionRuleNone.class);
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
            return isAllThrown()
                   || (isNoThrown() && resultThrowable == null)
                   || (resultThrowable != null && resultThrowable.getClass().equals(expectedExcp));
        }
    }

    class ThreadExpectedExceptionStatement extends Statement {
        private Statement statement;

        ThreadExpectedExceptionStatement(Statement statement) {
            this.statement = statement;
        }

        private void awaitThreads() {
            for (ThreadExpectedExceptionEntry entry : registerThreads) {
                try {
                    entry.getThread().join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void checkThreads() {
            for (ThreadExpectedExceptionEntry entry : registerThreads) {
                assertTrue(entry.getHandler().isThrowableExpected());
            }
        }

        @Override
        public void evaluate() throws Throwable {
            statement.evaluate();
            awaitThreads();
            checkThreads();
        }
    }
}
