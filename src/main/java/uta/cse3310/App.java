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

    
    // send the username keys of players as json
    private void sendPlayerList(WebSocket conn) {
        List<String> playerNames = new ArrayList<>();
        for (Map.Entry<String, Player> entry : playerMap.entrySet()) {
            playerNames.add(entry.getKey()); // Add the player's name to the list
        }
        
        Gson gson = new Gson();
        String playerNamesJson = gson.toJson(playerNames); // Convert list to JSON
        conn.send("update_players:" + playerNamesJson);
    }

    private void handleMessage(WebSocket conn, String message){
        
        if (message.startsWith("new_player:")){
            String username = message.substring(11);
            Player player = new Player(username);
            // append to playerMap with try catch
            try{
                addPlayer(player);
                conn.send("player_added");
                sendPlayerList(conn); // send's player
                
                // print conn
                System.out.println("Player List Update conn ? " + conn);
                System.out.println("New player added: " + player.getUsername());
            } catch (Exception e){
                conn.send("player_not_added");
                System.out.println("Player not added: " + player.getUsername());
            }
        }

        //"new_lobby:" + lobbyName
        // else if (message.startsWith("new_lobby:")){
        //     String lobbyName = message.substring(10);
            
        //     // Create a new lobby
        //     // append to gameMap with try catch
        //     try{
        //         gameMap.put(lobbyName, game);
        //         conn.send("lobby_added");
        //         System.out.println("New lobby added: " + game.getLobbyName());
        //     } catch (Exception e){
        //         conn.send("lobby_not_added");
        //         System.out.println("Lobby not added: " + game.getLobbyName());
        //     }
        // }

        // else if (message.startsWith("color_choice:")){
        //     String[] parts = message.split(":");
        //     String username = parts[1];
        //     String colorChoice = parts[2];
        //     Player player = playerMap.get(username);
        //     player.setColorChoice(colorChoice);
        //     conn.send("color_set");
        // }
        // else if (message.startsWith("game_start:")){
        //     String[] parts = message.split(":");
        //     String player1Username = parts[1];
        //     String player2Username = parts[2];
        //     Player player1 = playerMap.get(player1Username);
        //     Player player2 = playerMap.get(player2Username);
        //     Game game = new Game(player1, player2);
        //     // Start the game
        //     conn.send("game_started");
        // }
        // else if (message.startsWith("game_end:")){
        //     String[] parts = message.split(":");
        //     String winnerUsername = parts[1];
        //     String loserUsername = parts[2];
        //     Player winner = playerMap.get(winnerUsername);
        //     Player loser = playerMap.get(loserUsername);
        //     winner.setGamesWon(winner.getGamesWon() + 1);
        //     loser.setGamesLost(loser.getGamesLost() + 1);
        //     // End the game
        //     conn.send("game_ended");
        // }
        // else if (message.startsWith("score_update:")){
        //     String[] parts = message.split(":");
        //     String username = parts[1];
        //     int score = Integer.parseInt(parts[2]);
        //     Player player = playerMap.get(username);
        //     player.setInGameScore(score);
        //     conn.send("score_updated");
        // }

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
