/**
 * Created by n_buga on 13.03.16.
 */
public class FTPfile {
    private String name;
    private boolean isDirectory;

    public FTPfile(String name, boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }
}
