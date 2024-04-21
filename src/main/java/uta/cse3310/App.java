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
    // Create a map to store the connection and the username
    private Map<WebSocket, String> connectionUserMap = new HashMap<>();

    public App(int port) {
        super(new InetSocketAddress(port));
        initializeGameRooms();
    }



    // Player List
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

    // add to playermap
    private void addPlayer(Player player) {
        playerMap.put(player.getUsername(), player);
        // print out the playerMap
        System.out.println("Player Map:");
        for (Map.Entry<String, Player> entry : playerMap.entrySet()) {
            System.out.println("Username : " + entry.getKey());
        }
    }

    private void broadcastGameRooms() {
        //debug
        System.out.println("Broadcasting game rooms");
        List<Map<String, String>> gameRoomsInfo = new ArrayList<>();
        
        // Iterate through all game rooms to gather their information
        for (Map.Entry<String, Game> entry : gameMap.entrySet()) {
            Map<String, String> roomInfo = new HashMap<>();
            roomInfo.put("name", entry.getKey()); // Room name (lobby name)
            Game game = entry.getValue();
            // The number of players is obtained and formatted as a string like "1/2" or "2/2"
            String players = game.getCurrentNumberOfPlayers() + "/2";
            roomInfo.put("players", players);
            gameRoomsInfo.add(roomInfo); // Add the room info to the list

            //debug
            System.out.println("Room: " + entry.getKey() + " Players: " + players);
        }
    
        Gson gson = new Gson();
        String gameRoomsJson = gson.toJson(gameRoomsInfo); // Convert the list of maps to JSON string
    
        // Broadcast the game rooms JSON string to all connected clients
        for (WebSocket conn : this.getConnections()) {
            conn.send("update_gameRooms:" + gameRoomsJson);
            //debug
            System.out.println("[App.java broadcastGameRooms()]Broadcasting game rooms to client");
        }
    }
    


    // --------------------------- GAMES ---------------------------
    // Initialize Games
    private void initializeGameRooms() {
        // Create four game rooms with unique lobby names
        gameMap.put("Room1", new Game("Room1"));
        gameMap.put("Room2", new Game("Room2"));
        gameMap.put("Room3", new Game("Room3"));
        gameMap.put("Room4", new Game("Room4"));
    }

    public Map<String, Game> getGameMap() {
        return gameMap;
    }

    public void printAllGamePlayers() {
        System.out.println("Printing all game players:");
        for (Game game : gameMap.values()) {
            System.out.println("Game Lobby: " + game.getLobbyName());
            game.printPlayers();
        }
    }

    ////////// GAME ROOMS //////////////
    public synchronized String tryJoinGame(String lobbyName, Player player) {
        Game game = gameMap.get(lobbyName);
        if (game != null && !game.isFull()) {
            boolean added = game.addPlayer(player);
            if (added) {
                broadcastGameRooms();
                if (game.isReadyToStart()) {
                    game.startGame();
                    return "redirect:section2"; // Players are redirected to start the game
                }
                return "wait"; // Player needs to wait for another player to join
            }
        }
        return "full"; // Game is full, or no game exists with the given lobby name
    }


    private void handleMessage(WebSocket conn, String message) {

        if (message.startsWith("new_player:")) {
            String username = message.substring(11);
            Player player = new Player(username, conn);
            // append to playerMap with try catch
            try {
                addPlayer(player); // Store new player information in playerMap
                addPlayer(username, conn); // Store new player information in connectionUserMap
                // conn.send("player_added");
                // conn.send("player_added:" + username); // This will send message to
                // websocket.js socket.onmessage and it will show screen accordingly

                // send updated player list to all clients
                broadcastGameRooms();
                broadcastPlayerList();

                conn.send("set_username:" + username); // Use "set_username" to set the username for the client
                System.out.println("New player added: " + player.getUsername());

                conn.send("player_added:" + username);
                System.out.println("[from conn.send] New player added: " + player.getUsername());

            } catch (Exception e) {
                conn.send("player_not_added");
                System.out.println("Player not added: " + username);
            }
        }

        ///// game rooms //////
        else if (message.startsWith("new_player:")) {
            String username = message.substring("new_player:".length());
            addPlayer(username, conn); // Store new player information
            broadcastGameRooms(); // Update the player list for all clients
        } else if (message.startsWith("join_game:")) {
            String lobbyName = message.substring("join_game:".length());
            Player player = getPlayerByConnection(conn); // Get the player object associated with the connection
            String joinGameResponse = tryJoinGame(lobbyName, player);
            // Handle the join game response, such as updating client state or sending a
            // redirect command
            System.out.println("[Adding game] Join game response: " + joinGameResponse);
            printAllGamePlayers();
        }

        // socket.send("user_left:" + username); // This will send message to
        // websocket.js socket.onmessage and it will show screen accordingly
        else if (message.startsWith("user_left:")) {
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
        System.out
                .println("WebSocket connection closed: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());

        // // Find the username associated with the closing connection
        // String disconnectedUsername = null;
        // for (Map.Entry<String, Player> entry : playerMap.entrySet()) {
        // Player player = entry.getValue();
        // if (player.getWebSocket() != null && player.getWebSocket().equals(conn)) {
        // disconnectedUsername = entry.getKey();
        // break;
        // }
        // }

        // // If a username was found, remove that player from the map
        // if (disconnectedUsername != null) {
        // playerMap.remove(disconnectedUsername);
        // System.out.println("Player removed: " + disconnectedUsername);
        // // Optionally broadcast the updated player list after removal
        // broadcastPlayerList();
        // }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message from WebSocket client: " + message);
        handleMessage(conn, message);
        conn.send(message); // This will send message to websocket.js socket.onmessage and it will show
                            // screen accordingly
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());

        // Find the username associated with the connection that caused the error
        String errorUsername = null;
        for (Map.Entry<String, Player> entry : playerMap.entrySet()) {
            Player player = entry.getValue();
            if (player.getWebSocket() != null && player.getWebSocket().equals(conn)) {
                errorUsername = entry.getKey();
                break;
            }
        }

        // If a username was found, remove that player from the map
        if (errorUsername != null) {
            playerMap.remove(errorUsername);
            connectionUserMap.remove(conn); // Clean up the connection map
            System.out.println("Player removed (Tab Closed): " + errorUsername);
            // Optionally broadcast the updated player list after removal
            broadcastPlayerList();
            System.out.println("On Disconnect : ");;
            broadcastGameRooms();
        }

        System.out.println("On Error : ");;
        broadcastGameRooms();

    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started successfully");
        broadcastGameRooms();
    }

    // Get player by connection
    private Player getPlayerByConnection(WebSocket conn) {
        String username = connectionUserMap.get(conn);
        return playerMap.get(username);
    }

    // Add player to connectionMap

    private void addPlayer(String username, WebSocket conn) {
        Player player = new Player(username, conn);
        try {
            playerMap.put(username, player);
            connectionUserMap.put(conn, username);
            System.out.println("Adding player to connectionUserMap: " + username);
            broadcastGameRooms();
        } catch (Exception e) {
            System.out.println("Error adding player to connectionUserMap: " + e.getMessage());
        }
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
