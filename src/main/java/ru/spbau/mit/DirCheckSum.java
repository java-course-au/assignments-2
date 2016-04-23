package ru.spbau.mit;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class DirCheckSum {

    private static int BUFFER_SIZE = 4096;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final ForkJoinPool fjp = new ForkJoinPool();


//    static String toMD5(String path) throws IOException {
//        FileInputStream fis = new FileInputStream(new File("foo"));
//        String md5 = DigestUtils.md5Hex(fis);
//        fis.close();
//        return md5;
//    }

    public byte[] checkSumOneThread(Path path) throws NoSuchAlgorithmException, IOException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        if(Files.isDirectory(path)) {
            md5.update(path.toString().getBytes());
            Files.list(path).sorted().forEach(fileEntry -> {
                try {
                    md5.update(checkSumOneThread(fileEntry));
                } catch (IOException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
//            for(File fileEntry: Files.list(path).sorted().toArray()) { //path.toFile().listFiles()) {
//                md5.update(checkSumOneThread(fileEntry.toPath()));
//            }
        } else {
            DigestInputStream in = new DigestInputStream(
                    new FileInputStream(path.toFile()),
                    md5);
            in.on(true);
            byte[] tmp = new byte[BUFFER_SIZE];
            try {
                while(in.read(tmp, 0, BUFFER_SIZE) >= BUFFER_SIZE) {}
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        return md5.digest();
    }

    public byte[] checkSumThreadPool(Path path) throws NoSuchAlgorithmException, ExecutionException, InterruptedException, IOException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
//        ArrayList<Future<?>> subtasks = new ArrayList<>();

        if(Files.isDirectory(path)) {
            md5.update(path.toString().getBytes());
            //for(File fileEntry: (File[]) Files.list(path).sorted().toArray()) { //path.toFile().listFiles()) {


            List<Future<byte[]>> subtasks = Files.list(path)
                    .sorted()
                    .map(fileEntry -> {
                return threadPool.submit(() -> {
                    try {
                        return checkSumThreadPool(fileEntry);
                    } catch (FileNotFoundException |
                            InterruptedException |
                            ExecutionException |
                            NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });
//                subtasks.add(task);
            }).collect(Collectors.toList());
//
//                    .forEach(task -> {
//                byte[] res = new byte[0];
//                try {
//                    res = task.get();
//                } catch (InterruptedException | ExecutionException e) {
//                    e.printStackTrace();
//                    throw new RuntimeException(e);
//                }
//                md5.update(res);
//            });
            for(Future<byte[]> task: subtasks) {
                byte[] res = task.get();
                md5.update(res);
            }
        } else {
            DigestInputStream in = new DigestInputStream(
                    new FileInputStream(path.toFile()),
                    md5);
            in.on(true);
            byte[] tmp = new byte[BUFFER_SIZE];
            try {
                while(in.read(tmp, 0, BUFFER_SIZE) >= BUFFER_SIZE) {}
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        return md5.digest();
    }

    public byte[] checkSumForkJoin(Path path) throws
            NoSuchAlgorithmException,
            ExecutionException,
            InterruptedException,
            IOException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
//        ArrayList<Future<?>> subtasks = new ArrayList<>();

        if(Files.isDirectory(path)) {
            md5.update(path.toString().getBytes());
            List<ForkJoinTask<byte[]>> subtasks = Files.list(path)
                    .sorted()
                    .map(fileEntry -> {
                        return fjp.submit(() -> {
                            try {
                                return checkSumForkJoin(fileEntry);
                            } catch (FileNotFoundException |
                                    InterruptedException |
                                    ExecutionException |
                                    NoSuchAlgorithmException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        });
//                subtasks.add(task);
                    }).collect(Collectors.toList());
            for(ForkJoinTask<byte[]> task: subtasks) {
                byte[] res = task.get();
                md5.update(res);
            }


//                    .forEach(task -> {
//                byte[] res = new byte[0];
//                try {
//                    res = task.get();
//                } catch (InterruptedException | ExecutionException e) {
//                    e.printStackTrace();
//                    throw new RuntimeException(e);
//                }
//                md5.update(res);
//            });

//            for(File fileEntry: (File[]) Files.list(path).sorted().toArray()) {//path.toFile().listFiles()) {
//                ForkJoinTask<byte[]> task = fjp.submit(() -> {
//                    try {
//                        return checkSumForkJoin(fileEntry.toPath());
//                    } catch (FileNotFoundException |
//                            InterruptedException |
//                            ExecutionException |
//                            NoSuchAlgorithmException e) {
//                        e.printStackTrace();
//                        throw new RuntimeException(e);
//                    }
//                });
//                subtasks.add(task);
//            }
        } else {
            DigestInputStream in = new DigestInputStream(
                    new FileInputStream(path.toFile()),
                    md5);
            in.on(true);
            byte[] tmp = new byte[BUFFER_SIZE];
            try {
                while(in.read(tmp, 0, BUFFER_SIZE) >= BUFFER_SIZE) {}
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        return md5.digest();
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