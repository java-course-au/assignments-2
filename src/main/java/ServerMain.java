import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Created by n_buga on 14.05.16.
 */
public final class ServerMain {
    private ServerMain() {}

    public static void outFormat() {
        System.out.printf("need the name of file as an argument");
    }
    public static void main(String[] args) {
        if (args.length == 0) {
            outFormat();
            return;
        }
        String fileName = args[0];
        Path filePath = Paths.get(fileName);
        NonBlockingServer nonBlockingServer = new NonBlockingServer(filePath);
        Thread thread = nonBlockingServer.start();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (scanner.hasNext()) {
                String word = scanner.next();
                if (word.equals("end")) {
                    nonBlockingServer.close();
                    break;
                } else {
                    System.out.printf("The command %s doesn't exist. Use end for exit.\n", word);
                }
            }
        }
    }
}
