package ru.spbau.mit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.Statement;

import java.util.HashMap;
import java.util.Map;

public class ThreadExpectedException implements TestRule {

    private final HashMap<Thread, Throwable> threads = new HashMap<>();
    private Class<? extends Throwable> expected = null;

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
                for (Map.Entry<Thread, Throwable> entry : threads.entrySet()) {
                    if (entry.getKey().getState() != Thread.State.TERMINATED) {
                        throw new Exception("The thread is not stopped");
                    }
                    Throwable value = entry.getValue();
                    if((value == null && expected != null)
                            || (value != null && expected == null)
                            || (expected != null && !value.getClass().isAssignableFrom(expected))) {
                        throw new Exception("The thread threw an inappropriate exception");
                    }
                }
            }
        };
    }
}
