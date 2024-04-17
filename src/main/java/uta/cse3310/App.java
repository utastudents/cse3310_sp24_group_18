package uta.cse3310;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.util.Map;
import java.util.HashMap;

public class App extends WebSocketServer {
    private static Map<WebSocket, Game> games = new HashMap<>();

    public App(int port) {
        super(new InetSocketAddress(port));
        Game.loadWords();
    }

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

        App webSocketServer = new App(finalWsPort);
        webSocketServer.start();
        System.out.println("WebSocket Server started on port: " + finalWsPort);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Client connected: " + conn.getRemoteSocketAddress());
        Game game = new Game();
        games.put(conn, game);
        String initialData = "{\"action\":\"new_game_created\",\"grid\":" + game.getGridAsJson() + ",\"placedWords\":" + game.getPlacedWordsAsJson() + "}";
        conn.send(initialData);
        game.printGrid();
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Client disconnected: " + conn.getRemoteSocketAddress());
        games.remove(conn);
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
        Game game = games.get(conn);
        if (game != null) {
            game.handleMessage(message, conn);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket error with connection: " + conn + "\nError: " + ex.getMessage());
        games.remove(conn);
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started at ws://localhost:" + getPort() + "/websocket");
    }

    private static void savePlayerData(String username, String color) {
        Path filePath = Paths.get("src/main/webapp/html/new_players.json");

        try {
            String content = new String(Files.readAllBytes(filePath));
            content = content.trim();
            if (content.endsWith("]")) {
                content = content.substring(0, content.length() - 1);
            }
            if (!content.endsWith("[")) {
                content += ",";
            }

            String newPlayerJson = String.format("\n    {\"PlayerUsername\": \"%s\", \"Online\": true, \"GameWon\": 0, \"GameLost\": 0, \"InGamePoints\": 0, \"OpponentUsername\": null, \"GridColorChoice\": \"%s\"}", username, color);
            content += newPlayerJson + "\n]";

            Files.write(filePath, content.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Player data appended to file.");

        } catch (Exception e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
