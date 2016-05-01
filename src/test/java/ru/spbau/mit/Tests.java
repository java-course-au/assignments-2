package ru.spbau.mit;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class Tests {
    @Test
    public void testListAndGet() {
        Thread server = new Thread(() -> {
            try {
                new Server(8081).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Client client = new Client("localhost", 8081);
            List<FileInfo> files = client.sendListQuery("src/test/resources");
            assertSame(files.size(), 3);
            assertTrue(files.contains(new FileInfo("dir1", true)));
            assertTrue(files.contains(new FileInfo("dir2", true)));
            assertTrue(files.contains(new FileInfo("checkstyle.xml", false)));

            byte[] buffer = IOUtils.toByteArray(client.sendGetQuery("src/test/resources/dir2/a.txt"));
            byte[] expectedBuffer = IOUtils.toByteArray(new FileInputStream("src/test/resources/dir2/a.txt"));

            assertArrayEquals(expectedBuffer, buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
