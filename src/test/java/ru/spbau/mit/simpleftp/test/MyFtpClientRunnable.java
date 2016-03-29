package ru.spbau.mit.simpleftp.test;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import ru.spbau.mit.simpleftp.client.MyFtpClient;

public abstract class MyFtpClientRunnable extends MyFtpClient implements Runnable {
    public MyFtpClientRunnable(Path configPath, PrintStream log) {
        super(configPath, log);
        // TODO Auto-generated constructor stub
    }

    public abstract void doRun() throws Exception;

    @Override
    public void run() {
        try {
            connect();
            doRun();
        } catch (Exception e) {
            fail();
        } finally {
            try {
                closeSocket();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
