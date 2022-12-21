package Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Connection extends Thread{
    private Socket socket;
    private BufferedReader in;
    private PrintWriter pw;
    private String req = "";
    private String filename = "";
    private String http = "";
    private String rootPath = "";
    private List<File> searchedFile;

    public Connection(Socket socket, BufferedReader in, PrintWriter pw, String rootPath){
        this.socket = socket;
        this.in = in;
        this.pw = pw;
        this.rootPath = rootPath;
        this.searchedFile = new ArrayList<>();
    }

    public void extractFileName(InputStream inputStream) {

        String header = "";

        Reader reader = new InputStreamReader(inputStream);
        try {
            int c;
            while ((c = reader.read()) != -1) {
                header += (char) c;

                if (header.contains("\r\n"))
                    break;
            }

            String[] arr = header.split(" ");
            req = arr[0].trim();
            filename = arr[1].substring(1).trim();
            http = arr[2].trim();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public int findFile(File dir, String dst) {
        if((dir == null) || (dir.listFiles() == null)){
            return -1;
        }

        File f = new File(dir.getPath(), dst);

        for (File entry : dir.listFiles()) {
            if(f.equals(entry)){
                searchedFile.add(entry);
                if(entry.isFile()) return 0;
                else return 1;
            }else if(entry.isDirectory()){
                int res = findFile(entry, dst);
                if(res != -1) return res;
            }
        }

        return -1;
    }

    public void handleIrrelevantRequest(){
        pw.write("400 NOT FOUND\r\n");
        pw.write("Server: Java HTTP Server: 1.0\r\n");
        pw.write("Date: " + new Date() + "\r\n");
        pw.flush();
    }

    public void generateErrorMessage() throws Exception{
        File file = new File("404.html");
        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while(( line = br.readLine()) != null ) {
            sb.append( line );
            sb.append( '\n' );
        }

        String content = sb.toString();

        pw.write("HTTP/1.1 400 NOT FOUND\r\n");
        pw.write("Server: Java HTTP Server: 1.0\r\n");
        pw.write("Date: " + new Date() + "\r\n");
        pw.write("Content-Type: text/html\r\n");
        pw.write("Content-Length: " + content.length() + "\r\n");
        pw.write("\r\n");
        pw.write(content);
        pw.flush();

        System.out.println("404 : Page not found");
    }

    public void generateDirectoryResponse(File dir){
        String content ="<!DOCTYPE html>\n" +
                        "<html lang=\"en\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <title>Directory</title>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<h1>List of all files:</h1>\n" +
                        "<ol>\n";

        for(File f : dir.listFiles()){
            if(f.isDirectory()) content += "<li><a href = \"/" + f.getName() + "\"><b><i>" + f.getPath() + "</i></b></a></li>\n";
            else content += "<li><a href = \"/" + f.getName() + "\">" + f.getPath() + "</a></li>\n";
        }

        content += "</ol>\n" +
                   "</body>\n" +
                   "</html>";

        pw.write("HTTP/1.1 200 OK\r\n");
        pw.write("Server: Java HTTP Server: 1.0\r\n");
        pw.write("Date: " + new Date() + "\r\n");
        pw.write("Content-Type: text/html\r\n");
        pw.write("Content-Length: " + content.length() + "\r\n");
        pw.write("\r\n");
        pw.write(content);
        pw.flush();
    }

    @Override
    public void run(){
        while(true){
            try{
                InputStream is = this.socket.getInputStream();
                extractFileName(is);

                if(!http.equals("HTTP/1.1") || !req.equals("GET")){
                    handleIrrelevantRequest();
                    break;
                }else{
                    int res = findFile(new File(this.rootPath), this.filename);
                    //System.out.println("RES: " + res);

                    if(res == 1){
                        //code for dir
                        generateDirectoryResponse(new File(searchedFile.get(0).getPath()));
                    }else if(res == 0){
                        //code for file
                    }else generateErrorMessage();

                    break;
                }

                //use FileFilter
            }catch(Exception e){
                System.out.println(e);
            }
        }

        try {
            this.socket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
