package ru.spbau.mit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.HashMap;
import java.util.Map;

public class ThreadExpectedException implements TestRule {
    private HashMap<Thread, Throwable> threadExceptions = new HashMap<>();
    private Class<? extends Throwable> lastExpectedException = null;

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            private static final String TEST_FAILED = "Test failed due to exception: ";

            @Override
            public void evaluate() throws Throwable {
                statement.evaluate();
                checkThreadsForStopping();
                if (lastExpectedException != null) {
                    checkThreadsForExpectedException(lastExpectedException);
                }
            }

            private void checkThreadsForStopping() {
                for (Thread thread : threadExceptions.keySet()) {
                    if (thread.isAlive()) {
                        throw new RuntimeException(TEST_FAILED + "Thread " + thread.getName() + " is alive.");
                    }
                }
            }

            private void checkThreadsForExpectedException(Class<? extends Throwable> expectedException) {
                for (Map.Entry<Thread, Throwable> entry : threadExceptions.entrySet()) {
                    if (!entry.getValue().getClass().equals(expectedException)) {
                        throw new RuntimeException(TEST_FAILED + "Thread " + entry.getKey().getName()
                                + " had terminated with not expected exception.");
                    }
                }
            }
        };
    }

    public void expect(Class<? extends Throwable> e) {
        lastExpectedException = e;
    }

    public void registerThread(Thread t) {
        threadExceptions.put(t, null);
        t.setUncaughtExceptionHandler(threadExceptions::put);
    }
}
