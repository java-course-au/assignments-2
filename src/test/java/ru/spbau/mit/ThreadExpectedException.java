package ru.spbau.mit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.HashMap;
import java.util.Map;

public class ThreadExpectedException implements TestRule {
    private Class<? extends Throwable> expectedException = null;
    private Map<Thread, Throwable> threads = new HashMap<>();

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                base.evaluate();
                for (Map.Entry<Thread, Throwable> entry : threads.entrySet()) {
                    Thread thread = entry.getKey();
                    Throwable throwable = entry.getValue();
                    if (thread.getState() != Thread.State.TERMINATED) {
                        throw new Exception("Thread " + thread.getName() + " is not terminated");
                    }
                    if ((expectedException == null && throwable != null)
                            || (expectedException != null
                            && (throwable == null || !throwable.getClass().isAssignableFrom(expectedException)))) {
                        throw new Exception("Unexpected exception in thread " + thread.getName());
                    }
                }
            }
        };
    }

    public void expect(Class<? extends Throwable> e) {
        expectedException = e;
    }

    public void registerThread(Thread thread) {
        threads.put(thread, null);
        thread.setUncaughtExceptionHandler(threads::replace);
    }
}
