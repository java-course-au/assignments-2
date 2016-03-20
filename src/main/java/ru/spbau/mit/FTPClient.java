package ru.spbau.mit;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;

public class FTPClient {

    public static final int LIST = 1;
    public static final int GET = 2;
    private final String host;
    private final int port;
    private Socket clientSocket = null;
    public FTPClient(String hostName, int portNumber) throws IOException {
        host = hostName;
        port = portNumber;
        clientSocket = new Socket(host, port);
    }

    public InputStream makeRequest(int requestType, String requestBody) throws IOException {
        DataInputStream input = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

        output.writeInt(requestType);
        output.writeUTF(requestBody);
        output.flush();

        return input;
    }

    public ListResponse listRequestWrapper(int requestType, String requestBody) throws IOException {
        return new ListResponse(new DataInputStream(makeRequest(requestType, requestBody)));
    }

    public File getRequestWrapper(int requestType, String requestBody) throws IOException {
        File whereToSave = Files.createTempFile("get_response", "").toFile();
        processGetRequest((DataInputStream) makeRequest(requestType, requestBody),
                new FileOutputStream(whereToSave));
        return whereToSave;
    }

    private void processGetRequest(DataInputStream input, OutputStream output) throws IOException {
        int size = input.readInt();
        DataOutputStream out = new DataOutputStream(output);
        out.writeInt(size);
        IOUtils.copyLarge(input, out, 0, size);
        output.flush();
    }

    public void stop() throws IOException {
        clientSocket.close();
    }

    public static class ListResponse {
        private int size;
        private HashMap<String, Boolean> filesList = new HashMap<>();

        public ListResponse(DataInputStream input) throws IOException {
            size = input.readInt();
            for (int i = 0; i < size; i++) {
                String name = input.readUTF();
                Boolean isDir = input.readBoolean();
                filesList.put(name, isDir);
            }
        }

        public int getSize() {
            return size;
        }

        public HashMap<String, Boolean> getFilesList() {
            return filesList;
        }
    }

}
