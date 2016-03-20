package ru.spbau.mit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ThreadExpectedException implements TestRule {
    private Class<? extends Throwable> expectedException = null;
    private final List<Thread> threads = new ArrayList<>();
    private final List<Throwable> thrown = new ArrayList<>();

    public ThreadExpectedException() {
    }

    public void expect(Class<? extends Throwable> e) {
        expectedException = e;
    }

    public void registerThread(Thread t) {
        threads.add(t);
        thrown.add(null);
        final int id = thrown.size() - 1;
        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                thrown.set(id, e);
            }
        });
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    statement.evaluate();
                } catch (Exception e) {
                    // ignore
                }
                for (Thread t : threads) {
                    assertEquals(Thread.State.TERMINATED, t.getState());
                }
                if (expectedException != null) {
                    for (Throwable t : thrown) {
                        assertTrue(expectedException.isInstance(t));
                    }
                }
            }
        };
    }
}
