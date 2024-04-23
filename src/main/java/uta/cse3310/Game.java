package uta.cse3310;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;

public class Game {
    private Player player1;
    private Player player2;
    private boolean isFinished;
    private String lobbyName;
    private String roomId;
    private Chat chat;


    private static final int GRID_SIZE = 20;
    private static List<String> allWords = new ArrayList<>(); // List of all words in the game
    private char[][] grid; // The grid of the game
    private Map<String, Boolean> wordsPlaced;
    private static Random random = new Random();


    // Maintain the original naming convention
    private Map<String, Boolean> wordsFound = new HashMap<>();
    private Map<String, List<String>> wordsFoundByPlayer = new HashMap<>();

    public Game(String lobbyName, String roomId) {
        this.lobbyName = lobbyName;
        this.roomId = roomId;
        this.player1 = null;
        this.player2 = null;
        this.isFinished = false;
        this.grid = new char[GRID_SIZE][GRID_SIZE];
        this.wordsPlaced = new HashMap<>();
        loadWords(); // Load words specific to this game instance
        System.out.println("Creating game with lobby name: " + lobbyName + " and room id: " + roomId);
        initializeGrid();
        placeWords();
        System.out.println("Grid for [game: " + lobbyName + " room: " + roomId);
        printGrid();
        this.chat = new Chat(); // Initialize a new Chat object for this game

    }

    public List<String> checkWords(String username, String[] words) {
    List<String> foundWords = new ArrayList<>();
    for (String word : words) {
        if (checkWord(username, word)) {
            foundWords.add(word);
        }
    }
    return foundWords;
}

private boolean checkWord(String username, String word) {
    // Implement your logic here to check if the word is valid for the given username
    // You can use the existing method markWordAsFound to mark the word as found
    // For example:
    // if (isValidWordForUser(username, word)) {
    //     markWordAsFound(word, username);
    //     return true;
    // } else {
    //     return false;
    // }

    // For demonstration purposes, let's assume all words are valid for any user
    markWordAsFound(word, username);
    return true;
}
     // Method to mark a word as found
    public void markWordAsFound(String word, String username) {
        if (wordsFound.containsKey(word) && !wordsFound.get(word)) { // Check if word is placed and not yet marked as found
            wordsFound.put(word, true); // Mark the word as found
            if (!wordsFoundByPlayer.containsKey(username)) {
                wordsFoundByPlayer.put(username, new ArrayList<>()); // Create a new list for this user if not exist
            }
            wordsFoundByPlayer.get(username).add(word); // Add the word to the user's found list
            System.out.println(username + " found the word: " + word);
        }
    }

    // Retrieve the list of words found by a specific player
    public List<String> getWordsFoundByPlayer(String username) {
        return wordsFoundByPlayer.getOrDefault(username, new ArrayList<>());
    }

    // Debugging method to print words found by each player
    public void printWordsFound() {
        for (Map.Entry<String, List<String>> entry : wordsFoundByPlayer.entrySet()) {
            System.out.println("User: " + entry.getKey() + ", Words Found: " + entry.getValue());
        }
    }

    // Retrieve the list of all words that have been placed and their found status
    public List<String> getPlacedWords() {
        return new ArrayList<>(wordsFound.keySet());
    }

    // Check if a specific word is placed in the game and has been found
    public boolean isWordPlacedAndFound(String word) {
        return wordsFound.containsKey(word) && wordsFound.get(word);
    }



    public void reset() {
        // Reset the game's properties
        System.out.println("Resetting game in lobby: " + lobbyName);
        this.player1 = null;
        this.player2 = null;
        this.isFinished = false;
        this.wordsPlaced.clear();
        initializeGrid();
        placeWords();
        System.out.println("Game reset in lobby: " + lobbyName);
    }
    

    // CHAT
    public void addChatMessage(String message) {
        this.chat.addMessage(message);
    }

    public List<String> getChatMessages() {
        return this.chat.getMessages();
    }

    public Chat getChat() {
        return chat;
    }


    public void sendGameDetails(WebSocket conn) {
        Gson gson = new Gson();
        //String gridJson = getGridAsJson(); // Get JSON representation of the game grid
        List<String> placedWords = new ArrayList<>(wordsPlaced.keySet()); // Get list of placed words
        String wordsJson = gson.toJson(placedWords); // Convert placed words list to JSON

        System.out.println("Sending game details to client: " + conn.getRemoteSocketAddress());
        try {
            //conn.send("update_grid:" + this.roomId + ":" + gridJson);
            System.out.println("Sent!");
        } catch (Exception e) {
            System.out.println("Error sending grid to client: " + e.getMessage());
        }

        System.out.println("Sending words to client: " + conn.getRemoteSocketAddress());
        try{
            conn.send("update_words:" + this.roomId + ":" + wordsJson);
            System.out.println("Sent!");
        } catch (Exception e) {
            System.out.println("Error sending words to client: " + e.getMessage());
        }
    }

    private void initializeGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            Arrays.fill(grid[i], '_');
        }
    }

    // Attempts to place all words into the grid
    private void placeWords() {
        for (String word : allWords) {
            boolean placed = false;
            int attempts = 0;
            while (!placed && attempts < 100) {
                placed = placeWordInGrid(word);
                attempts++;
            }
            if (placed) {
                wordsPlaced.put(word, false);
            }
        }
    }

    // Attempts to place a single word in the grid randomly
    private boolean placeWordInGrid(String word) {
        int orientation = random.nextInt(2); // 0 for horizontal, 1 for vertical
        for (int attempts = 0; attempts < 100; attempts++) {
            int row = random.nextInt(GRID_SIZE);
            int col = random.nextInt(GRID_SIZE);
            if (canPlaceWord(word, row, col, orientation)) {
                for (int i = 0; i < word.length(); i++) {
                    grid[row + (orientation == 1 ? i : 0)][col + (orientation == 0 ? i : 0)] = word.charAt(i);
                }
                return true;
            }
        }
        return false;
    }

    // Checks if a word can be placed at the specified position
    private boolean canPlaceWord(String word, int row, int col, int orientation) {
        for (int i = 0; i < word.length(); i++) {
            int newRow = row + (orientation == 1 ? i : 0);
            int newCol = col + (orientation == 0 ? i : 0);
            if (newRow >= GRID_SIZE || newCol >= GRID_SIZE || grid[newRow][newCol] != '_') {
                return false;
            }
        }
        return true;
    }

    // Prints the current state of the grid
    public void printGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }

    // Loads the words from a text file into the allWords list
    public void loadWords() {
        try {
            allWords = Files.readAllLines(Paths.get("words.txt"));
            System.out.println("Words loaded successfully");
        } catch (IOException e) {
            System.err.println("Error loading words: " + e.getMessage());
        }
    }

    // Converts the grid to a JSON string format
    public String getGridAsJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < GRID_SIZE; i++) {
            sb.append("[");
            for (int j = 0; j < GRID_SIZE; j++) {
                sb.append("\"").append(grid[i][j]).append("\"");
                if (j < GRID_SIZE - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            if (i < GRID_SIZE - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    // Prints the words that were successfully placed in the grid
    public void printWords() {
        for (String word : wordsPlaced.keySet()) {
            System.out.println("Placed word: " + word);
        }
    }

    // Method to add a player to the game
    public boolean addPlayer(Player player) {
        if (player1 == null) {
            player1 = player;
            return true; // Player added successfully
        } else if (player2 == null) {
            player2 = player;
            return true; // Player added successfully
        } else {
            return false; // Game is already full
        }
    }

    // Check if the game is ready to start
    public boolean isReadyToStart() {
        return player1 != null && player2 != null;
    }

    // Getters and setters
    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public boolean isFull() {
        return player1 != null && player2 != null;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public String roomID() {
        return roomId;
    }

    public void startGame() {
        // Start the game
    }

    public String getCurrentNumberOfPlayers() {
        int count = 0;
        if (player1 != null) {
            count++;
        }
        if (player2 != null) {
            count++;
        }
        return count + ""; // "" is added to convert int to string
    }

    // print players in all game
    public void printPlayers() {
        // Check if player1 is not null and print the player's name
        if (player1 != null) {
            System.out.println("Player 1: " + player1.getUsername());
        } else {
            System.out.println("Player 1: [Empty]");
        }

        // Check if player2 is not null and print the player's name
        if (player2 != null) {
            System.out.println("Player 2: " + player2.getUsername());
        } else {
            System.out.println("Player 2: [Empty]");
        }
    }

    // store the players list, used to redirect to the game page
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        if (player1 != null)
            players.add(player1);
        if (player2 != null)
            players.add(player2);
        return players;
    }

    public String getGameRoomId() {
        return roomId;
    }

    public Player[] getPlayers_chat() {
        return new Player[] {player1, player2};
      }

    // Other methods as needed...
}
