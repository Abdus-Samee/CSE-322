package Client;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ClientConnection extends Thread{
    private String filename;
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    static final String FILE_PATH = "src/Client/dir/";
    static final int CHUNK_SIZE = 50;
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

    public void uploadFile(File file) throws Exception{
        FileInputStream fis = new FileInputStream(file);
        String type = "";
        if(file.getName().toLowerCase().endsWith(".pdf")) type = "application/pdf";
        else if(file.getName().toLowerCase().endsWith(".docx")) type = "application/msword";

        String response = "UPLOAD " + this.filename + "\r\n" +
                          "Content-Type: " + type + "\r\n" +
                          "Content-Disposition: attachment; filename=" + file.getName() + "\r\n" +
                          "Content-Length: " + fis.available() + "\r\n\r\n";

        os.write(response.getBytes());

        int count;
        byte[] buffer = new byte[CHUNK_SIZE];
        while ((count = fis.read(buffer)) > 0) os.write(buffer, 0, count);

        os.flush();
        fis.close();
        System.out.println("uploaded");
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
                }
            }

            if(!found) System.out.println("File not found... ...");

            uploadFile(reqd);
        }catch(Exception e){
            closeConnection();
            System.out.println(e);
        }
    }
}
