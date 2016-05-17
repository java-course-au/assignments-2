import javafx.concurrent.Task;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.omg.CORBA.TIMEOUT;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * Created by n_buga on 01.03.16.
 */
public class DifferentRealization {
    public static class ExecutorServiceCheckSum{
        private static ExecutorService executorService;;

        public static String doExecute(File file) throws IOException, NoSuchAlgorithmException {
            executorService = Executors.newCachedThreadPool();
            String result = execute(file);
            executorService.shutdown();
            return result;
        }

        public static String execute(File file) throws IOException, NoSuchAlgorithmException {
            String result = "";
            if (file.isDirectory()) {
                result += executorServiceCheckSumDirectory(file);
            } else if (file.isFile()) {
                result += executorServiceCheckSumFile(file);
            } else {
                return result;
            }
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] answer = digest.digest(result.getBytes());
            return Hex.encodeHexString(answer);
        }

        private static String executorServiceCheckSumFile(File file) throws IOException, NoSuchAlgorithmException {
            System.out.printf("Check file name %s\n", file.toString());
            FileInputStream fileInputStream = new FileInputStream(file);
            DigestUtils digestUtils = new DigestUtils();
            String answer = digestUtils.md5Hex(fileInputStream);
            return answer;
        }

        private static String executorServiceCheckSumDirectory(File file) throws IOException, NoSuchAlgorithmException {
            System.out.printf("Check directory name %s\n", file.toString());
            String result = file.getName();
            for (File subFile : file.listFiles()) {
                Future futureResult;
                if (subFile.isFile() || subFile.isDirectory()) {
                    futureResult = executorService.submit(() -> execute(subFile));
                } else {
                    continue;
                }
                try {
                    result += (String) futureResult.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    System.out.println("Can't execute");
                    return "";
                }
            }
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] answer = digest.digest(result.getBytes());
            return Hex.encodeHexString(answer);
        }
    }

    public static class SingleThreadCheckSum{

        public static String execute(File file) throws IOException, NoSuchAlgorithmException {
            if (file.isDirectory()) {
                return singleThreadCheckSumDirectory(file);
            }
            if (file.isFile()) {
                return singleThreadCheckSumFile(file);
            }
            return "";
        }

        private static String singleThreadCheckSumFile(File file) throws IOException, NoSuchAlgorithmException {
            System.out.printf("Check file name %s\n", file.toString());
            FileInputStream fileInputStream = new FileInputStream(file);
            DigestUtils digestUtils = new DigestUtils();
            String answer = digestUtils.md5Hex(fileInputStream);
            return answer;
        }

        private static String singleThreadCheckSumDirectory(File file) throws IOException, NoSuchAlgorithmException {
            System.out.printf("Check directory name %s\n", file.toString());
            String result = file.getName();
            for (File subFile: file.listFiles()) {
                if (subFile.isFile()) {
                    result += singleThreadCheckSumFile(subFile);
                }
                else if (subFile.isDirectory()) {
                    result += singleThreadCheckSumDirectory(subFile);
                }
            }
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] answer = digest.digest(result.getBytes());
            return Hex.encodeHexString(answer);
        }
    }

    public static class ForkJoinCheckSum {
        private static ForkJoinPool forkJoinPool = new ForkJoinPool();

        private static class CheckSumTask extends RecursiveTask<String> {
            private File file;

            public CheckSumTask(File file) {
                this.file = file;
            }

            @Override
            protected String compute() {
                if (file.isFile()) {
                    try {
                        return SingleThreadCheckSum.execute(file);
                    } catch (IOException | NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                } else if (file.isDirectory()) {
                    ArrayList<CheckSumTask> taskList = new ArrayList<>();
                    for (File subFile: file.listFiles()) {
                        CheckSumTask curTask = new CheckSumTask(subFile);
                        curTask.fork();
                        taskList.add(curTask);
                    }
                    String result = "";
                    for (CheckSumTask curTask: taskList) {
                        result += curTask.join();
                    }
                    return result;
                }
                return "";
            }
        }

        public static String execute(File file) {
            CheckSumTask task = new CheckSumTask(file);
            return forkJoinPool.invoke(task);
        }
    }
}
