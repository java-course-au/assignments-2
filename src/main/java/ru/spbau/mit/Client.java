package ru.spbau.mit;

import java.io.IOException;
import java.net.Socket;

public class Client {
    private static final int MAX_BYTES = 1000000;

    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", Server.PORT)) {
            byte[] bytes = new byte[MAX_BYTES];
            int offset = 0;
            while (true) {
                int r = socket.getInputStream().read(bytes, offset, bytes.length - offset);
                System.out.println(r);
                if (r == -1) {
                    break;
                }
                offset += r;
            }
            for (int i = 0; i < offset; ++i) {
                System.out.printf("%c", bytes[i]);
            }
        }
    }
}
