package uta.cse3310;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class App {
    public static void main(String[] args) {
        int httpPort = 9080;
        String httpPortEnv = System.getenv("HTTP_PORT");
        if (httpPortEnv != null) {
            httpPort = Integer.parseInt(httpPortEnv);
        }

        HttpServer httpServer = new HttpServer(httpPort, "src/main/webapp/html");
        httpServer.start();
        System.out.println("[App.java] HTTP Server started on port: " + httpPort);

        int wsPort = 9180;
        String wsPortEnv = System.getenv("WEBSOCKET_PORT");
        if (wsPortEnv != null) {
            wsPort = Integer.parseInt(wsPortEnv);
        }

        final int finalWsPort = wsPort;

        WebSocketServer webSocketServer = new WebSocketServer(new InetSocketAddress(finalWsPort)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                System.out.println("Client connected: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                System.out.println("Client disconnected: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                System.out.println("Received message from client: " + message);
                if (message.startsWith("{") && message.contains("\"PlayerUsername\"")) {
                    // Parse the message to extract username and color
                    String username = message.split("\"PlayerUsername\":\"")[1].split("\"")[0];
                    String color = message.split("\"GridColorChoice\":\"")[1].split("\"")[0];
                    savePlayerData(username, color);
                    conn.send("Player data saved successfully.");
                    conn.send("Player saved");
                }
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                System.err.println("WebSocket error with connection: " + conn + "\nError: " + ex.getMessage());
                ex.printStackTrace();
            }

            @Override
            public void onStart() {
                System.out.println("WebSocket server started at ws://localhost:" + finalWsPort + "/websocket");
            }
        };

        webSocketServer.start();
        System.out.println("WebSocket Server started on port: " + finalWsPort);
    }

    private static void savePlayerData(String username, String color) {
        Path filePath = Paths.get("src/main/java/uta/cse3310/players.json");
        
        try {
            // Read the entire file content as a single string
            String content = new String(Files.readAllBytes(filePath));
            // Remove the closing bracket from the JSON array
            content = content.trim();
            if (content.endsWith("]")) {
                content = content.substring(0, content.length() - 1);
            }
            
            // Comma handling for JSON array elements
            if (!content.endsWith("[")) {
                content += ",";
            }

            // Create the new player JSON string
            String newPlayerJson = String.format("\n    {\"PlayerUsername\": \"%s\", \"Online\": true, \"GameWon\": 0, \"GameLost\": 0, \"InGamePoints\": 0, \"OpponentUsername\": null, \"GridColorChoice\": \"%s\"}", username, color);
            
            // Append the new player JSON and close the array
            content += newPlayerJson + "\n]";

            // Write back to the file
            Files.write(filePath, content.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Player data appended to file.");
            
        } catch (Exception e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    
    }
}
