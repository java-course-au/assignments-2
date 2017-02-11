package ru.spbau.mit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ThreadExpectedException implements TestRule {
    private Class<? extends Throwable> exception = null;
    private ArrayList<Thread> registerThreads = new ArrayList<>();

    private Map<Thread, Class<? extends Throwable>> threadsException = new HashMap<>();

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                statement.evaluate();

                for (Thread t : registerThreads) {
                    if (t.getState() == Thread.State.TERMINATED) {
                        if (threadsException.containsKey(t) && exception == null) {
                            throw new Exception("Thread " + t.getName() + " throw exception "
                                    + threadsException.get(t).getName() + " but waiting not exception");
                        }
                        if (!threadsException.containsKey(t) && exception != null) {
                            throw new Exception("Thread " + t.getName() + " doesn't throw exception "
                                    + " but waiting exception");
                        }
                        if (threadsException.get(t) != exception) {
                            throw new Exception("Thread " + t.getName() + " throw exception "
                                    + threadsException.get(t).getName() + " but waiting " + exception.getName());
                        }
                    } else {
                        throw new Exception("Thread " + t.getName() + " is not in terminated state");
                    }
                }
            }
        };
    }

    public void expect(Class<? extends Throwable> e) {
        exception = e;
    }

    public void registerThread(Thread t) {
        registerThreads.add(t);
        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                                          @Override
                                          public void uncaughtException(Thread t, Throwable e) {
                                              threadsException.put(t, e.getClass());
                                          }
                                      }
        );
    }

}
