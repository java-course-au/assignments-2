package ru.spbau.mit;

public class FileInfo {
    private final String name;
    private final boolean isDir;

    public FileInfo(String name, boolean isDir) {
        this.name = name;
        this.isDir = isDir;
    }

    public String getName() {
        return name;
    }

    public boolean getIsDir() {
        return isDir;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object.getClass() == getClass()
                && ((FileInfo) object).name.equals(name) && ((FileInfo) object).getIsDir() == isDir;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + Boolean.hashCode(isDir);
    }
}
