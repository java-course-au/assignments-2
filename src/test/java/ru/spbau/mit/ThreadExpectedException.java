package ru.spbau.mit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ThreadExpectedException implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return null;
    }

    public void expect(Class<? extends Throwable> e) {
        
    }

    public void registerThread(Thread t) {

    }
}
