package ru.spbau.mit;


import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Сева on 01.03.2016.
 */
public final class DirChecksum {
    private DirChecksum(){
    }

    private static String getFileHash(File file) {
        try (FileInputStream stream = new FileInputStream(file)) {
            return DigestUtils.md5Hex(stream);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getOneThreadChecksum(String dirPath) {
        File root = new File(dirPath);
        if (root.isFile()) {
            return getFileHash(root);
        }

        StringBuilder result = new StringBuilder(root.getName());
        for (File file : root.listFiles()) {
            result.append(getOneThreadChecksum(file.getAbsolutePath()));
        }

        return DigestUtils.md5Hex(result.toString());
    }

    private static class MultiThreadDirectoryProcessor implements Callable<String> {
        private final String path;

        MultiThreadDirectoryProcessor(String path) {
            this.path = path;
        }

        @Override
        public String call() throws Exception {
            File root = new File(path);
            if (root.isFile()) {
                return getFileHash(root);
            }

            ExecutorService service = Executors.newFixedThreadPool(2);

            List<Future> futureList = new ArrayList<>();
            for (File file : root.listFiles()) {
                futureList.add(service.submit(new MultiThreadDirectoryProcessor(file.getAbsolutePath())));
            }

            StringBuilder result = new StringBuilder(root.getName());
            for (Future task : futureList) {
                result.append(task.get());
            }

            service.shutdownNow();
            return DigestUtils.md5Hex(result.toString());
        }
    }

    public static String getMultiThreadChecksum(String dirPath) {
        try {
            return new MultiThreadDirectoryProcessor(dirPath).call();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static class ForkJoinDirectoryProcessor extends RecursiveTask<String> {
        private final String path;

        ForkJoinDirectoryProcessor(String path) {
            this.path = path;
        }
        @Override
        protected String compute() {
            File root = new File(path);
            if (root.isFile()) {
                return getFileHash(root);
            }

            List<ForkJoinDirectoryProcessor> tasks = new ArrayList<>();
            for (File file : root.listFiles()) {
                ForkJoinDirectoryProcessor task = new ForkJoinDirectoryProcessor(file.getAbsolutePath());
                task.fork();
                tasks.add(task);
            }

            StringBuilder result = new StringBuilder(root.getName());
            for (ForkJoinDirectoryProcessor task : tasks) {
                result.append(task.join());
            }

            return DigestUtils.md5Hex(result.toString());
        }

    }

    public static String getForkJoinChecksum(String dirPath) {
        return new ForkJoinDirectoryProcessor(dirPath).compute();
    }
}

