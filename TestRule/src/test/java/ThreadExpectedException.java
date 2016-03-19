import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import java.util.ArrayList;

/**
 * Created by n_buga on 19.03.16.
 */


public class ThreadExpectedException implements TestRule {
    private ArrayList<Thread> registeredThreads = new ArrayList<>();
    private Class<? extends Throwable> expectedException = null;
    private ArrayList<Throwable> thrownExceptions = new ArrayList<>();

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                statement.evaluate();
                registeredThreads.stream().filter(Thread::isAlive).forEach(java.lang.Thread::interrupt);
                if (expectedException != null) {
                    if (thrownExceptions.size() == 0) {
                        throw new Exception("Expected exception. There wasn't any exception");
                    }
                    for (Throwable e : thrownExceptions) {
                        if (e.getClass() != expectedException) {
                            throw new Exception("Expected only " + expectedException.getName());
                        }
                    }
                }
            }
        };
    }
    public void registerThread(Thread t) {
        registeredThreads.add(t);
        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                thrownExceptions.add(e);
            }
        });
    }
    public void expect(Class<? extends Throwable> e) {
        expectedException = e;
    }
}
