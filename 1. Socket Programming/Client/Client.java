package Client;

import java.net.Socket;
import java.util.Scanner;

public class Client {
    static final int PORT = 5021;

    public static void main(String[] args) throws Exception {
        while(true){
            Socket socket = new Socket("127.0.0.1", PORT);
            System.out.println("Enter filename:");
            Scanner scanner = new Scanner(System.in);
            String in = scanner.nextLine();
            new ClientConnection(socket, in).start();
        }
    }
}
