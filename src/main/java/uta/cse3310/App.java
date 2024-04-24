package uta.cse3310;

import java.io.IOException;
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


        ///// --------- GRID --------- /////
        private void broadcastGridUpdate(Game game) {
            if (game != null) {
                String gridJson = game.getGridAsJson();  // Make sure Game.java has this method
                String roomId = game.getGameRoomId();
                String message = String.format("update_grid:%s:%s", roomId, gridJson);
                for (Player player : game.getPlayers()) {
                    if (player.getWebSocket() != null && player.getWebSocket().isOpen()) {
                        player.getWebSocket().send(message);
                    }
                }
            }
        }

        
    private void broadcastGameRooms() {
        // debug
        System.out.println("broadcastGameRooms()_Broadcasting game rooms");
        List<Map<String, String>> gameRoomsInfo = new ArrayList<>();

        // Iterate through all game rooms to gather their information
        for (Map.Entry<String, Game> entry : gameMap.entrySet()) {
            Map<String, String> roomInfo = new HashMap<>();
            roomInfo.put("name", entry.getKey()); // Room name (lobby name)
            Game game = entry.getValue();
            // The number of players is obtained and formatted as a string like "1/2" or
            // "2/2"
            String players = game.getCurrentNumberOfPlayers() + "/2";
            roomInfo.put("players", players);
            gameRoomsInfo.add(roomInfo); // Add the room info to the list

            // debug
            System.out.println("Room: " + entry.getKey() + " Players: " + players);
        }

        Gson gson = new Gson();
        String gameRoomsJson = gson.toJson(gameRoomsInfo); // Convert the list of maps to JSON string

        // Broadcast the game rooms JSON string to all connected clients
        for (WebSocket conn : this.getConnections()) {
            conn.send("update_gameRooms:" + gameRoomsJson);
            // debug
            System.out.println("\n[App.java broadcastGameRooms()]\nBroadcasting game rooms to client");
        }
    }

    // --------------------------- GAMES ---------------------------
    // Initialize Games
    private void initializeGameRooms() {
        // Create four game rooms with unique lobby names
        // gameMap.put("Room1", new Game("Room1", "gameroom1"));
        // gameMap.put("Room2", new Game("Room2","gameroom2"));
        // gameMap.put("Room3", new Game("Room3","gameroom3"));
        // gameMap.put("Room4", new Game("Room4","gameroom4"));

        // 5 concurrent games
        gameMap.put("gameroom1", new Game("Room1", "gameroom1"));
        gameMap.put("gameroom2", new Game("Room2", "gameroom2"));
        gameMap.put("gameroom3", new Game("Room3", "gameroom3"));
        gameMap.put("gameroom4", new Game("Room4", "gameroom4"));
        gameMap.put("gameroom5", new Game("Room5", "gameroom5"));
        System.out.println("Game rooms initialized.");

    }

    public void printAllGamePlayers() {
        System.out.println("Printing all game players:");
        for (Game game : gameMap.values()) {
            System.out.println("Game Lobby: " + game.getLobbyName());
            game.printPlayers();
        }
    }

    //////// GAME ROOMS //////////////
    public synchronized String tryJoinGame(String lobbyName, Player player) {
        Game game = gameMap.get(lobbyName);
        if (game != null && !game.isFull()) {
            boolean added = game.addPlayer(player);
            if (added) {
                broadcastGameRooms();
                if (game.isReadyToStart()) {
                    ///// ----------------- GAME START ----------------- /////
                    List<Player> players = game.getPlayers(); // Get list of players
                    if (players.size() == 2) { // Ensuring exactly two players are present
                        String gameRoomId = game.getGameRoomId(); // Get the game room ID dynamically
                        String startCommand = String.format("start_game:%s:%s:%s",
                                gameRoomId, players.get(0).getUsername(), players.get(1).getUsername());
    
                        // Send game details such as the grid and words to both players
                        game.sendGameDetails(players.get(0).getWebSocket());
                        game.sendGameDetails(players.get(1).getWebSocket());
    
                        // Send the start game command to both players
                        players.get(0).getWebSocket().send(startCommand);
                        players.get(1).getWebSocket().send(startCommand);
    
                        // Send the initial grid as JSON to both players
                        String gridJson = game.getGridAsJson(); // Make sure Game.java has this method
                        String gridUpdateMessage = String.format("update_grid:%s:%s", gameRoomId, gridJson);
                        players.get(0).getWebSocket().send(gridUpdateMessage);
                        players.get(1).getWebSocket().send(gridUpdateMessage);
    
                        game.startGame();
                        return "redirect:" + gameRoomId; // Redirect to the specific game room
                    }
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
        else if (message.startsWith("check_word:")) {
            String[] parts = message.split(":");
            if (parts.length > 3) {
                String roomId = parts[1];
                String username = parts[2];
                String word = parts[3];
                Game game = gameMap.get(roomId);
                if (game != null && game.checkWord(username, word)) {
                    // After updating the score in checkWord, now broadcast the score
                    broadcastScore(roomId);
                }
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
            // System.out.println("[Adding game] Join game response: " + joinGameResponse);
            printAllGamePlayers();
        }
        else if (message.startsWith("check_word:")) {
            String[] parts = message.split(":");
            if (parts.length > 3) {
                String roomId = parts[1];
                String username = parts[2];
                String word = parts[3];
                System.out.println("\n- GOT CHECK WORD REQUEST -\n" + "WORD : " + word + " ");
                handleCheckWord(conn, roomId, username, word);
            }

        // socket.send("user_left:" + username); // This will send message to
        // websocket.js socket.onmessage and it will show screen accordingly
        else if (message.startsWith("user_left:")) {
            String username = message.substring(10);
            playerMap.remove(username);
            conn.send("user_removed");
        }
        else {
            System.out.println("NULL FUNC");
        }}
        
    }
    private void handleCheckWord(WebSocket conn, String roomId, String username, String word) {
        Game game = gameMap.get(roomId);
        if (game != null) {
            boolean wordFound = game.checkWord(username, word);
            if (wordFound) {
                conn.send("word_found:" + word);  // Notify client that the word was found
                game.printWordsFoundByUser(username);  // Optionally print all words found by the user so far
                broadcastScore(roomId);  // Broadcast updated scores after a word is found
            } else {
                conn.send("word_not_found:" + word);  // Notify client that the word was not found or already marked
            }
        } else {
            conn.send("error:Game not found");
        }
    }
  
    public void printPlayerWordCounts() {
        for (Game game : gameMap.values()) {
            System.out.println("Game Lobby: " + game.getLobbyName() + " Word Counts:");
            game.printAllWordsAndTheirStatus(); // Assuming Game.java has this method to print each word and whether it's found
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
            System.out.println("\n--PLAYER REMOVED--\nPlayer removed (Tab Closed): " + errorUsername+"\n\n");
            // Optionally broadcast the updated player list after removal
            broadcastPlayerList();
            System.out.println("On Disconnect : ");
            ;
            broadcastGameRooms();
        }

        System.out.println("On Error : ");
        ;
        broadcastGameRooms();

    }

    private void broadcastChatMessage(String roomID, String message) {
        Game game = findGameByRoomID(roomID);
        if (game != null) {
            // Retrieve the players from the game room
            Player[] players = game.getPlayers_chat(); // Assuming you have a method to retrieve players
            for (Player player : players) {
                if (player != null && player.getWebSocket() != null && player.getWebSocket().isOpen()) {
                    player.getWebSocket().send("chat_update:" + roomID + ":" + message);
                }
            }
        } else {
            System.err.println("Game with room ID '" + roomID + "' not found.");
        }
    }

    private Game findGameByRoomID(String roomID) {
        return gameMap.get(roomID);
    }

    private void clearPlayerStates(Game game) {
        if (game.getPlayer1() != null) {
            removePlayer(game.getPlayer1());
        }
        if (game.getPlayer2() != null) {
            removePlayer(game.getPlayer2());
        }
        System.out.println("Cleared players from the reset game.");
    }

    // Call this method to remove player and ensure all states are synchronized
    private void removePlayer(Player player) {
        playerMap.remove(player.getUsername());
        WebSocket conn = player.getWebSocket();
        if (conn != null && conn.isOpen()) {
            conn.close(); // Optionally close the connection or notify the player
        }
    }

    // Helper method to clear players from a game
    private void clearPlayersFromGame(Game game) {
        // Disconnect and remove players from the game
        disconnectPlayer(game.getPlayer1());
        disconnectPlayer(game.getPlayer2());

        // Player1 and Player2 are set to null
        System.out.println("\n---Players after RESET-\nPrinting players in the game: ");
        System.out.println("\n---Player's after DISCONNECT---\n");
        game.printPlayers();
        // Ensure the game is aware that the players are removed

        game.setPlayer1(null);
        game.setPlayer2(null);
        System.out.println("n---Player's after SET NULL---\n");
        game.printPlayers();
    }

    private void disconnectPlayer(Player player) {
        if (player != null && player.getWebSocket() != null) {
            WebSocket conn = player.getWebSocket();
            if (conn.isOpen()) {
                try {
                    conn.close(); // Close the WebSocket connection
                    System.out.println("Disconnected player: " + player.getUsername());
                } catch (Exception e) {
                    System.err.println("Error disconnecting player: " + player.getUsername() + "; " + e.getMessage());
                }
            }
        }
    }

    private void resetGame(String roomId, WebSocket conn) {
        Game game = gameMap.get(roomId);
        if (game != null) {
            // Reset the game, which clears all its internal states
            game.reset();

            // Clear player connections from the game
            clearPlayersFromGame(game);

            try {
                System.out.println("Sending game reset message to client: " + conn.getRemoteSocketAddress());
                conn.send("game_reset:" + roomId); // Notify the client that the game has been reset
                System.out.println("Game reset message sent.");

                System.out.println("resetGame() broadcasting game rooms");
                broadcastGameRooms(); // Update all clients with the new state of the game rooms
                broadcastPlayerList(); // Update all clients with the new player list

            } catch (Exception e) {
                System.err.println("Error sending game reset message to client: " + e.getMessage());
            }
        } else {
            conn.send("error:Game not found");
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("[broadcastChatMessage]Message from WebSocket client: " + message);
        if (message.startsWith("chat:")) {
            String[] parts = message.split(":", 3); // Split to get roomID and chat message
            String roomID = parts[1];
            String chatMessage = parts[2];

            Game game = findGameByRoomID(roomID);
            if (game != null) {
                game.getChat().addMessage(chatMessage); // Add message to the game's chat
                broadcastChatMessage(roomID, chatMessage); // Send the message to all clients in the room
            } else {
                System.err.println("Game with room ID '" + roomID + "' not found.");

            }
        }

        if (message.startsWith("reset_game:")) {
            System.out.println("\n---RESET----\n[App.java] Resetting game...");
            String roomId = message.split(":")[1];
            try {
                resetGame(roomId, conn);
            } catch (Exception e) {
                System.out.println("Error resetting game: " + e.getMessage());
            }
        }

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
            System.out.println("\n--PLAYER REMOVED--\nPlayer removed (Tab Closed): " + errorUsername+"\n\n");
            // Optionally broadcast the updated player list after removal
            broadcastPlayerList();
            System.out.println("On Disconnect : ");
            ;
            broadcastGameRooms();
        }

        System.out.println("On Error : ");
        ;
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

    public void broadcastScore(String roomId) {
        System.out.println("\n\nReached BroadcastScore()");
        Game game = gameMap.get(roomId);
        if (game != null) {
            Map<String, Integer> scores = game.getPlayerScores();
            Gson gson = new Gson();
            String scoresJson = gson.toJson(scores);
            
            for (WebSocket conn : this.getConnections()) {
                conn.send("update_scores:" + roomId + ":" + scoresJson);
            }
            // print scoresJson
            System.out.println("Scores JSON: " + scoresJson);
            System.out.println("\nScores broadcasted");
        }
        else
        {
            System.out.println("Game not found");
        }
    }

    public static void main(String[] args) {
        int httpPort = 9018;
        String httpPortEnv = System.getenv("HTTP_PORT");
        if (httpPortEnv != null) {
            httpPort = Integer.parseInt(httpPortEnv);
        }

        HttpServer httpServer = new HttpServer(httpPort, "src/main/webapp/html");
        httpServer.start();
        System.out.println("[App.java] HTTP Server started on port: " + httpPort);

        int wsPort = 9118;
        String wsPortEnv = System.getenv("WEBSOCKET_PORT");
        if (wsPortEnv != null) {
            wsPort = Integer.parseInt(wsPortEnv);
        }

        App webSocketServer = new App(wsPort);
        webSocketServer.start();
        System.out.println("WebSocket Server started on port: " + wsPort);
    }
}
