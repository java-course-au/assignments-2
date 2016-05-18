import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by n_buga on 15.05.16.
 */
public final class ClientMain {
    private ClientMain() {}

    public static void main(String[] args) {
        if (args.length == 0) {
            outFormat();
            return;
        }
        String fileName = args[0];
        Path filePath = Paths.get(fileName);
        String ip = "localhost";
        if (args.length > 1) {
            ip = args[0];
        }
        NonBlockingClient nonBlockingClient = new NonBlockingClient(ip, filePath);
        nonBlockingClient.request();
    }

    private static void outFormat() {
        System.out.printf("use <fileName> <localhost>\n");
    }
}
