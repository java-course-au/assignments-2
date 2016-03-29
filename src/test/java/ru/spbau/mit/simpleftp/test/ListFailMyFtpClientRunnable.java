package ru.spbau.mit.simpleftp.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import ru.spbau.mit.simpleftp.common.MyFtpListResponse;
import ru.spbau.mit.simpleftp.common.MyFtpRequest;

public class ListFailMyFtpClientRunnable extends MyFtpClientRunnable {
    private MyFtpRequest request;

    private static final int REQUEST_PACKAGES_NUM = 10;

    public ListFailMyFtpClientRunnable(Path configPath, PrintStream log, MyFtpRequest request) {
        super(configPath, log);
        this.request = request;
    }

    @Override
    public void doRun() throws IOException {
        for (int i = 0; i < REQUEST_PACKAGES_NUM; i++) {
            sendRequest(request);
            MyFtpListResponse response = nextMyFtpListResponse();
            assertTrue(response.isFailed());
        }
    }
}
