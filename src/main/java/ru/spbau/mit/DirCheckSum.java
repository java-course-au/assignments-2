package ru.spbau.mit;

import javafx.util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DirCheckSum {

    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final ForkJoinPool fjp = new ForkJoinPool();
    private final Logger logger = Logger.getLogger("CHECKER");


    public byte[] checkSumOneThread(Path path) throws NoSuchAlgorithmException, IOException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        String tag = "ONE THREAD";
        logger.info(tag + "\n" + path.toString());

        if(Files.isDirectory(path)) {
            md5.update(path.toString().getBytes());
            List<Path> files = Files.list(path).sorted().collect(Collectors.toList());
            for(Path fileEntry: files) {
//                logger.info("ONE THREAD: fileEntry " + fileEntry.toString());
                md5.update(checkSumOneThread(fileEntry));
            }
        } else {
            countMD5(path, md5);
//            logger.info("ONE THREAD: counted hash of " + path.toString());
        }

        return md5.digest();
    }

    public byte[] checkSumThreadPool(Path path) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        String tag = "THREAD POOL";
        logger.info(tag + "\n" + path.toString());

        if(Files.isDirectory(path)) {
            md5.update(path.toString().getBytes());

            List<Pair<Future<byte[]>, String>> subtasks = Files.list(path)
                    .sorted()
                    .map(fileEntry -> {
                        Future<byte[]> task = threadPool.submit(() -> {
                            try {
//                            logger.info("THREAD POOL: submitting fileEntry " + fileEntry.toString());
                                return checkSumThreadPool(fileEntry);
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        });
                        return new Pair<>(task, fileEntry.toString());
                    }).collect(Collectors.toList());

            for(Pair<Future<byte[]>, String> task: subtasks) {
                byte[] res = task.getKey().get();
                logger.info(tag + ": counting " + Arrays.toString(res) + "\n" + task.getValue());
                md5.update(res);
            }
        } else {
            countMD5(path, md5);
//            logger.info("THREAD POOL: single file " + path.toString() + " " + Arrays.toString(tmp));
        }

        return md5.digest();
    }

    public byte[] checkSumForkJoin(Path path) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        String tag = "FORK JOIN";
        logger.info(tag + "\n" + path.toString());

        if(Files.isDirectory(path)) {
            md5.update(path.toString().getBytes());
            List<Pair<ForkJoinTask<byte[]>, String>> subtasks = Files.list(path)
                    .sorted()
                    .map(fileEntry -> {
                        ForkJoinTask<byte[]> task = fjp.submit(() -> {
                            try {
                                return checkSumForkJoin(fileEntry);
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        });
                        return new Pair<>(task, path.toString());
                    }).collect(Collectors.toList());
            for(Pair<ForkJoinTask<byte[]>, String> task: subtasks) {
                byte[] res = task.getKey().get();
                logger.info(tag + ": counting " + Arrays.toString(res) + "\n" + task.getValue());
                md5.update(res);
            }

        } else {
            countMD5(path, md5);
        }

        return md5.digest();
    }

    private void countMD5(Path path, MessageDigest md5) throws FileNotFoundException {
        DigestInputStream in = new DigestInputStream(
                new FileInputStream(path.toFile()),
                md5);
        in.on(true);
        int BUFFER_SIZE = 4096;
        byte[] tmp = new byte[BUFFER_SIZE];
        try {
            while(in.read(tmp, 0, BUFFER_SIZE) >= BUFFER_SIZE) {}
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
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