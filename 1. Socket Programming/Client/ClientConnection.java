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
    private String[] extensions = {"txt", "pdf", "docx", "jpg", "png", "bmp", "mp4", "doc"};

    public ClientConnection(Socket socket, String filename) throws Exception{
        this.filename = filename;
        this.socket = socket;
        this.is = socket.getInputStream();
        this.os = socket.getOutputStream();
    }

    public boolean checkIfFileHasExtension(String s, String[] extensions) {
        return Arrays.stream(extensions).anyMatch(entry -> s.endsWith(entry));
    }

    public void uploadFile(File file, int found) throws Exception{
        FileInputStream fis = null;
        String type = "";
        if(file.getName().toLowerCase().endsWith(".pdf")) type = "application/pdf";
        else if(file.getName().toLowerCase().endsWith(".docx")) type = "application/msword";

        String response = "";
        if(found == 1){
            fis = new FileInputStream(file);
            response += "UPLOAD " + this.filename + "\r\n" +
                    "Content-Type: " + type + "\r\n" +
                    "Content-Disposition: attachment; filename=" + file.getName() + "\r\n" +
                    "Content-Length: " + fis.available() + "\r\n\r\n";
        }else if(found == 0){
            response += "UPLOAD " + this.filename + "_format" + "\r\n\r\n\r\n";
        }else{
            response += "UPLOAD " + this.filename + "_name" + "\r\n\r\n\r\n";
        }

        os.write(response.getBytes());

        if(found == 1){
            int count;
            byte[] buffer = new byte[CHUNK_SIZE];
            while ((count = fis.read(buffer)) > 0) os.write(buffer, 0, count);
        }

        os.flush();
        if(found == 1) fis.close();

        if(found == 1) System.out.println("Uploading file " + this.filename + ": successful");
        else if(found == 0) System.out.println("Uploading file " + this.filename + ": invalid file format");
        else System.out.println("Uploading file " + this.filename + ": invalid file name");

        closeConnection();
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
            int found = -1;
            File dir = new File(FILE_PATH);
            File reqd = new File(FILE_PATH, this.filename);

            //File[] files = dir.listFiles((file, s) -> checkIfFileHasExtension(s, extensions));
            File[] files = dir.listFiles();

            for(File f : files){
                if(f.equals(reqd)){
                    if(checkIfFileHasExtension(f.getName().toLowerCase().split("[.]", 0)[1], extensions)) found = 1;
                    else found = 0;
                    break;
                }
            }

            uploadFile(reqd, found);
        }catch(Exception e){
            closeConnection();
            e.printStackTrace();
        }
    }
}
