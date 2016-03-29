package ru.spbau.mit.simpleftp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ru.spbau.mit.simpleftp.common.MyFtpListResponse;
import ru.spbau.mit.simpleftp.common.MyFtpRequest;

public class ListMyFtpClientRunnable extends MyFtpClientRunnable {

    private Path serverPathDirToCheck;
    private MyFtpRequest request;

    private static final int REQUEST_PACKAGES_NUM = 10;

    public ListMyFtpClientRunnable(
            Path configPath, PrintStream log, Path serverPathDirToCheck, MyFtpRequest request) {
        super(configPath, log);
        this.serverPathDirToCheck = serverPathDirToCheck;
        this.request = request;
    }

    @Override
    public void doRun() throws IOException {
        Set<String> paths = Files.list(serverPathDirToCheck).map(x -> x.getFileName().toString())
                .collect(Collectors.toSet());
        for (int i = 0; i < REQUEST_PACKAGES_NUM; i++) {
            sendRequest(request);
            MyFtpListResponse response = nextMyFtpListResponse();
            List<MyFtpListResponse.Entry> contents = response.getContents();
            assertEquals(contents.size(), paths.size());
            for (MyFtpListResponse.Entry entry : contents) {
                assertTrue(paths.contains(entry.getFileName()));
                assertEquals(entry.isDir(), Files.isDirectory(serverPathDirToCheck.resolve(entry.getFileName())));
            }
        }
    }
}
