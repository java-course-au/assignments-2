package ru.spbau.mit;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.RecursiveTask;

public class DirCheckSum {

    public interface CheckSum<T> {

        int BUFFER_SIZE = 4096;

        String checkSum(Path dirname) throws NoSuchAlgorithmException, FileNotFoundException;

        static String toMD5(String path) throws IOException {
            FileInputStream fis = new FileInputStream(new File("foo"));
            String md5 = DigestUtils.md5Hex(fis);
            fis.close();
            return md5;
        }
    }

    public class CheckSumOneThread implements CheckSum {

        @Override
        public String checkSum(Path path) throws NoSuchAlgorithmException, FileNotFoundException {
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            if(Files.isDirectory(path)) {
                md5.update(path.toString().getBytes());
                for(File fileEntry: path.toFile().listFiles()) {
                    md5.update(checkSum(fileEntry.toPath()).getBytes());
                }
            } else {
                DigestInputStream in = new DigestInputStream(
                        new FileInputStream(path.toFile()),
                        md5);
                in.on(true);
                byte[] tmp = new byte[BUFFER_SIZE];
                try {
                    while(true) {
                        in.read(tmp, 0, BUFFER_SIZE);
                    }
                } catch (IOException e) {}
            }

            return new String(md5.digest());
        }
    }

//    public class CheckSumForkJoin<T> implements CheckSum {
//
//        @Override
//        public String checkSum(String dirname) {
//            return null;
//        }
//    }
}

/*
Есть объект. У него есть метод accept. Он спит, пока кто-то не подключится. При подключении выплёвывает сокет,
который обрабатывает в другом потоке.

Плохой синтаксис, но всё же
while(s = r.accept != null) {
    handle(s);//обычно в другом потоке
}


формат запроса
[4 байта: 0001]
DataInput\OutputStreams -- из человекопонятных ворматов в байтовые

 */