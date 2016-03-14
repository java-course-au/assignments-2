import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

/**
 * Created by n_buga on 13.03.16.
 */
public class ClientMain {
    public static void out1() {
        System.out.println("Use following format:");
        System.out.println("<host> <port>");
    }
    public static void out2() {
        System.out.println("Use following formats:");
        System.out.println("get <path>");
        System.out.println("get <path> <output_file>");
        System.out.println("list <path>");
    }
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Not enough arguments");
            out1();
        } else {
            String host = args[0];
            String stringPort = args[1];
            int port;
            try {
                port = Integer.parseInt(stringPort);
            } catch (NumberFormatException e) {
                System.out.println("Wrong port format");
                out1();
                return;
            }
            Client client = new Client(host, port);
            Scanner in = new Scanner(System.in);
            out2();
            while (true) {
                String type = in.next();
                if (type.equals("exit")) {
                    break;
                }
                if (type.equals("get")) {
                    String path = in.next();
                    client.get(path, System.out);
                } else if (type.equals("list")) {
                    String path = in.next();
                    ArrayList<FTPfile> files = client.list(path);
                    for (FTPfile file: files) {
                        System.out.print(file.name);
                        System.out.print(" ");
                        System.out.println(file.isDirectory);
                    }
                } else {
                    System.out.println("Wrong command format");
                }
            }
        }
    }
}
