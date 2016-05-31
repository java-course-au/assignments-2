package ru.spbau.mit;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

public final class MD5Sum {
    private static ExecutorService service;

    private MD5Sum(){}

    private static String getFileMD5Sum(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        String md5 = DigestUtils.md5Hex(fis);
        fis.close();
        return md5;
    }

    private static String getDirMD5Sum(File file) {
        if (file.isFile()) {
            try {
                return getFileMD5Sum(file);
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        } else if (file.isDirectory()) {
            assert file.isDirectory();

            String preDirMD5sum = null;
            try {
                preDirMD5sum = file.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
            File[] files = file.listFiles();

            assert files != null;

            for (File currentFile : files) {
                preDirMD5sum += getDirMD5Sum(currentFile);
            }

            return DigestUtils.md5Hex(preDirMD5sum);
        }
        return "";
    }

    public static String countMD5SumInOneThread(File file) {
        return getDirMD5Sum(file);
    }

    public static String countMD5SumInExecutorService(File file) {
        service = Executors.newCachedThreadPool();

        Future<String> res = service.submit(() -> getDirMD5SumExecuteService(file));
        try {
            return res.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            service.shutdown();
        }
        return "";
    }

    private static String getDirMD5SumExecuteService(File file) {
        if (file.isFile()) {
            try {
                return getFileMD5Sum(file);
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }
        if (!file.isDirectory()) {
            return "";
        }

        File[] files = file.listFiles();
        assert files != null;

        String preDirMD5sum;
        try {
            preDirMD5sum = file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        ArrayList<Future<String>> md5sums = new ArrayList<>();
        for (File currentFile : files) {
            if (currentFile.isDirectory() || currentFile.isFile()) {
                md5sums.add(service.submit(() -> getDirMD5SumExecuteService(currentFile)));
            }
        }

        for (Future<String> md5sum : md5sums) {
            try {
                preDirMD5sum += md5sum.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return DigestUtils.md5Hex(preDirMD5sum);
    }

    public static String countMD5SumInForkJoin(File file) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        return forkJoinPool.invoke(new MD5SumTask(file));
    }

    private static final class MD5SumTask extends RecursiveTask<String> {
        private final File file;

        private MD5SumTask(File file) {
            this.file = file;
        }

        @Override
        protected String compute() {
            if (file.isFile()) {
                try {
                    return getFileMD5Sum(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    return "";
                }
            }

            if (!file.isDirectory()) {
                return "";
            }

            File[] files = file.listFiles();
            assert files != null;

            String preDirMD5sum;
            try {
                preDirMD5sum = file.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }

            ArrayList<MD5SumTask> tasks = new ArrayList<>();

            for (File currentFile : files) {
                tasks.add(new MD5SumTask(currentFile));
                tasks.get(tasks.size() - 1).fork();
            }


            for (MD5SumTask task : tasks) {
                preDirMD5sum += task.join();
            }

            return DigestUtils.md5Hex(preDirMD5sum);
        }
    }
}
