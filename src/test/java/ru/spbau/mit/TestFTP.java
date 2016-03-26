package ru.spbau.mit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestFTP {
    private static final int PORT = 12345;
    private static final String LOCALHOST = "localhost";

    private static final String RES = "src/test/resources/";
    private static final String CHECKSTYLE_FILE = RES + "checkstyle.xml";
    private static final String CHECKSTYLE_SUPPRESSIONS_FILE = RES + "checkstyle-suppressions.xml";
    private static final FTPFileEntry CHECKSTYLE = new FTPFileEntry(
            CHECKSTYLE_FILE,
            false
    );
    private static final FTPFileEntry CHECKSTYLE_SUPPRESSIONS = new FTPFileEntry(
            CHECKSTYLE_SUPPRESSIONS_FILE,
            false
    );

    @Test
    public void testListEmpty() throws IOException {
        try (
                @SuppressWarnings("unused")
                FTPServer server = new FTPServer(PORT + 1);
                FTPClient client = new FTPClient(LOCALHOST, PORT + 1)
        ) {
            List<FTPFileEntry> list = client.list(LOCALHOST);
            assertEquals(Collections.emptyList(), list);
        }
    }

    @Test
    public void testList() throws IOException {
        try (
                @SuppressWarnings("unused")
                FTPServer server = new FTPServer(PORT + 2);
                FTPClient client = new FTPClient(LOCALHOST, PORT + 2)
        ) {
            List<FTPFileEntry> list = client.list(RES);
            List<FTPFileEntry> expected = Arrays.asList(CHECKSTYLE, CHECKSTYLE_SUPPRESSIONS);
            assertTrue(expected.containsAll(list) && list.containsAll(expected));
        }
    }

    @Test
    public void testGetEmpty() throws IOException {
        Path tmpFile = Files.createTempFile("", "");
        try (
                @SuppressWarnings("unused")
                FTPServer server = new FTPServer(PORT + 3);
                FTPClient client = new FTPClient(LOCALHOST, PORT + 3);
                OutputStream os = Files.newOutputStream(tmpFile)
        ) {
            IOUtils.copy(client.get(LOCALHOST), os);
            assertEquals(0, Files.size(tmpFile));
        } finally {
            Files.delete(tmpFile);
        }
    }

    @Test
    public void testGet() throws IOException {
        Path tmpFile = Files.createTempFile("", "");
        try (
                @SuppressWarnings("unused")
                FTPServer server = new FTPServer(PORT + 4);
                FTPClient client = new FTPClient(LOCALHOST, PORT + 4);
                OutputStream os = Files.newOutputStream(tmpFile)
        ) {
            IOUtils.copy(client.get(CHECKSTYLE_FILE), os);
            assertTrue(FileUtils.contentEquals(tmpFile.toFile(), new File(CHECKSTYLE_FILE)));
        } finally {
            Files.delete(tmpFile);
        }
    }
}
