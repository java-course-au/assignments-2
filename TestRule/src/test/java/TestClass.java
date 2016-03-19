/**
 * Created by n_buga on 19.03.16.
 */

import org.junit.Rule;
import org.junit.Test;

import java.net.ConnectException;
import java.util.ArrayList;

public class TestClass {

    @Rule
    public ThreadExpectedException threadExpectedException = new ThreadExpectedException();

    @Test
    public void throwExceptionTest() throws InterruptedException {
        threadExpectedException.expect(RuntimeException.class);
        Thread th = new Thread(() -> {
            throw new RuntimeException();
        });
        threadExpectedException.registerThread(th);
        th.start();
        th.join();
    }

    @Test
    public void notThrowExceptionTest() {
        threadExpectedException.expect(Exception.class);
        Thread th = new Thread(() -> {

        });
        threadExpectedException.registerThread(th);
        th.start();
    }

    @Test
    public void wrongThrowException() {
        threadExpectedException.expect(ConnectException.class);
        Thread th = new Thread(() -> {
            throw new RuntimeException();
        });
        threadExpectedException.registerThread(th);
        th.start();
    }

    @Test
    public void manyThreadsTest() {
        for (int i = 0; i < 5; i++) {
            Thread th = (new Thread(() -> {
                while (!Thread.interrupted()) {
                    System.out.println(Thread.currentThread().getName());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }));
            threadExpectedException.registerThread(th);
            th.start();
        }
    }

    @Test
    public void throwExceptionManyThreadsTest() {
        threadExpectedException.expect(RuntimeException.class);
        for (int i = 0; i < 5; i++) {
            Thread th = new Thread(() -> {
                throw new RuntimeException();
            });
            threadExpectedException.registerThread(th);
            th.start();
        }
    }

    @Test
    public void throwWrongExceptionManyThreadsTest() {
        threadExpectedException.expect(RuntimeException.class);
        for (int i = 0; i < 5; i++) {
            Thread th = new Thread(() -> {
                while (!Thread.interrupted()) {
                    System.out.println(Thread.currentThread().getName());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });
            threadExpectedException.registerThread(th);
            th.start();
        }
        Thread th = new Thread(() -> {
            ArrayList emptyList = new ArrayList();
            emptyList.get(0);
        });
        threadExpectedException.registerThread(th);
        th.start();
    }
}
