package ru.spbau.mit.simpleftp.common;

import java.util.LinkedList;
import java.util.List;

public class MyFtpListResponse implements MyFtpResponse {
    public static class Entry {
        private String fileName;
        private boolean isDir;

        public Entry(String path, boolean isDir) {
            super();
            this.fileName = path;
            this.isDir = isDir;
        }

        public String getFileName() {
            return fileName;
        }

        public boolean isDir() {
            return isDir;
        }
    }

    private List<Entry> entryContents = new LinkedList<>();

    public void addEntry(Entry entry) {
        entryContents.add(entry);
    }

    public void setFailed() {
        entryContents = new LinkedList<>();
    }

    public boolean isFailed() {
        return entryContents.isEmpty();
    }

    public List<Entry> getContents() {
        return entryContents;
    }
}
