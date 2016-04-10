/**
 * Created by n_buga on 31.03.16.
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FileInfo {
    public final int SIZE_OF_FILE_PIECE = (int) 1e5;

    private int id;
    private String name;
    private int size;
    private int countOfPieces = 0;
    private Set<Client> clients = new HashSet<>();

    FileInfo(String name, int size, int id) {
        this.name = name;
        this.size = size;
        this.id = id;
        countOfPieces = (int) Math.ceil(size / SIZE_OF_FILE_PIECE);
    }

    public void addClient(Client client) {
        clients.add(client);
    }

    public int getCountOfPieces() {
        return countOfPieces;
    }

    public int getID() {
        return id;
    }

    public Set<Client> getClients() {
        return clients;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }
}
