package ru.spbau.mit;

import java.io.*;
import java.net.Socket;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

final class SocketTestUtils {
    private SocketTestUtils() {}

    static byte[] getBytes(ThrowingConsumer<DataOutputStream, IOException> streamWriter) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(buffer);
        streamWriter.consume(stream);
        buffer.close();
        return buffer.toByteArray();
    }

    static void checkSocketIO(
            ThrowingConsumer<DataOutputStream, IOException> inputWriter,
            ThrowingConsumer<Socket, IOException> testedCode,
            ThrowingConsumer<DataOutputStream, IOException> expectedOutputWriter)
            throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(getBytes(inputWriter));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        testedCode.consume(socket);
        verify(socket, times(1)).close();

        byte[] expected = getBytes(expectedOutputWriter);
        assertArrayEquals(expected, out.toByteArray());
    }
}
