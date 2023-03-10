package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {
    static final int PORT = 5021;

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

        while(true){
            Socket socket = serverSocket.accept();
            InputStream in = socket.getInputStream();
            OutputStream pw = socket.getOutputStream();
            new Connection(socket, in, pw).start();
        }
    }
}

