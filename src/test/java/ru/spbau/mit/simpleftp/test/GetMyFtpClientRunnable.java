package ru.spbau.mit.simpleftp.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import ru.spbau.mit.simpleftp.common.MyFtpGetResponse;
import ru.spbau.mit.simpleftp.common.MyFtpRequest;

public class GetMyFtpClientRunnable extends MyFtpClientRunnable {

    private Path serverPathFileToCheck;
    private Path downloadFilePath;
    private MyFtpRequest request;

    private static final int REQUEST_PACKAGES_NUM = 1;

    public GetMyFtpClientRunnable(Path configPath, PrintStream log, Path serverPathDirToCheck, Path downloadPath,
            MyFtpRequest request) {
        super(configPath, log);
        this.serverPathFileToCheck = serverPathDirToCheck;
        this.request = request;
        this.downloadFilePath = downloadPath;
    }

    @Override
    public void doRun() throws IOException {
        File srcFile = new File(serverPathFileToCheck.toString());
        for (int i = 0; i < REQUEST_PACKAGES_NUM; i++) {
            sendRequest(request);
            MyFtpGetResponse response = nextMyFtpGetResponse(downloadFilePath);
            assertFalse(response.isFailed());
            File gotFile = new File(response.getPath().toString());
            assertTrue(FileUtils.contentEquals(srcFile, gotFile));
        }
    }
}
