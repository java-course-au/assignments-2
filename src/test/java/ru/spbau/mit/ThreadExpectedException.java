package ru.spbau.mit;

import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ThreadExpectedException implements TestRule {

    private Class<? extends Throwable> expected = null;
    private final HashMap<Thread, Throwable> threads = new HashMap<>();

    public void expect(Class<? extends Throwable> e) {
        expected = e;
    }

    public void registerThread(Thread t) {
        threads.put(t, null);
        t.setUncaughtExceptionHandler((t1, e) -> threads.replace(t1, e));
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                statement.evaluate();
                for (Map.Entry<Thread, Throwable> entry: threads.entrySet()) {
                    if (entry.getKey().getState() != Thread.State.TERMINATED) {
                        throw new Exception("The thread is not stopped");
                    }
                    Throwable value = entry.getValue();
                    Class classs = value.getClass();
                    if (!classs.isAssignableFrom(expected)) {
                        throw new Exception("The thread threw an inappropriate exception");
                    }
                }
            }
        };
    }
}