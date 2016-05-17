import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by n_buga on 01.03.16.
 */
public final class CheckSumExecutor {
    private CheckSumExecutor() {

    }

    public static void main(String[] args) throws Exception {
        String path1;
        String path2 = null;
        switch (args.length) {
            case 0:
                path1 = "." + File.separator;
                path2 = path1;
                break;
            case 1:
                path1 = args[0];
                path2 = "." + File.separator;
                break;
            case 2:
                path1 = args[0];
                path2 = args[1];
                break;
            default:
                return;
        }
        if (!checkValid(path1, path2)) {
            System.out.println("Not valid");
        }
        timeAllExecute(path1);
    }

    public static int timeAllExecute(String path1) throws IOException, NoSuchAlgorithmException {
        File file = new File(path1);
        long beginSingle =  System.currentTimeMillis();
        DifferentRealization.SingleThreadCheckSum.execute(file);
        long endSingle = System.currentTimeMillis();
        long beginExecutor = System.currentTimeMillis();
        DifferentRealization.ExecutorServiceCheckSum.doExecute(file);
        long endExecutor = System.currentTimeMillis();
        long beginForkJoin = System.currentTimeMillis();
        DifferentRealization.ForkJoinCheckSum.execute(file);
        long endForkJoin = System.currentTimeMillis();
        System.out.printf("Single Thread: %d\nExecutorService: %d\nFork Join Pool: %d\n",
                endSingle - beginSingle, endExecutor - beginExecutor, endForkJoin - beginForkJoin);
        return 0;
    }

    public static boolean checkValid(String path1, String path2) throws IOException, NoSuchAlgorithmException {
        File file1 = new File(path1);
        String singleThreadCheckSumFirst = DifferentRealization.SingleThreadCheckSum.execute(file1);
        String executorServiceCheckSumFirst = DifferentRealization.ExecutorServiceCheckSum.doExecute(file1);
        String forkJoinCheckSumFirst = DifferentRealization.ForkJoinCheckSum.execute(file1);
        if (path2 != null) {
            File file2 = new File(path2);
            String singleThreadCheckSumSecond = DifferentRealization.SingleThreadCheckSum.execute(file2);
            String executorServiceCheckSumSecond = DifferentRealization.ExecutorServiceCheckSum.doExecute(file2);
            String forkJoinCheckSumSecond = DifferentRealization.ForkJoinCheckSum.execute(file2);
            if (!path2.equals(path1)) {
                if (singleThreadCheckSumFirst.equals(singleThreadCheckSumSecond)) {
                    System.out.println("Wrong realization single thread executor");
                    return false;
                }
                if (executorServiceCheckSumFirst.equals(executorServiceCheckSumSecond)) {
                    System.out.println("Wrong realization executor service");
                    return false;
                }
                if (forkJoinCheckSumFirst.equals(forkJoinCheckSumSecond)) {
                    System.out.println("Wrong realization fork join pool");
                    return false;
                }
            } else {
                if (!singleThreadCheckSumFirst.equals(singleThreadCheckSumSecond)) {
                    System.out.println("Wrong realization executor service");
                    return false;
                }
                if (!executorServiceCheckSumFirst.equals(executorServiceCheckSumSecond)) {
                    System.out.println("Wrong realization executor service");
                    return false;
                }
                if (!forkJoinCheckSumFirst.equals(forkJoinCheckSumSecond)) {
                    System.out.println("Wrong realization fork join pool");
                    return false;
                }
            }
        }
        return true;
    }
}
