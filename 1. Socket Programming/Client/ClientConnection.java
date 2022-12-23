package Client;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class ClientConnection extends Thread{
    private String filename;
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    static final String FILE_PATH = "src/Client/dir/";
    private String[] extensions = {".txt", ".pdf", ".jpg", ".png", ".bmp", ".mp4"};

    public ClientConnection(Socket socket, String filename) throws Exception{
        this.filename = filename;
        this.socket = socket;
        this.is = socket.getInputStream();
        this.os = socket.getOutputStream();
    }

    public boolean checkIfFileHasExtension(String s, String[] extensions) {
        return Arrays.stream(extensions).anyMatch(entry -> s.endsWith(entry));
    }

    public void closeConnection(){
        try{
            this.socket.close();
        }catch(Exception e){
            System.out.println(e);
        }
    }

    @Override
    public void run(){
        try{
            boolean found = false;
            File dir = new File(FILE_PATH);
            File reqd = new File(FILE_PATH, this.filename);

            File[] files = dir.listFiles((file, s) -> checkIfFileHasExtension(s, extensions));

            for(File f : files){
                if(f.equals(reqd)){
                    found = true;
                    System.out.println(f.getPath());
                }
            }

            if(!found) System.out.println("File not found... ...");

        }catch(Exception e){
            closeConnection();
            System.out.println(e);
        }
    }
}
