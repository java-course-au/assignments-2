import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by n_buga on 10.04.16.
 */
public class ClientFileData {
    private Set<Integer> idAvailableFiles;
    private Map<Integer, ClientFileInfo> idFileMap;

    ClientFileData() {
        idAvailableFiles = new HashSet<>();
        idFileMap = new HashMap<>();
    }

    public void addFile(int id, long size, Path filePath) {
        if (!idFileMap.containsKey(id)) {
            idAvailableFiles.add(id);
            idFileMap.put(id, new ClientFileInfo(size, filePath));
        }
    }

    public void addPart(int id, int part) {
        idFileMap.get(id).addAvailablePart(part);
    }

    public void addAllParts(int id) {
        idFileMap.get(id).addAllParts();
    }

    public Set<Integer> getIdAvailableFiles() {
        return idAvailableFiles;
    }

    public Map<Integer, ClientFileInfo> getIdFileMap() {
        return idFileMap;
    }

    public void updateDataFromFile() {
    }
}
