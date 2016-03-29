package ru.spbau.mit.simpleftp.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Paths;

import ru.spbau.mit.simpleftp.common.MyFtpRequest;

public class MyFtpSocketRequestReader {
    public static class MyFtpParseRequestException extends Exception {
        private static final long serialVersionUID = 4448519501858646038L;
    }

    private DataInputStream inputStream;

    public MyFtpSocketRequestReader(Socket socket) throws IOException {
        this.inputStream = new DataInputStream(socket.getInputStream());
    }

    public MyFtpRequest nextMyFtpRequest() throws IOException, MyFtpParseRequestException {
        MyFtpRequest parsedRequest = new MyFtpRequest();
        int type = inputStream.readInt();
        switch (type) {
            case 1:
                parsedRequest.setType(MyFtpRequest.Type.LIST);
                break;
            case 2:
                parsedRequest.setType(MyFtpRequest.Type.GET);
                break;
            default:
                throw new MyFtpParseRequestException();
        }
        parsedRequest.setPath(Paths.get(inputStream.readUTF()));
        return parsedRequest;
    }

    public void close() throws IOException {
        inputStream.close();
    }
}
