package Server;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Connection extends Thread{
    private boolean running;
    private Socket socket;
    private InputStream in;
    private OutputStream pw;
    private String req = "";
    private String filename = "";
    private String http = "";
    private String rootPath = "";
    private List<File> searchedFile;
    static final int CHUNK_SIZE = 50;

    public Connection(Socket socket, InputStream in, OutputStream pw, String rootPath){
        this.running = true;
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
            filename = filename.replaceAll("%20", " ");
            //System.out.println(req+" "+filename+" "+http);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public int findFile(File dir, String dst) {
        File f = new File(dir.getPath(), dst);

        if(f.equals(dir)){
            searchedFile.add(f);
            return 1;
        }

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

    public void handleIrrelevantRequest() throws Exception {
        String response = "400 NOT FOUND\r\nServer: Java HTTP Server: 1.0\nDate: \" + new Date() + \"\\r\\n";
        byte[] arr = response.getBytes();
        pw.write(arr);
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
        String response = "HTTP/1.1 400 NOT FOUND\n" +
                "Server: Java HTTP Server: 1.0\n" +
                "Date: " + new Date() + "\r\n" +
                "Content-Type: text/html\n" +
                "Content-Length: " + content.length() + "\r\n" +
                "\r\n" + content;

        byte[] arr = response.getBytes();
        pw.write(arr);
        pw.flush();

        System.out.println("404 : Page not found");
    }

    public void generateDirectoryResponse(File dir) throws Exception{
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>\n");
        htmlContent.append("<html lang=\"en\">\n");
        htmlContent.append("<head>\n");
        htmlContent.append("<meta charset=\"UTF-8\">\n");
        htmlContent.append("<title>Directory</title>\n");
        htmlContent.append("</head>\n");
        htmlContent.append("<body>\n");
        htmlContent.append("<h1>List of all files:</h1>\n");
        htmlContent.append("<ol>\n");

        for(File f : dir.listFiles()){
            if(f.isDirectory()) htmlContent.append("<li><a href = \"/" + f.getName() + "\"><b><i>" + f.getPath() + "</i></b></a></li>\n");
            else htmlContent.append("<li><a href = \"/" + f.getName() + "\">" + f.getPath() + "</a></li>\n");
        }

        htmlContent.append("</ol>\n");
        htmlContent.append("</body>\n");
        htmlContent.append("</html>");
        String htmlString = htmlContent.toString();
        byte[] htmlBytes = htmlString.getBytes();

        String response = "HTTP/1.1 200 OK\r\n" +
                "Server: Java HTTP Server: 1.0\r\n" +
                "Date: " + new Date() + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + htmlBytes.length + "\r\n\r\n";
        pw.write(response.getBytes());
        pw.write(htmlBytes);
        pw.flush();
    }

    public void generateFileResponse(File file) throws Exception{
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>\n");
        htmlContent.append("<html lang=\"en\">\n");
        htmlContent.append("<head>\n");
        htmlContent.append("<meta charset=\"UTF-8\">\n");
        htmlContent.append("<title>Content of File</title>\n");
        htmlContent.append("</head>\n");
        htmlContent.append("<body>\n");
        htmlContent.append("<h1>Content:</h1>\n");

        if(file.getName().toLowerCase().endsWith(".txt")){
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while(( line = br.readLine()) != null ) {
                sb.append( line );
                sb.append( '\n' );
            }

            String text = sb.toString();

            htmlContent.append("<p>" + text + "</p>\n");
            htmlContent.append("</body>\n");
            htmlContent.append("</html>");
            String htmlString = htmlContent.toString();
            byte[] htmlBytes = htmlString.getBytes();

            String response = "HTTP/1.1 200 OK\r\n" +
                    "Server: Java HTTP Server: 1.0\r\n" +
                    "Date: " + new Date() + "\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + htmlBytes.length + "\r\n\r\n";
            pw.write(response.getBytes());
            pw.write(htmlBytes);
            pw.flush();
        }else if(file.getName().toLowerCase().endsWith(".jpg")){
            htmlContent.append("<img src=\"/image/" + file.getPath() + "\" alt=\"" + file.getName() + " image\" height=\"800\" width=\"800\">\n");
            htmlContent.append("</body>\n");
            htmlContent.append("</html>");
            String htmlString = htmlContent.toString();
            byte[] htmlBytes = htmlString.getBytes();

            String htmlResponse = "HTTP/1.1 200 OK\r\n" +
                    "Server: Java HTTP Server: 1.0\r\n" +
                    "Date: " + new Date() + "\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + htmlBytes.length + "\r\n\r\n";
            pw.write(htmlResponse.getBytes());
            pw.write(htmlBytes);
            pw.flush();
        }else{
            FileInputStream fis = new FileInputStream(file);

            String type = "";
            if(file.getName().toLowerCase().endsWith(".pdf")) type = "application/pdf";
            else if(file.getName().toLowerCase().endsWith(".docx")) type = "application/msword";

            String httpResponse = "HTTP/1.1 200 OK\r\n" +
                    "Server: Java HTTP Server: 1.0\r\n" +
                    "Date: " + new Date() + "\r\n" +
                    "Content-Type: " + type + "\r\n" +
                    "Content-Disposition: attachment; filename=" + file.getName() + "\r\n" +
                    "Content-Length: " + fis.available() + "\r\n\r\n";
            pw.write(httpResponse.getBytes());

            int count;
            byte[] buffer = new byte[CHUNK_SIZE];
            while ((count = fis.read(buffer)) > 0)
            {
                pw.write(buffer, 0, count);
            }

            pw.flush();
            fis.close();
        }
    }

    public void getImage(File image) throws Exception{
        byte[] imageBytes = Files.readAllBytes(image.toPath());
        String imageResponse = "HTTP/1.1 200 OK\r\n" +
                "Server: Java HTTP Server: 1.0\r\n" +
                "Date: " + new Date() + "\r\n" +
                "Content-Type: image/jpeg\r\n" +
                "Content-Length: " + imageBytes.length + "\r\n\r\n";
        pw.write(imageResponse.getBytes());
        pw.write(imageBytes);
        pw.flush();
    }

    public void closeConnection(){
        try {
            this.socket.close();
            this.running = false;
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    @Override
    public void run(){
        try{
            extractFileName(in);

            if(!http.equals("HTTP/1.1") || !req.equals("GET") || this.filename.equals("favicon.ico")){
                handleIrrelevantRequest();
            }
            else if(filename.startsWith("image/")){
                //send an image
                getImage(new File(filename.substring(6)));
            }
            else{
                int res = findFile(new File(this.rootPath), this.filename);
                //System.out.println("FILENAME: " + this.filename + ", RES: " + res);

                if(res == 1){
                    //code for dir
                    generateDirectoryResponse(new File(searchedFile.get(0).getPath()));
                }else if(res == 0){
                    //code for file
                    generateFileResponse(new File(searchedFile.get(0).getPath()));
                }else if(res == -1) generateErrorMessage();
            }
            closeConnection();
        }catch(Exception e){
            closeConnection();
            System.out.println(e);
        }
    }
}
