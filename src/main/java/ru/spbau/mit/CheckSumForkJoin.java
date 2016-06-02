package ru.spbau.mit;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

public final class CheckSumForkJoin {
    private CheckSumForkJoin() {
    }

    public static String getSumForkJoin(String path) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        return forkJoinPool.invoke(new FindSumTask(path));
    }

    private static final class FindSumTask extends RecursiveTask<String> {
        private String path;

        private FindSumTask(String path) {
            this.path = path;
        }

        @Override
        protected String compute() {
            File file = new File(path);

            if (file.isDirectory()) {
                String result = file.getName();
                String[] paths = file.list();
                List<FindSumTask> tasks = new ArrayList<>();
                for (String p : paths) {
                    FindSumTask task = new FindSumTask(path + "/" + p);
                    tasks.add(task);
                    task.fork();
                }

                for (FindSumTask task : tasks) {
                    result += task.join();
                }
                return md5Hex(result);
            } else {
                if (!file.isFile()) {
                    return "";
                }
                try (FileInputStream fis = new FileInputStream(file)) {
                    return md5Hex(fis);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            }
        }
    }
}
