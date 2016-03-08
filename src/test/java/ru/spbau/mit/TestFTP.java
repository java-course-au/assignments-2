package ru.spbau.mit;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestFTP {
    private static final int PORT = 12345;
    private static final String LOCALHOST = "localhost";

    private static final String RES = "src/test/resources/";
    private static final String CHECKSTYLE_FILE = RES + "checkstyle.xml";
    private static final FTPFileEntry CHECKSTYLE = new FTPFileEntry(
            CHECKSTYLE_FILE,
            false
    );

    @Test
    public void testList() throws IOException {
        try (
                @SuppressWarnings("unused")
                FTPServer server = new FTPServer(PORT);
        ) {
            FTPClient client = new FTPClient(LOCALHOST, PORT);
            List<FTPFileEntry> list1 = client.list(LOCALHOST);
            assertEquals(new ArrayList<>(), list1);

            List<FTPFileEntry> list2 = client.list(RES);
            assertEquals(Collections.singletonList(CHECKSTYLE), list2);
        }
    }

    @Test
    public void testGet() throws IOException {
        Path tmpFile = Files.createTempFile("", "");
        try (
                @SuppressWarnings("unused")
                FTPServer server = new FTPServer(PORT);
        ) {
            FTPClient client = new FTPClient(LOCALHOST, PORT);

            try (OutputStream os = Files.newOutputStream(tmpFile)) {
                client.get(LOCALHOST, os);
            }
            assertEquals(0, Files.size(tmpFile));

            try (OutputStream os = Files.newOutputStream(tmpFile)) {
                client.get(CHECKSTYLE_FILE, os);
            }
            assertTrue(FileUtils.contentEquals(tmpFile.toFile(), new File(CHECKSTYLE_FILE)));
        }
    }
}
