import junit.framework.TestCase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

/**
 * Created by n_buga on 14.03.16.
 */

public class ClientTest extends TestCase {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 20005;
    private static final String TEST_STRING = "Hello!";

    public void testGet() throws Exception {
        try (
                Server testServer = new Server(PORT);
                Client testClient = new Client(HOST, PORT); )
        {
            File pathOut;
            File pathIn;
            try {
                pathOut = File.createTempFile("abc", "");
                pathIn = File.createTempFile("bde", "");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            try (OutputStream os = new FileOutputStream(pathOut)) {
                testClient.get(pathIn.toString(), os);
                assert (pathOut.length() == 0);
                assert (pathIn.length() == 0);
            }

            try (DataOutputStream os = new DataOutputStream(new FileOutputStream(pathIn))) {
                os.writeUTF(TEST_STRING);
            }

            try (OutputStream os = new FileOutputStream(pathOut)) {
                testClient.get(pathIn.toString(), os);
                assert (FileUtils.contentEquals(pathIn, pathOut));
            }
            pathIn.delete();
            pathOut.delete();
        }
    }

    public void testList() throws Exception {
        try (Server testServer = new Server(PORT + 1);
             Client testClient = new Client(HOST, PORT +1 );){

            Path path = Files.createTempDirectory("");
            ArrayList<FTPfile> list = testClient.list(path.toString());
            assert (list.size() == 0);

            ArrayList<Path> innerDirs = new ArrayList<>();

            for (int j = 0; j < 5; j++) {
                innerDirs.add(Files.createTempDirectory(path, ""));
            }

            list = testClient.list(path.toString());
            assert (list.size() == 5);
            for (FTPfile ftpfile : list) {
                assert (ftpfile.isDirectory);
            }

            for (int j = 0; j < 5; j++) {
                innerDirs.get(j).toFile().delete();
                innerDirs.set(j, Files.createTempFile(path, "", ""));
            }

            list = testClient.list(path.toString());
            assert (list.size() == 5);
            for (FTPfile ftpfile : list) {
                assert !ftpfile.isDirectory;
            }

            for (int j = 0; j < 5; j++) {
                innerDirs.get(j).toFile().delete();
            }

            path.toFile().delete();
        }
    }
}