package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {
    static final int PORT = 5021;
    static final String rootPath = "root/";

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

        while(true){
            Socket socket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            new Connection(socket, in, pw, rootPath).start();
        }
    }
}
