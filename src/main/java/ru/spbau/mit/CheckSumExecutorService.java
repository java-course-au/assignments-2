package ru.spbau.mit;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

public final class CheckSumExecutorService {
    private static ExecutorService executorService;

    private CheckSumExecutorService() {
    }

    public static void main(String[] args) {
        String result = getSumExecutorService(args[0]);
        System.out.println(result);
    }

    public static String getSumExecutorService(String path) {
        executorService = Executors.newCachedThreadPool();
        String result = getSum(path);
        executorService.shutdown();
        return result;
    }

    private static String getSum(String path) {
        File file = new File(path);

        if (file.isDirectory()) {
            String result = file.getName();
            String[] paths = file.list();
            List<Future<String>> sums = new ArrayList<>();
            for (String p : paths) {
                sums.add(executorService.submit(() -> getSum(path + "/" + p)));
            }

            for (Future<String> future : sums) {
                try {
                    result += future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
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
