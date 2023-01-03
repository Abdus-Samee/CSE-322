package Client;

import java.net.Socket;
import java.util.Scanner;

public class Client {
    static final int PORT = 5021;

    public static void main(String[] args) throws Exception {
        System.out.println("Enter filenames separated by spaces:");
        Scanner scanner = new Scanner(System.in);
        String[] in = scanner.nextLine().split(" ");
        for(String s : in){
            Socket socket = new Socket("127.0.0.1", PORT);
            new ClientConnection(socket, s).start();
        }
    }
}
