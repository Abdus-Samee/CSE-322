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
    private List<File> searchedFile;
    static final String rootPath = "root/";
    static final String uploadPath = "uploaded/";
    static final int CHUNK_SIZE = 50;
    static final String LOG_FILE = "log.txt";

    public Connection(Socket socket, InputStream in, OutputStream pw){
        this.running = true;
        this.socket = socket;
        this.in = in;
        this.pw = pw;
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
            if(arr.length == 3){
                req = arr[0].trim();
                filename = arr[1].substring(1).trim();
                http = arr[2].trim();
            }else{
                req = arr[0].trim();
                filename = arr[1].trim();
            }
            filename = filename.replaceAll("%20", " ");
            //System.out.println(req+" "+filename+" "+http);
            //System.out.println(req+" "+filename);
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
        String response = "400 NOT FOUND\r\nServer: Java HTTP Server: 1.0\r\nDate: " + new Date() + "\r\n";
        byte[] arr = response.getBytes();
        pw.write(arr);
        pw.flush();

        generateLog(response);
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
        String response = "HTTP/1.1 400 NOT FOUND\r\n" +
                "Server: Java HTTP Server: 1.0\r\n" +
                "Date: " + new Date() + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + content.length() + "\r\n" +
                "\r\n" + content;

        byte[] arr = response.getBytes();
        pw.write(arr);
        pw.flush();

        System.out.println("404 : Page not found");
        generateLog(response);
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

        generateLog(response);
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

            generateLog(response);
        }else if(file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".png") || file.getName().toLowerCase().endsWith(".bmp")){
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

            generateLog(htmlResponse);
        }else{
            FileInputStream fis = new FileInputStream(file);

            String type = "";
            if(file.getName().toLowerCase().endsWith(".pdf")) type = "application/pdf";
            else if(file.getName().toLowerCase().endsWith(".docx")) type = "application/msword";
            else if(file.getName().toLowerCase().endsWith(".mp4")) type = "video/mp4";
            else if(file.getName().toLowerCase().endsWith(".mp4")) type = "audio/mpeg";

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

            generateLog(httpResponse);
        }
    }

    public void getImage(File image) throws Exception{
        String type = "";
        if(image.getName().toLowerCase().endsWith(".jpg")) type = "image/jpeg";
        else if(image.getName().toLowerCase().endsWith(".png")) type = "image/png";
        else if(image.getName().toLowerCase().endsWith(".bmp")) type = "image/bmp";

        byte[] imageBytes = Files.readAllBytes(image.toPath());
        String imageResponse = "HTTP/1.1 200 OK\r\n" +
                "Server: Java HTTP Server: 1.0\r\n" +
                "Date: " + new Date() + "\r\n" +
                "Content-Type: " + type + "\r\n" +
                "Content-Length: " + imageBytes.length + "\r\n\r\n";
        pw.write(imageResponse.getBytes());
        pw.write(imageBytes);
        pw.flush();

        generateLog(imageResponse);
    }

    public void uploadFile(File file) throws Exception{
        FileOutputStream fos = new FileOutputStream(file);
        BufferedInputStream bin = new BufferedInputStream(in);
        byte[] input = new byte[CHUNK_SIZE];
        int c;
        while((c = bin.read(input)) != -1) fos.write(input, 0, c);
        fos.close();
        bin.close();
        System.out.println("Uploaded: " + this.filename);
    }

    public void uploadFileError(String msg){
        System.out.println("Uploading file " + this.filename.split("_")[0] + " " + msg);
    }

    public void generateLog(String response){
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE, true));
            bw.write("----------HTTP REQUEST----------\n");
            bw.write("Http request:\n");
            bw.write(this.req + " " + this.filename + " " + this.http + "\n\n");
            bw.write("Http response:\n");
            bw.write(response + "\n");
            bw.close();
        }catch(Exception e){
            System.out.println(e);
            closeConnection();
        }
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

            if(req.equals("UPLOAD")){
                if(this.filename.endsWith("_format")) uploadFileError("invalid file format");
                else if(this.filename.endsWith("_name")) uploadFileError("invalid file name");
                else uploadFile(new File(uploadPath, this.filename));
            }
            else if(!http.equals("HTTP/1.1") || !req.equals("GET") || this.filename.equals("favicon.ico")){
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
                    generateDirectoryResponse(new File(searchedFile.get(0).getPath()));
                }else if(res == 0){
                    generateFileResponse(new File(searchedFile.get(0).getPath()));
                }else if(res == -1) generateErrorMessage();
            }
            closeConnection();
        }catch(Exception e){
            closeConnection();
            //System.out.println(e);
        }
    }
}
