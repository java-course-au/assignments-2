package ru.spbau.mit;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.internal.matchers.ThrowableCauseMatcher.hasCause;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;

/**
 * Created by ldvsoft on 15.03.16.
 */
public final class ThreadExpectedException implements TestRule {
    public static ThreadExpectedException none() {
        return new ThreadExpectedException();
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new ThreadExpectedExceptionStatement(statement);
    }

    public ThreadExpectedException reportMissingExceptionWithMessage(String message) {
        missingExceptionMessage = message;
        return this;
    }

    public ThreadExpectedException reportNotStoppedThreadWithMessage(String message) {
        notStoppedThreadMessage = message;
        return this;
    }

    public void expect(Matcher<?> matcher) {
        matcherBuilder.add(matcher);
    }

    public void expect(Class<? extends Throwable> type) {
        expect(instanceOf(type));
    }

    public void expectMessage(String substring) {
        expectMessage(containsString(substring));
    }

    public void expectMessage(Matcher<String> matcher) {
        expect(hasMessage(matcher));
    }

    public void expectCause(Matcher<? extends Throwable> expectedCause) {
        expect(hasCause(expectedCause));
    }

    public void registerThread(Thread t) {
        ExceptionCatcher catcher = new ExceptionCatcher();
        t.setUncaughtExceptionHandler(catcher);
        entries.add(new RegisteredEntry(t, catcher));
    }

    private String missingExceptionMessage = "Expected thread %s to throw %s";
    private String notStoppedThreadMessage = "Expected thread %s to be stopped";
    private final ExpectedExceptionMatcherBuilder matcherBuilder = new ExpectedExceptionMatcherBuilder();
    private final List<RegisteredEntry> entries = new ArrayList<>();

    private static final class RegisteredEntry {
        private final Thread thread;
        private final ExceptionCatcher catcher;

        private RegisteredEntry(Thread thread, ExceptionCatcher catcher) {
            this.thread = thread;
            this.catcher = catcher;
        }
    }

    private class ExceptionCatcher implements Thread.UncaughtExceptionHandler {
        private Throwable e = null;

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            this.e = e;
        }
    }

    private final class ThreadExpectedExceptionStatement extends Statement {
        private final Statement next;

        private ThreadExpectedExceptionStatement(Statement base) {
            next = base;
        }

        @Override
        public void evaluate() throws Throwable {
            next.evaluate();
            for (RegisteredEntry entry : entries) {
                if (entry.thread.isAlive()) {
                    failDueToNotStoppedThread(entry.thread);
                }
                if (entry.catcher.e != null) {
                    handleException(entry.catcher.e);
                } else if (isAnyExceptionExpected()) {
                    failDueToMissingException(entry.thread);
                }
            }
        }
    }

    private void handleException(Throwable e) throws Throwable {
        if (isAnyExceptionExpected()) {
            assertThat(e, matcherBuilder.build());
        } else {
            throw e;
        }
    }

    private boolean isAnyExceptionExpected() {
        return matcherBuilder.expectsThrowable();
    }

    private void failDueToMissingException(Thread t) throws AssertionError {
        fail(missingExceptionMessage(t));
    }

    private void failDueToNotStoppedThread(Thread t) throws AssertionError {
        fail(notStoppedThreadMessage(t));
    }

    private String missingExceptionMessage(Thread t) {
        String threadName = t.getName();
        String expectation = StringDescription.toString(matcherBuilder.build());
        return format(missingExceptionMessage, threadName, expectation);
    }

    private String notStoppedThreadMessage(Thread t) {
        String threadName = t.getName();
        return format(notStoppedThreadMessage, threadName);
    }
}
