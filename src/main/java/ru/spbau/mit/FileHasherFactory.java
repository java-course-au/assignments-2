package ru.spbau.mit;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by ldvsoft on 01.03.16.
 */
public abstract class FileHasherFactory {
    private static String getFileHash(Path path) {
        try {
            InputStream fis = Files.newInputStream(path);
            String md5 = DigestUtils.md5Hex(fis);
            fis.close();
            return md5;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static FileHasher getSingleThreadFileHasher() {
        return new SingleThreadFileHasher();
    }

    public static FileHasher getThreadPoolFileHasher() {
        return new ThreadPoolFileHasher();
    }

    public static FileHasher getForkJoinFileHasher() {
        return new ForkJoinFileHasher();
    }

    private static class SingleThreadFileHasher implements FileHasher {
        @Override
        public String getDigest(Path path) {
            if (Files.isRegularFile(path)) {
                return getFileHash(path);
            }
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(path.toString().getBytes());
                Files.list(path)
                        .sorted()
                        .forEach(subPath -> {
                            md.update(getDigest(subPath).getBytes());
                        });
                return String.valueOf(Hex.encodeHex(md.digest()));
            } catch (NoSuchAlgorithmException ignored) {
                //MD5 is guaranteed to exist
                return "";
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    private static class ThreadPoolFileHasher implements FileHasher {
        private ExecutorService executorService = Executors.newCachedThreadPool();

        @Override
        public String getDigest(Path path) {
            ExecutorService executorService = Executors.newCachedThreadPool();
            String result = doGetDigest(path, executorService);
            executorService.shutdownNow();
            return result;
        }

        public String doGetDigest(Path path, ExecutorService executorService) {
            if (Files.isRegularFile(path)) {
                return getFileHash(path);
            }
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(path.toString().getBytes());
                Files.list(path)
                        .sorted()
                        .map(subPath -> (Callable<byte[]>) (() -> getDigest(subPath).getBytes()))
                        .map(executorService::submit)
                        .collect(Collectors.toList()).stream()
                        .forEach(future -> {
                            try {
                                md.update(future.get());
                            } catch (InterruptedException | ExecutionException ignored) {
                            }
                        });
                return String.valueOf(Hex.encodeHex(md.digest()));
            } catch (NoSuchAlgorithmException ignored) {
                //MD5 is guaranteed to exist
                return "";
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    private static class ForkJoinFileHasher implements FileHasher {
        final class Task extends RecursiveTask<String> {
            private Path path;

            private Task(Path path) {
                this.path = path;
            }

            @Override
            protected String compute() {
                if (Files.isRegularFile(path)) {
                    return getFileHash(path);
                }
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    md.update(path.toString().getBytes());
                    Files.list(path)
                            .sorted()
                            .map(Task::new)
                            .peek(ForkJoinTask::fork)
                            .collect(Collectors.toList()).stream()
                            .map(ForkJoinTask::join)
                            .forEach(s -> md.update(s.getBytes()));
                    return String.valueOf(Hex.encodeHex(md.digest()));
                } catch (NoSuchAlgorithmException ignored) {
                    //MD5 is guaranteed to exist
                    return "";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "";
                }
            }
        }

        @Override
        public String getDigest(Path path) {
            return new ForkJoinPool().invoke(new Task(path));
        }
    }
}
