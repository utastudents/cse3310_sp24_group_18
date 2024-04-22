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

public class Game {
    private Player player1;
    private Player player2;
    private boolean isFinished;
    private String lobbyName;
    private String roomId;

    private static final int GRID_SIZE = 20;
    private static List<String> allWords = new ArrayList<>(); // List of all words in the game
    private char[][] grid; // The grid of the game
    private Map<String, Boolean> wordsFound;
    private static Random random = new Random();

    public Game(String lobbyName, String roomId) {
        this.lobbyName = lobbyName;
        this.roomId = roomId;
        this.player1 = null;
        this.player2 = null;
        this.isFinished = false;
        this.grid = new char[GRID_SIZE][GRID_SIZE];
        this.wordsFound = new HashMap<>();
        System.out.println("Creating game with lobby name: " + lobbyName);

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
                wordsFound.put(word, false);
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
    private static void loadWords() {
        try {
            allWords = Files.readAllLines(Paths.get("words.txt"));
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
        for (String word : wordsFound.keySet()) {
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

    //store the players list, used to redirect to the game page
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        if (player1 != null) players.add(player1);
        if (player2 != null) players.add(player2);
        return players;
    }

    public String getGameRoomId() {
        return roomId;
    }
    
    

    // Other methods as needed...
}
