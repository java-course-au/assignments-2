package ru.spbau.mit.simpleftp.server;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import ru.spbau.mit.simpleftp.common.MyFtpGetResponse;
import ru.spbau.mit.simpleftp.common.MyFtpListResponse;
import ru.spbau.mit.simpleftp.common.MyFtpResponse;

public class MyFtpSocketResponseWriter {
    private DataOutputStream dataStream;

    public MyFtpSocketResponseWriter(Socket socket) throws IOException {
        dataStream = new DataOutputStream(socket.getOutputStream());
    }

    private void writeMyFtpListResponse(MyFtpListResponse response) throws IOException {
        dataStream.writeInt(response.getContents().size());
        for (MyFtpListResponse.Entry entry : response.getContents()) {
            dataStream.writeUTF(entry.getFileName());
            dataStream.writeBoolean(entry.isDir());
        }
        dataStream.flush();
    }

    private static final int READ_WRITE_BLOCK_SIZE = 4096;

    private void writeMyFtpGetResponse(MyFtpGetResponse response) throws IOException {
        MyFtpGetResponse getResponse = (MyFtpGetResponse) response;
        dataStream.writeLong(getResponse.getSize());
        dataStream.flush();
        /* old staff */
        if (!response.isFailed()) {
            byte[] readWriteBlock = new byte[READ_WRITE_BLOCK_SIZE];
            try (BufferedInputStream stream = new BufferedInputStream(
                    new FileInputStream(response.getPath().toString()))) {
                while (true) {
                    int gotBytes = stream.read(readWriteBlock);

                    if (gotBytes == -1) {
                        break;
                    }

                    dataStream.write(readWriteBlock, 0, gotBytes);
                    dataStream.flush();
                }
            }
        }
    }

    public void writeMyFtpResponse(MyFtpResponse response) throws IOException {
        if (response instanceof MyFtpListResponse) {
            writeMyFtpListResponse((MyFtpListResponse) response);
        } else {
            writeMyFtpGetResponse((MyFtpGetResponse) response);
        }
    }

    public void close() throws IOException {
        dataStream.close();
    }
}
