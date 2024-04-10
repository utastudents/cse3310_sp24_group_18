package uta.cse3310;

import java.io.*;
import java.net.*;
import java.nio.file.*;

public class Server {
    private final int port;
    private final String webroot;

    public Server(int port, String webroot) {
        this.port = port;
        this.webroot = webroot;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server has started on port " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleClient(clientSocket);
                } catch (IOException e) {
                    System.out.println("Server exception: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Server could not start on port " + port);
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            String requestLine = in.readLine();
            if (requestLine != null && !requestLine.isEmpty()) {
                System.out.println("Request: " + requestLine);
                String[] tokens = requestLine.split(" ");
                if (tokens.length >= 3 && tokens[0].equals("GET")) {
                    String filePath = tokens[1].equals("/") ? "/index.html" : tokens[1];
                    serveFile(out, filePath);
                }
            }
        } catch (IOException e) {
            System.out.println("Client handler exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void serveFile(OutputStream out, String filePath) {
        Path path = Paths.get(webroot, filePath);
        if (Files.exists(path) && !Files.isDirectory(path)) {
            try {
                String contentType = determineContentType(path);

                // Send HTTP Headers
                out.write(("HTTP/1.1 200 OK\r\nContent-Type: " + contentType + "\r\nContent-Length: " + Files.size(path) + "\r\n\r\n").getBytes());
                out.flush();

                // Send File Content
                Files.copy(path, out);
                out.flush();
            } catch (IOException e) {
                System.out.println("Error serving the file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            try {
                // File not found - send 404
                String response = "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\n\r\n";
                out.write(response.getBytes());
                out.flush();
            } catch (IOException e) {
                System.out.println("Error sending 404: " + e.getMessage());
            }
        }
    }

    private String determineContentType(Path path) throws IOException {
        String mimeType = Files.probeContentType(path);
        if (mimeType == null) {
            String fileName = path.toString().toLowerCase();
            if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
                mimeType = "text/html";
            } else if (fileName.endsWith(".css")) {
                mimeType = "text/css";
            } else if (fileName.endsWith(".js")) {
                mimeType = "application/javascript";
            } else if (fileName.endsWith(".json")) {
                mimeType = "application/json";
            } else if (fileName.endsWith(".csv")) {
                mimeType = "text/csv";
            } else if (fileName.endsWith(".java")) {
                mimeType = "text/plain"; // Source code should be served as plain text
            } // Add more file types and MIME types as needed
            else {
                mimeType = "application/octet-stream"; // Fallback binary type
            }
        }
        return mimeType;
    }

    public static void main(String[] args) {
        String webroot = "./src/main/webapp/html"; // The webroot should point to where your files are served from
        int port = 12345;
        new Server(port, webroot).start();
    }
}
