package ru.spbau.mit;

import javafx.scene.shape.Path;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.RecursiveTask;

/**
 * Created by liza on 01.03.16.
 */
public class DirCheckSum {

    public interface CheckSum<T> {

        String checkSum(String dirname);

        static String toMD5(String path) throws IOException {
            FileInputStream fis = new FileInputStream(new File("foo"));
            String md5 = DigestUtils.md5Hex(fis);
            fis.close();
            return md5;
        }
    }

    public class CheckSumOneThread implements CheckSum {

        @Override
        public String checkSum(String path) {
            if(Files.isDirectory())
        }
    }

    public class CheckSumForkJoin<T> implements CheckSum {

        @Override
        public String checkSum(String dirname) {
            return null;
        }
    }
}

/*
Есть объект. У него есть метод accept. Он спит, пока кто-то не подключится. При подключении выпл1вывает сокет, который обрабатывает в другом потоке

Плохой синтаксис, но всё же
while(s = r.accept != null) {
    handle(s);//обычно в другом потоке
}


формат запроса
[4 байта: 0001]
DataInput\OutputStreams -- из человекопонятных ворматов в байтовые

 */