/**
 * Created by n_buga on 31.03.16.
 */

import java.util.HashSet;
import java.util.Set;

public class TrackerFileInfo {
    private int id;
    private String name;
    private long size;
    private Set<ClientInfo> clientInfos = new HashSet<>();

    TrackerFileInfo(String name, long size, int id) {
        this.name = name;
        this.size = size;
        this.id = id;
    }

    public void addClient(ClientInfo clientInfo) {
        clientInfos.add(clientInfo);
    }

    public int getID() {
        return id;
    }

    public Set<ClientInfo> getClientInfos() {
        return clientInfos;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }
}
