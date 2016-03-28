package ru.spbau.mit;

public final class DirectoryItem {
    public final String name;
    public final boolean isDirectory;

    DirectoryItem(String name, boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DirectoryItem)) {
            return false;
        }
        DirectoryItem other = (DirectoryItem) obj;
        return name.equals(other.name) && isDirectory == other.isDirectory;
    }

    @Override
    public int hashCode() {
        // CHECKSTYLE.OFF: MagicNumber
        return name.hashCode() * 239017 + Boolean.hashCode(isDirectory);
        // CHECKSTYLE.ON: MagicNumber
    }
}
