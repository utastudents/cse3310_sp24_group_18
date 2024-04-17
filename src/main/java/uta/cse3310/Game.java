package uta.cse3310;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.java_websocket.WebSocket;

public class Game {
    private static final int GRID_SIZE = 20;
    private static List<String> allWords = new ArrayList<>();
    private char[][] grid;
    private Map<String, Boolean> wordsFound;
    private static Random random = new Random();

    public Game() {
        this.grid = new char[GRID_SIZE][GRID_SIZE];
        this.wordsFound = new HashMap<>();
        initializeGrid();
        placeWords();
        printWords();

    }

    public String getPlacedWordsAsJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean isFirst = true;
        for (String word : wordsFound.keySet()) {
            if (!isFirst) {
                sb.append(",");
            }
            sb.append("\"").append(word).append("\"");
            isFirst = false;
        }
        sb.append("]");
        return sb.toString();
    }

    // Print all the words that are placed in the grid
    public void printWords() {
        for (String word : wordsFound.keySet()) {
            System.out.println(word);
        }
    }

    // Load words from file into allWords list
    public static void loadWords() {
        System.out.println("Loading words from file...");
        try {
            allWords = Files.readAllLines(Paths.get("words.txt"));
        } catch (IOException e) {
            System.err.println("Error reading words.txt: " + e.getMessage());
        }
    }

    private void initializeGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            Arrays.fill(grid[i], '_');
        }
    }
    private void placeWords() {
        // Assuming you have a method to place words correctly
        // This is a simplified version. Implement word placement logic based on your needs.
        int maxAttempts = 10;
        for (String word : allWords) {
            boolean placed = false;
            for (int attempt = 0; attempt < maxAttempts && !placed; attempt++) {
                placed = placeWordInGrid(word);
            }
            if (placed) {
                wordsFound.put(word, false); // Word placed but not found
            }
        }
    }

    private boolean placeWordInGrid(String word) {
        int orientation = random.nextInt(2); // 0 for horizontal, 1 for vertical
        int attempts = 0;

        while (attempts < 100) {
            int row = random.nextInt(GRID_SIZE);
            int col = random.nextInt(GRID_SIZE);

            if (canPlaceWord(word, row, col, orientation)) {
                for (int i = 0; i < word.length(); i++) {
                    grid[row + (orientation == 1 ? i : 0)][col + (orientation == 0 ? i : 0)] = word.charAt(i);
                }
                return true;
            }
            attempts++;
        }
        return false;
    }

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

    public void printGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }
    public static void main(String[] args) {
        loadWords(); // Load words before creating a new Game instance
        Game game = new Game();
        game.printGrid();
    }

    public void handleMessage(String message, WebSocket conn) {
        try {
            if (message.equals("request_grid")) {
                conn.send(getGridAsJson()); // Send the grid as JSON
            } else if (message.startsWith("check_word:")) {
                String word = message.substring("check_word:".length()).trim();
                Boolean found = wordsFound.get(word);
                if (found != null) {
                    conn.send("{\"word\":\"" + word + "\", \"found\":" + found + "}");
                } else {
                    conn.send("{\"error\":\"Word not found in game.\"}");
                }
            } else {
                conn.send("{\"error\":\"Unknown command.\"}");
            }
        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
            conn.send("{\"error\":\"Error processing your request.\"}");
        }
    }
    

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
    
}
