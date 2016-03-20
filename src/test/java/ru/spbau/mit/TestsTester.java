package ru.spbau.mit;

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestsTester extends ParentRunner<Class> {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface ShouldSucceed {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface ShouldFail {}

    public TestsTester(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected List<Class> getChildren() {
        List<Class> result = new ArrayList<>();
        for (Class<?> c : getTestClass().getJavaClass().getClasses()) {
            if (c.getAnnotation(ShouldSucceed.class) == null && c.getAnnotation(ShouldFail.class) == null) {
                continue;
            }
            if (c.getAnnotation(Ignore.class) != null) {
                continue;
            }
            if (c.getAnnotation(ShouldSucceed.class) != null && c.getAnnotation(ShouldFail.class) != null) {
                throw new IllegalArgumentException("Class is annotated with both ShouldSucceed and ShouldFail");
            }
            result.add(c);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Description describeChild(Class aClass) {
        return Description.createTestDescription(aClass.getSimpleName(),
                aClass.getAnnotation(ShouldSucceed.class) != null ? "shouldSucceed" : "shouldFail"
                );
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void runChild(Class aClass, RunNotifier runNotifier) {
        Description d = describeChild(aClass);
        runNotifier.fireTestStarted(d);
        Result result = new JUnitCore().run(aClass);
        try {
            assertEquals("Tests run", 1, result.getRunCount());
            if (aClass.getAnnotation(ShouldFail.class) != null) {
                assertEquals("Failure count", 1, result.getFailureCount());
            } else {
                assertEquals("Failure count", 0, result.getFailureCount());
            }
        } catch (AssertionError e) {
            runNotifier.fireTestFailure(new Failure(d, e));
        }
        runNotifier.fireTestFinished(d);
    }
}
