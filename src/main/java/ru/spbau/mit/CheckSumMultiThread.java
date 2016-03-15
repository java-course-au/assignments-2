package ru.spbau.mit;

import java.io.File;
import java.io.FileInputStream;

public class CheckSumMultiThread {
    public static void main(String[] args) {
        String result = getSumMultiThread(args[0]);
        System.out.println(result);
    }

    public static String getSumMultiThread(String path) {
        File file = new File(path);

        String result = "";
        if (file.isDirectory()) {
            result = file.getName();
            String[] paths = file.list();
            for (String p : paths) {
                result += getSumMultiThread(p);
            }
            result = org.apache.commons.codec.digest.DigestUtils.md5Hex(result);
        } else {
            try (FileInputStream fis = new FileInputStream(new File("foo"))) {
                result = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
