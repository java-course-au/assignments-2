package ru.spbau.mit;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;

public class Tests {
    @Test
    public void testGet() throws IOException {
        Path path = Files.createTempDirectory("FTP");
        File file = new File(path.toString() + File.separator + "tmp");

        PrintWriter writer = new PrintWriter(file);

        String fileString = "test   @";
        writer.print(fileString);

        writer.close();

        OutputStream output = new OutputStream() {
            private StringBuilder string = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                this.string.append((char) b);
            }

            public String toString() {
                return this.string.toString();
            }
        };

        Server server = new Server(12345);
        server.start();

        Client client = new Client("localhost", 12345);

        try {
            assertEquals(client.get(path.toString() + File.separator + "tmp", output), fileString.length());

            assertEquals(output.toString(), fileString);
        } finally {
            server.stop();
            client.close();
        }
    }

    @Test
    public void testList() throws IOException {
        String[] fileName = new String[]{"dir1", "dir2", "dir3", "dir4", "file1", "file2", "file3", "file4"};

        Path path = Files.createTempDirectory("FTP");
        (new File(path.toString() + File.separator + fileName[0])).mkdir();
        (new File(path.toString() + File.separator + fileName[1])).mkdir();
        (new File(path.toString() + File.separator + fileName[2])).mkdir();
        (new File(path.toString() + File.separator + fileName[3])).mkdir();
        (new File(path.toString() + File.separator + fileName[4])).createNewFile();
        (new File(path.toString() + File.separator + fileName[5])).createNewFile();
        (new File(path.toString() + File.separator + fileName[6])).createNewFile();
        (new File(path.toString() + File.separator + fileName[7])).createNewFile();

        Server server = new Server(12345);
        server.start();


        Client client = new Client("localhost", 12345);
        try {
            ArrayList<Client.FileEntry> ls = client.list(path.toString());
            assertEquals(ls.size(), 8);
            ls.sort(new Comparator<Client.FileEntry>() {
                @Override
                public int compare(Client.FileEntry o1, Client.FileEntry o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            for (int i = 0; i < 4; ++i) {
                assertEquals(ls.get(i).getDir(), true);
            }


            for (int i = 4; i < 8; ++i) {
                assertEquals(ls.get(i).getDir(), false);
            }

            for (int i = 0; i < 8; ++i) {
                assertEquals(ls.get(i).getName(), fileName[i]);
            }

            new File(path.toString()).delete();
        } finally {
            server.stop();
            client.close();
        }
    }
}
