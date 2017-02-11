package ru.spbau.mit;

import com.google.common.base.Stopwatch;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public final class Main {
    private Main() {}
    private static final String ONE_THREAD_OPTION = "one_thread";
    private static final String EXECUTOR_SERVICE_OPTION = "executor";
    private static final String FORK_JOIN_OPTION = "fork_join";
    private static final String HELP = String.format("<JAR> <%s|%s|%s> <path>", ONE_THREAD_OPTION,
            EXECUTOR_SERVICE_OPTION, FORK_JOIN_OPTION);

    public static void help() {
        System.out.println(HELP);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            help();
            return;
        }
        DirHasher dirHasher = null;
        switch (args[0]) {
            case ONE_THREAD_OPTION:
                dirHasher = new DirHasherOneThread();
                break;
            case EXECUTOR_SERVICE_OPTION:
                dirHasher = new DirHasherExecutorService();
                break;
            case FORK_JOIN_OPTION:
                dirHasher = new DirHasherForkJoin();
                break;
            default:
        }
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();

            System.out.println("RESULT : " + dirHasher.calculateMd5(Paths.get(args[1])));
            System.out.println("ELAPSED " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " MILSEC");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
