/**
 * Created by n_buga on 13.03.16.
 */
public abstract class ServerMain {

    public static void out() {
        System.out.println("Use following format:");
        System.out.println("<port>:int");
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Wrong format");
        } else {
            String stringPort = args[0];
            int port;
            try {
                port = Integer.parseInt(stringPort);
            } catch (NumberFormatException e) {
                System.out.println("Wrong port format");
                out();
                return;
            }
            Server server = new Server(port);
            server.start();
            System.out.println("Accept!");
        }
    }
}
