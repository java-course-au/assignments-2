import org.apache.commons.io.input.BoundedInputStream;

import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

/**
 * Created by n_buga on 13.03.16.
 */
public abstract class ClientMain {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Not enough arguments");
            outCommandLine();
            return;
        }
        String host = args[0];
        String stringPort = args[1];
        Integer port = tryParseOrNull(stringPort);
        if (port == null) {
            System.out.println("Wrong port format");
            outCommandLine();
            return;
        }
        Client client = new Client(host, port);
        Scanner in = new Scanner(System.in);
        outCommandFormat();
        while (true) {
            String type = in.next();
            if (type.equals("exit")) {
                break;
            }
            if (type.equals("get")) {
                String path = in.next();
                BoundedInputStream result = client.get(path);
                System.out.println(result.available());
                while (result.available() > 0) {
                    try {
                        System.out.print(result.read());
                    } catch (Exception e) {
                        break;
                    }
                }
                break;
            } else if (type.equals("list")) {
                String path = in.next();
                ArrayList<FTPfile> files = client.list(path);
                for (FTPfile file : files) {
                    System.out.print(file.getName());
                    System.out.print(" ");
                    System.out.println(file.isDirectory());
                }
                break;
            } else {
                System.out.println("Wrong command format");
                break;
            }
        }
    }

    private static void outCommandLine() {
        System.out.println("Use following format:");
        System.out.println("<host> <port>");
    }

    private static void outCommandFormat() {
        System.out.println("Use following formats:");
        System.out.println("get <path>");
        System.out.println("get <path> <output_file>");
        System.out.println("list <path>");
    }

    private static Integer tryParseOrNull(String string) {
        Integer result;
        try {
            result = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return null;
        }
        return result;
    }
}
