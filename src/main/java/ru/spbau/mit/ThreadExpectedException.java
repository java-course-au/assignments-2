package ru.spbau.mit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Сева on 22.03.2016.
 */
public class ThreadExpectedException implements TestRule {
    private Class expectedException = null;
    private final Map<Thread, Throwable> exceptions = new HashMap<>();
    private boolean expectingUnfinishedThread = false;

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                statement.evaluate();
                for (Map.Entry<Thread, Throwable> entry : exceptions.entrySet()) {
                    Thread thread = entry.getKey();
                    if (thread.isAlive() && !expectingUnfinishedThread) {
                        throw new RuntimeException("Test failed: thread " + thread.getId() + " is alive");
                    } else if (!thread.isAlive() && expectingUnfinishedThread) {
                        throw new RuntimeException("Test failed: thread " + thread.getId()
                                + " is expected to be alive");
                    }

                    if (entry.getValue() == null) {
                        if (expectedException != null) {
                            throw new RuntimeException("Test failed: expected " + expectedException.toString());
                        } else {
                            continue;
                        }
                    }

                    Class exception = entry.getValue().getClass();
                    if (!exception.equals(expectedException)) {
                        if (expectedException != null) {
                            throw new RuntimeException("Test failed: expected " + expectedException.toString()
                                    + " but got " + exception.toString() + " in thread " + thread.getId());
                        } else {
                            throw new RuntimeException("Test failed: unexpected exception: "
                                    + exception.toString() + " in thread " + thread.getId());
                        }
                    }
                }
            }
        };
    }

    public void expect(Class<? extends Throwable> e) {
        expectedException = e;
    }

    public void registerThread(Thread t) {
        exceptions.put(t, null);
        t.setUncaughtExceptionHandler(exceptions::put);
    }

    public void expectUnfinishedThread() {
        expectingUnfinishedThread = true;
    }
}
