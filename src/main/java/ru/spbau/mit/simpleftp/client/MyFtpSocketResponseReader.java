package ru.spbau.mit.simpleftp.client;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;

import ru.spbau.mit.simpleftp.common.MyFtpGetInputStreamResponse;
import ru.spbau.mit.simpleftp.common.MyFtpGetResponse;
import ru.spbau.mit.simpleftp.common.MyFtpListResponse;

public class MyFtpSocketResponseReader {
    private DataInputStream socketInputStream;

    public MyFtpSocketResponseReader(Socket socket) throws IOException {
        socketInputStream = new DataInputStream(socket.getInputStream());
    }

    public MyFtpGetInputStreamResponse nextMyFtpGetInputStreamResponse() throws IOException {
        MyFtpGetInputStreamResponse response = new MyFtpGetInputStreamResponse();
        response.setSize(socketInputStream.readLong());
        response.setInputStream(socketInputStream);
        return response;
    }

    public static MyFtpGetResponse downloadFullFile(MyFtpGetInputStreamResponse inputStreamResponse,
            Path newFileLocation) throws IOException {
        MyFtpGetResponse response = new MyFtpGetResponse();
        response.setSize(inputStreamResponse.getSize());
        response.setPath(newFileLocation);
        if (!response.isFailed()) {
            byte[] readWriteBlock = new byte[READ_WRITE_BLOCK_SIZE];
            try (BufferedOutputStream fileStream = new BufferedOutputStream(
                    new FileOutputStream(response.getPath().toString()))) {
                int gotBytes = 0;
                for (Long index = 0L; index < response.getSize(); index += gotBytes) {
                    gotBytes = inputStreamResponse.getInputStream().read(readWriteBlock);

                    if (gotBytes == -1) {
                        throw new IOException();
                    }

                    fileStream.write(readWriteBlock, 0, gotBytes);
                }
            }
        }
        return response;
    }

    private static final int READ_WRITE_BLOCK_SIZE = 4096;

    public MyFtpGetResponse nextMyFtpGetResponse(Path newFileLocation) throws IOException {
        return downloadFullFile(nextMyFtpGetInputStreamResponse(), newFileLocation);
    }

    public MyFtpListResponse nextMyFtpListResponse() throws IOException {
        MyFtpListResponse response = new MyFtpListResponse();
        int size = socketInputStream.readInt();
        for (int index = 0; index < size; index++) {
            String gotFileName;
            gotFileName = socketInputStream.readUTF();
            boolean isDir = socketInputStream.readBoolean();
            response.addEntry(new MyFtpListResponse.Entry(gotFileName, isDir));
        }
        return response;
    }

    public void close() throws IOException {
        socketInputStream.close();
    }
}
