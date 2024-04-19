package uta.cse3310;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import com.google.gson.Gson;

public class App extends WebSocketServer {

    // Create an appendable map to store players
    private Map<String, Player> playerMap = new HashMap<>();
    // Create an appendable map to store games
    private Map<String, Game> gameMap = new HashMap<>();

    public App(int port) {
        super(new InetSocketAddress(port));
    }

    // add to playermap
    private void addPlayer(Player player){
        playerMap.put(player.getUsername(), player);
        // print out the playerMap
        System.out.println("Player Map:");
        for (Map.Entry<String, Player> entry : playerMap.entrySet()) {
            System.out.println("Username : " + entry.getKey());
        }
    }

    
    private void broadcastPlayerList() {
        List<String> playerNames = new ArrayList<>();
        for (Map.Entry<String, Player> entry : playerMap.entrySet()) {
            playerNames.add(entry.getKey()); // Add the player's name to the list
        }
    
        Gson gson = new Gson();
        String playerNamesJson = gson.toJson(playerNames); // Convert list to JSON
    
        // Broadcast to all connected clients
        for (WebSocket conn : this.getConnections()) {
            conn.send("update_players:" + playerNamesJson);
        }
    }
    

    private void handleMessage(WebSocket conn, String message){
        
        if (message.startsWith("new_player:")){
            String username = message.substring(11);
            Player player = new Player(username);
            // append to playerMap with try catch
            try{
                addPlayer(player);
                // conn.send("player_added");
               // conn.send("player_added:" + username); // This will send message to websocket.js socket.onmessage and it will show screen accordingly

                // send updated player list to all clients
                broadcastPlayerList();

                conn.send("set_username:" + username); // Use "set_username" to set the username for the client
                System.out.println("New player added: " + player.getUsername());

                conn.send("player_added:" + username);
                System.out.println("[from conn.send] New player added: " + player.getUsername());


            } catch (Exception e){
                conn.send("player_not_added");
                System.out.println("Player not added: " + username);
            }
        }

        // socket.send("user_left:" + username);  // This will send message to websocket.js socket.onmessage and it will show screen accordingly
        else if (message.startsWith("user_left:")){
            String username = message.substring(10);
            playerMap.remove(username);
            conn.send("user_removed");
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New WebSocket connection: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
        conn.send("section0");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("WebSocket connection closed: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message from WebSocket client: " + message);
        handleMessage(conn, message);
        conn.send(message); // This will send message to websocket.js socket.onmessage and it will show screen accordingly
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started successfully");
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

        App webSocketServer = new App(wsPort);
        webSocketServer.start();
        System.out.println("WebSocket Server started on port: " + wsPort);
    }
}
