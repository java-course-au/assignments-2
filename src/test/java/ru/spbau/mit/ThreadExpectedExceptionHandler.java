package ru.spbau.mit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.junit.Assert.assertTrue;

public class ThreadExpectedExceptionHandler implements TestRule {
    private ThreadExpectedException threadExpectedException = new ThreadExpectedException();
    private boolean expected = false;

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                boolean wasException = false;
                try {
                    threadExpectedException.apply(statement, description).evaluate();
                } catch (Throwable e) {
                    wasException = true;
                }
                assertTrue(wasException == expected);
            }
        };
    }

    public void setExpectedRuntimeException(boolean expected) {
        this.expected = expected;
    }

    public void expect(Class<? extends Throwable> e) {
        threadExpectedException.expect(e);
    }

    public void registerThread(Thread t) {
        threadExpectedException.registerThread(t);
    }
}

