import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by n_buga on 10.04.16.
 */
public class ClientFileInfo {
    public static final int SIZE_OF_FILE_PIECE = (int) 1e5;

    private Set<Integer> partsOfFile;
    private Path filePath;
    private int id;
    private long size;
    private int countOfPieces = 0;

    public ClientFileInfo(long size, Path filePath) {
        this.size = size;
        this.filePath = filePath;
        partsOfFile = new HashSet<>();
        countOfPieces =  (int) ((size - 1) / SIZE_OF_FILE_PIECE) + 1;
    }

    public void addAllParts() {
        for (int i = 1; i <=  ((size - 1) / SIZE_OF_FILE_PIECE + 1); i++) {
            partsOfFile.add(i);
        }
    }

    public boolean addAvailablePart(int part) {
        if (part > countOfPieces) {
            return false;
        }
        partsOfFile.add(part);
        return true;
    }

    public int getCountOfPieces() {
        return countOfPieces;
    }

    public Set<Integer> getPartsOfFile() {
        return partsOfFile;
    }

    public RandomAccessFile getFile() {
        try {
            return new RandomAccessFile(filePath.toString(), "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public long getSize() {
        return size;
    }
}
