import java.util.Arrays;

/**
 * Created by n_buga on 31.03.16.
 */
public class ClientInfo {
    private byte[] serverIP;
    private int serverPort;

    public ClientInfo(byte[] serverIP, int port) {
        this.serverIP = serverIP;
        serverPort = port;
    }

    public byte[] getServerIP() {
        return serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public boolean myEquals(ClientInfo clientInfo) {
        return (Arrays.equals(serverIP, clientInfo.getServerIP()) && serverPort == clientInfo.getServerPort());
    }
}
