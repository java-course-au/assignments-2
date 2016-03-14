package ru.spbau.mit;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.HashMap;

public class FTPClient {

    public static final int LIST = 1;
    public static final int GET = 2;
    public static class ListResponse {
        public int size;
        public HashMap<String, Boolean> filesList = new HashMap<>();

        public ListResponse (DataInputStream input) throws IOException {
            size = input.readInt();
            for(int i = 0; i < size; i++) {
                String name = input.readUTF();
                Boolean isDir = input.readBoolean();
                filesList.put(name, isDir);
            }
        }
    }
    private static final int pageSize = 4096;

    private final String host;
    private final int port;
    private Socket clientSocket = null;
    public FTPClient(String hostName, int portNumber) throws IOException {
        host = hostName;
        port = portNumber;
        clientSocket = new Socket(host, port);
    }

    public Object makeRequest(int requestType, String requestBody, File whereToSave) throws IOException {
        DataInputStream input = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

        output.writeInt(requestType);
        output.writeUTF(requestBody);
        output.flush();

        if(requestType == LIST) {
            return new ListResponse(input);
        } else if(requestType == GET) {
            processGetRequest(input, new FileOutputStream(whereToSave));
            return  whereToSave;
        }

        return null;
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

}
