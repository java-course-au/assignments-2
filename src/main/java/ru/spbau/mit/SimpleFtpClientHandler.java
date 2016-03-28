package ru.spbau.mit;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.spbau.mit.SimpleFtpProtocol.*;
import static ru.spbau.mit.SimpleFtpProtocol.COMMAND_GET;

class SimpleFtpClientHandler implements Runnable {
    private final Path rootPath;
    private final Socket client;

    SimpleFtpClientHandler(Path rootPath, Socket client) {
        this.rootPath = rootPath;
        this.client = client;
    }

    @Override
    public void run() {
        try (
            DataInputStream in = new DataInputStream(client.getInputStream());
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
        ) {
            new Handler(in, out).run();
        } catch (EOFException e) {
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!client.isClosed()) {
                try {
                    client.shutdownInput();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    client.shutdownOutput();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final class Handler {
        private final DataInputStream in;
        private final DataOutputStream out;

        private Handler(DataInputStream in, DataOutputStream out) {
            this.in = in;
            this.out = out;
        }

        public void run() throws IOException {
            while (true) {
                int command = in.readInt();
                switch (command) {
                    case COMMAND_LIST:
                        handleList();
                        break;
                    case COMMAND_GET:
                        handleGet();
                        break;
                    default:
                        throw new RuntimeException("Invalid command received from client");
                }
            }
        }

        private void handleList() throws IOException {
            Path dir = rootPath.resolve(in.readUTF());
            if (!Files.isDirectory(dir)) {
                out.writeInt(0);
                return;
            }
            List<Path> content =
                    Files.list(dir)
                            .sorted(Comparator.comparing(Path::getFileName))
                            .collect(Collectors.toList());
            out.writeInt(content.size());
            for (Path inside : content) {
                out.writeUTF(inside.getFileName().toString());
                out.writeBoolean(Files.isDirectory(inside));
            }
        }

        private void handleGet() throws IOException {
            Path file = rootPath.resolve(in.readUTF());
            if (Files.isDirectory(file)) {
                out.writeInt(0);
                return;
            }
            try (InputStream fin = Files.newInputStream(file)) {
                out.writeInt((int) Files.size(file));
                IOUtils.copyLarge(fin, out);
            } catch (NoSuchFileException | UnsupportedOperationException | AccessDeniedException e) {
                out.writeInt(0);
            }
        }
    }
}
