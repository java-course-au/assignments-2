import junit.framework.TestCase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.BoundedInputStream;

/**
 * Created by n_buga on 14.03.16.
 */

public class ClientTest extends TestCase {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 20005;
    private static final String TEST_STRING = "Hello!";

    public void testGet() throws Exception {
        try (Server testServer = new Server(PORT)) {
            Client testClient = new Client(HOST, PORT);
            File pathOut;
            File pathIn;
            try {
                pathOut = File.createTempFile("abc", "");
                pathIn = File.createTempFile("bde", "");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            BoundedInputStream result = testClient.get(pathIn.toString());
            assertTrue(result.available() == 0);
            assertTrue(pathIn.length() == 0);

            result.close();

            try (DataOutputStream os = new DataOutputStream(new FileOutputStream(pathIn))) {
                os.writeUTF(TEST_STRING);
            }

            try (DataOutputStream os = new DataOutputStream(new FileOutputStream(pathOut))) {
                result = testClient.get(pathIn.toString());
                while (result.available() > 0) {
                    os.writeByte(result.read());
                }
                assertTrue(FileUtils.contentEquals(pathIn, pathOut));
            }
            pathIn.delete();
            pathOut.delete();

        }
    }

    public void testList() throws Exception {
        try (Server testServer = new Server(PORT + 1)) {

            final int countOfFiles = 5;

            Client testClient = new Client(HOST, PORT + 1);

            Path path = Files.createTempDirectory("");
            ArrayList<FTPfile> list = testClient.list(path.toString());
            assertTrue(list.size() == 0);

            ArrayList<Path> innerDirs = new ArrayList<>();

            for (int j = 0; j < countOfFiles; j++) {
                innerDirs.add(Files.createTempDirectory(path, ""));
            }

            list = testClient.list(path.toString());
            assertTrue(list.size() == countOfFiles);
            for (FTPfile ftpfile : list) {
                assertTrue(ftpfile.isDirectory());
            }

            for (int j = 0; j < countOfFiles; j++) {
                innerDirs.get(j).toFile().delete();
                innerDirs.set(j, Files.createTempFile(path, "", ""));
            }

            list = testClient.list(path.toString());
            assertTrue(list.size() == countOfFiles);
            for (FTPfile ftpfile : list) {
                assertTrue(!ftpfile.isDirectory());
            }

            for (int j = 0; j < countOfFiles; j++) {
                innerDirs.get(j).toFile().delete();
            }

            path.toFile().delete();
        }
    }
}
