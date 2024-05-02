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

import uta.cse3310.Game.GameEventListener;

public class App extends WebSocketServer implements GameEventListener {

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

    private void broadcastWinner(String message) {
        for (WebSocket conn : this.getConnections()) {
            conn.send("winner:" + message);
        }
    }

    @Override
    public void onGameFinished(String gameId) {
        Game game = gameMap.get(gameId);
        if (game != null) {
            Map<String, Integer> scores = game.getPlayerScores();
            String winner = scores.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (winner != null) {
                broadcastWinner(winner + " wins!");
            }
        }
    }

    ///// --------- GRID --------- /////

    private void broadcastGameRooms() {
        // Debug statement to log the broadcasting of game rooms
        System.out.println("broadcastGameRooms()_Broadcasting game rooms");
        List<Map<String, String>> gameRoomsInfo = new ArrayList<>();
    
        // Iterate through all game rooms to gather their information
        for (Map.Entry<String, Game> entry : gameMap.entrySet()) {
            Map<String, String> roomInfo = new HashMap<>();
            Game game = entry.getValue();
    
            // Add room name and player count
            roomInfo.put("name", entry.getKey()); // Room name (lobby name)
            String players = game.getCurrentNumberOfPlayers() + "/2";
            roomInfo.put("players", players);   
    
            // Include grid generation time in the info
            double gridTime = game.getGridGenerationTime();
            String formattedGridTime = String.format("%.4f", gridTime);
            roomInfo.put("gridGenerationTime", formattedGridTime);  // Append " seconds" for clarity
    
            // Add the room info to the list for broadcasting
            gameRoomsInfo.add(roomInfo);
    
            // Debug statement to show room details
            System.out.println("Room: " + entry.getKey() + " Players: " + players + " Grid Time: " + game.getGridGenerationTime());
        }
    
        // Convert the list of maps to a JSON string
        Gson gson = new Gson();
        String gameRoomsJson = gson.toJson(gameRoomsInfo);
    
        // Broadcast the game rooms JSON string to all connected clients
        for (WebSocket conn : this.getConnections()) {
            conn.send("update_gameRooms:" + gameRoomsJson);
            // Debug statement to confirm broadcasting to client
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
        gameMap.put("gameroom1", new Game("Room1", "gameroom1", this));
        gameMap.put("gameroom2", new Game("Room2", "gameroom2", this));
        gameMap.put("gameroom3", new Game("Room3", "gameroom3", this));
        gameMap.put("gameroom4", new Game("Room4", "gameroom4", this));
        gameMap.put("gameroom5", new Game("Room5", "gameroom5", this));
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
                        String gridJson = game.getGridAsJson();
                        String gridUpdateMessage = String.format("update_grid:%s:%s", gameRoomId, gridJson);
                        players.get(0).getWebSocket().send(gridUpdateMessage);
                        players.get(1).getWebSocket().send(gridUpdateMessage);

                        // game.startGame();
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
            String username = message.substring("new_player:".length());
            // Check if username already exists in the player map
            if (playerMap.containsKey(username)) {
                conn.send("username_exists"); // Notify the client that the username already exists
                System.out.println("Username already exists: " + username);
            } else {
                // Create a new player and add to the game
                Player player = new Player(username, conn);
                try {
                    playerMap.put(username, player); // Store new player information in playerMap
                    connectionUserMap.put(conn, username); // Map the connection to the username
                    broadcastGameRooms(); // Update the game rooms for all clients
                    broadcastPlayerList(); // Update the player list for all clients
                    conn.send("player_added:" + username); // Notify the client that the player was added successfully
                    System.out.println("New player added: " + username);
                } catch (Exception e) {
                    conn.send("player_not_added"); // Send error if the addition failed
                    System.out.println("Error adding player: " + username + "; " + e.getMessage());
                }
            }
        } else if (message.startsWith("check_word:")) {
            String[] parts = message.split(":");
            if (parts.length > 3) {
                String roomId = parts[1];
                String username = parts[2];
                String word = parts[3];
                Game game = gameMap.get(roomId);
                if (game != null && handleCheckWord(conn, roomId, username, word)) {
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
        } else if (message.startsWith("check_word:")) {
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
            } else {
                System.out.println("NULL FUNC");
            }
        }

    }

    private void broadcastUpdatedWords(String roomId) {
        Game game = gameMap.get(roomId);
        if (game != null) {
            Gson gson = new Gson();
            String wordsJson = gson.toJson(new ArrayList<>(game.getRemainingWords()));

            for (WebSocket conn : this.getConnections()) {
                conn.send("update_words:" + roomId + ":" + wordsJson);
            }
        }
    }

    // Method in App.java to broadcast highlighted cells
    public void broadcastHighlightUpdate(String roomId, String word, List<Integer[]> positions) {
        Gson gson = new Gson();
        String positionsJson = gson.toJson(positions);

        Game game = gameMap.get(roomId);
        if (game != null) {
            for (Player player : game.getPlayers()) {
                if (player != null && player.getWebSocket() != null && player.getWebSocket().isOpen()) {
                    player.getWebSocket().send("update_highlight:" + word + ":" + positionsJson);
                }
            }
        }
    }

    private boolean handleCheckWord(WebSocket conn, String roomId, String username, String word) {
        System.out.println("\n\nREACHED HANDLE CHECK WORD\n\n");
        Game game = gameMap.get(roomId);
        if (game != null) {
            List<Integer[]> positions = game.checkWordAndGetPositions(username, word);
            if (positions != null) {
                System.out.println("\n-----WORD FOUND----\n" + "WORD CHECKED : " + word);

                Gson gson = new Gson();
                String positionsJson = gson.toJson(positions);
                conn.send("word_correct:" + word + ":" + positionsJson);

                game.printWordsFoundByUser(username);
                broadcastUpdatedWords(roomId); // New function to broadcast updated words list
                broadcastHighlightUpdate(roomId, word, positions);

                return true;
            } else {
                System.out.println("\n-----WORD INCORRECT----\n" + "WORD CHECKED : " + word);
                conn.send("word_incorrect:" + word);
                return false;
            }
        } else {
            conn.send("error:Game not found");
            return false;
        }
    }

    public void printPlayerWordCounts() {
        for (Game game : gameMap.values()) {
            System.out.println("Game Lobby: " + game.getLobbyName() + " Word Counts:");
            game.printAllWordsAndTheirStatus();

        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New WebSocket connection: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
        conn.send("section0");
    }

    

    private void broadcastChatMessage(String roomID, String message) {
        Game game = findGameByRoomID(roomID);
        if (game != null) {
            // Retrieve the players from the game room
            Player[] players = game.getPlayers_chat();
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
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out
                .println("WebSocket connection closed: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());

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
            handlePlayerDisconnection(conn);
            System.out.println("\n--PLAYER REMOVED--\nPlayer removed (Tab Closed): " + errorUsername + "\n\n");
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
            handlePlayerDisconnection(conn);
            System.out.println("\n--PLAYER REMOVED--\nPlayer removed (Tab Closed): " + errorUsername + "\n\n");
            broadcastPlayerList();
            System.out.println("On Disconnect : ");
            ;
            broadcastGameRooms();
        }
    
        System.out.println("On Error : ");
        ;
        broadcastGameRooms();
    
    }
    
    
    private void handlePlayerDisconnection(WebSocket conn) {
        String username = connectionUserMap.remove(conn);
        if (username != null && playerMap.containsKey(username)) {
            Player player = playerMap.remove(username);
            removePlayerFromGame(player);
            System.out.println("Player " + username + " has been removed due to disconnection.");
        }
    }
private void removePlayerFromGame(Player player) {
    for (Game game : gameMap.values()) {
        if (game.getPlayer1() != null && game.getPlayer1().equals(player)) {
            game.setPlayer1(null);
            System.out.println("Player1 (" + player.getUsername() + ") removed from Game Room: " + game.getGameRoomId());
        }
        if (game.getPlayer2() != null && game.getPlayer2().equals(player)) {
            game.setPlayer2(null);
            System.out.println("Player2 (" + player.getUsername() + ") removed from Game Room: " + game.getGameRoomId());
        }
    }
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
        } else {
            System.out.println("Game not found");
        }
    }

    public Map getGameMap()
    {
        return gameMap;
    }
    public void setGameMap(Map gameMap)
    {
        this.gameMap = gameMap;
    }

    public Map getPlayerMap()
    {
        return playerMap;
    }
    public void setPlayerMap(Map playerMap)
    {
        this.playerMap = playerMap;
    }

    public Map getConnectionUserMap()
    {
        return connectionUserMap;
    }
    public void setConnectionUserMap(Map connectionUserMap)
    {
        this.connectionUserMap = connectionUserMap;
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
