import java.util.Arrays;
import java.util.Set;

/**
 * Created by n_buga on 31.03.16.
 */
public class Client {
    private byte[] serverIP;
    private short serverPort;

    public Client(byte[] serverIP, short port) {
        this.serverIP = serverIP;
        serverPort = port;
    }

    public byte[] getServerIP() {
        return serverIP;
    }

    public short getServerPort() {
        return serverPort;
    }

    public boolean equals(Client client) {
        return (Arrays.equals(serverIP, client.getServerIP()) && serverPort == client.getServerPort());
    }
}
