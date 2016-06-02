package ru.spbau.mit;

import java.io.File;
import java.io.FileInputStream;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

public final class CheckSumSingleThread {
    private CheckSumSingleThread() {
    }

    public static void main(String[] args) {
        String result = getSumSingleThread(args[0]);
        System.out.println(result);
    }

    public static String getSumSingleThread(String path) {
        File file = new File(path);

        if (file.isDirectory()) {
            String result = file.getName();
            String[] paths = file.list();
            for (String p : paths) {
                result += getSumSingleThread(path + "/" + p);
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
