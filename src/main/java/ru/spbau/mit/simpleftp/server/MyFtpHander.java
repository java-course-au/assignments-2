package ru.spbau.mit.simpleftp.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;

import ru.spbau.mit.simpleftp.common.MyFtpGetResponse;
import ru.spbau.mit.simpleftp.common.MyFtpListResponse;
import ru.spbau.mit.simpleftp.common.MyFtpRequest;
import ru.spbau.mit.simpleftp.common.MyFtpResponse;
import ru.spbau.mit.simpleftp.server.MyFtpServer.MyFtpSharedComponents;

public class MyFtpHander implements Runnable {
    private Socket socket;
    private MyFtpSharedComponents sharedComponents;
    private MyFtpSocketRequestReader requestReader;
    private MyFtpSocketResponseWriter responseWriter;

    MyFtpHander(Socket socket, MyFtpSharedComponents sharedComponents) {
        this.socket = socket;
        this.sharedComponents = sharedComponents;
    }

    @Override
    public void run() {
        try {
            requestReader = new MyFtpSocketRequestReader(socket);
            responseWriter = new MyFtpSocketResponseWriter(socket);
        } catch (IOException e) {
            e.printStackTrace(sharedComponents.log);
            return;
        }

        String hostAddr = socket.getInetAddress().getHostAddress();

        sharedComponents.log.println(String.format("New client %s have connected", hostAddr));

        for (int requestId = 0;; requestId++) {
            MyFtpRequest request = null;
            try {
                request = requestReader.nextMyFtpRequest().resolvePathWithRootDir(sharedComponents.rootDir);
            } catch (IOException e) {
                e.printStackTrace(sharedComponents.log);
                break;
            } catch (MyFtpSocketRequestReader.MyFtpParseRequestException e) {
                e.printStackTrace(sharedComponents.log);
                continue;
            }

            sharedComponents.log.println(
                    String.format("Client %s send %d request %s", hostAddr, requestId, request.toString()));

            MyFtpResponse response;
            try {
                response = dispatchResponse(request);
            } catch (MyFtpDispatchException e1) {
                e1.printStackTrace(sharedComponents.log);
                continue;
            }

            try {
                responseWriter.writeMyFtpResponse(response);
            } catch (IOException e) {
                e.printStackTrace(sharedComponents.log);
            }
            sharedComponents.log.println(
                    String.format("Response to client %s to %d request have sended", hostAddr, requestId));
        }

        sharedComponents.log.println(String.format("Client %s have disconnected", hostAddr));
        closeSocket();
    }

    private void closeSocket() {
        try {
            requestReader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace(sharedComponents.log);
        }

        try {
            responseWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace(sharedComponents.log);
        }

        try {
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace(sharedComponents.log);
        }
    }

    private class MyFtpDispatchException extends Exception{
        private static final long serialVersionUID = 8964566548132270723L;
    }

    private MyFtpResponse dispatchResponse(MyFtpRequest request) throws MyFtpDispatchException {
        switch (request.getType()) {
            case LIST:
                return getResponseFromListResponse(request);
            case GET:
                return getResponseFromGetResponse(request);
            default:
                throw new MyFtpDispatchException();
        }
    }

    private MyFtpListResponse getResponseFromListResponse(MyFtpRequest request) {
        MyFtpListResponse response = new MyFtpListResponse();
        if (!Files.isDirectory(request.getPath())) {
            sharedComponents.log.println(request.getPath().toString() + " isn't dir");
            response.setFailed();
            return response;
        }
        try {
            Files.list(request.getPath()).forEach(filePath -> {
                response.addEntry(new MyFtpListResponse.Entry(filePath.getFileName().toString(),
                        Files.isDirectory(filePath)));
            });
        } catch (IOException e) {
            response.setFailed();
            e.printStackTrace(sharedComponents.log);
        }
        return response;
    }

    private MyFtpGetResponse getResponseFromGetResponse(MyFtpRequest request) {
        MyFtpGetResponse response = new MyFtpGetResponse();
        if (!Files.exists(request.getPath())) {
            sharedComponents.log.println(request.getPath().toString() + " isn't exists");
            response.setFailed();
            return response;
        }
        response.setPath(request.getPath());
        try {
            response.setSize(Files.size(request.getPath()));
        } catch (IOException e) {
            e.printStackTrace(sharedComponents.log);
            response.setFailed();
        }
        return response;
    }

}
