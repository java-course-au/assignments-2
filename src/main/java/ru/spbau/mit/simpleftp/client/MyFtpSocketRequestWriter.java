package ru.spbau.mit.simpleftp.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import ru.spbau.mit.simpleftp.common.MyFtpRequest;

public class MyFtpSocketRequestWriter {
    private DataOutputStream dataStream;

    public MyFtpSocketRequestWriter(Socket socket) throws IOException {
        dataStream = new DataOutputStream(socket.getOutputStream());
    }

    public void writeMyFtpRequest(MyFtpRequest request) throws IOException {
        switch (request.getType()) {
            case LIST:
                dataStream.writeInt(1);
                break;
            case GET:
                dataStream.writeInt(2);
                break;
            default:
                break;
        }
        dataStream.writeUTF(request.getPath().toString());
        dataStream.flush();
    }

    public void close() throws IOException {
        dataStream.close();
    }
}
