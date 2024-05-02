package uta.cse3310;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;

public class Game {
    private Player player1;
    private Player player2;
    private boolean isFinished;
    private String lobbyName;
    private String roomId;
    private Chat chat;
    private GameEventListener listener;
    private String gameId;
    private long gridGenerationTime; // Added to measure the time to generate the grid

    private static final int GRID_SIZE = 25;
    private static List<String> allWords = new ArrayList<>(); // List of all words in the game
    private char[][] grid; // The grid of the game
    private Map<String, Boolean> wordsPlaced;
    private Set<Integer> placedLetters = new HashSet<>();
    private static Random random = new Random();

    private ArrayList<String> wordsFound = new ArrayList<String>(); // Create an ArrayList object
    private Map<String, List<String>> wordsFoundByPlayer = new HashMap<>();
    private Map<String, List<Integer[]>> wordPositions = new HashMap<>();

    public Game(String lobbyName, String roomId, GameEventListener listener) {
        this.lobbyName = lobbyName;
        this.roomId = roomId;
        this.gameId = roomId;
        this.player1 = null;
        this.player2 = null;
        this.isFinished = false;
        this.listener = listener;

        this.grid = new char[GRID_SIZE][GRID_SIZE];
        this.wordsPlaced = new HashMap<>();
        loadWords(); // Load words specific to this game instance
        System.out.println("Creating game with lobby name: " + lobbyName + " and room id: " + roomId);
        this.chat = new Chat(); // Initialize a new Chat object for this game
        startGame();

    }
    

    private void checkForWinner() {
        if (areAllWordsFound()) {
            if (listener != null) {
                listener.onGameFinished(gameId);
            }
        }
    }

    public boolean isWordCorrect(String word) {
        Boolean status = wordsPlaced.get(word);
        return status != null && status; // True if the word is correct and not yet marked as found
    }

    public Set<String> getRemainingWords() {
        return wordsPlaced.entrySet().stream()
                .filter(Map.Entry::getValue) // Only include words that are still available (not found)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public List<Integer[]> checkWordAndGetPositions(String username, String word) {
        if (isWordCorrect(word)) {
            wordsPlaced.put(word, false); // Mark the word as found
            wordsFound.add(word);
            wordsFoundByPlayer.computeIfAbsent(username, k -> new ArrayList<>()).add(word);

            List<Integer[]> positions = wordPositions.get(word);
            // Add logging here to debug positions
            if (positions != null) {
                System.out.println("Word found: " + word);
                System.out.print("Positions: ");
                for (Integer[] pos : positions) {
                    System.out.print(Arrays.toString(pos) + " ");
                }
                System.out.println(); // New line for clear logging
            } else {
                System.out.println("Positions not found for word: " + word);
            }

            return positions; // Return positions if the word is correct
        } else {
            System.out.println("Word incorrect or not found: " + word);
            return null; // Return null if the word is not correct
        }
    }

    public boolean checkWord(String username, String word) {
        if (isWordCorrect(word)) { // Use the new method to check word correctness
            wordsPlaced.put(word, false); // Mark the word as found
            wordsFound.add(word);
            wordsFoundByPlayer.computeIfAbsent(username, k -> new ArrayList<>()).add(word);
            List<Integer[]> positions = wordPositions.get(word);
            return true;
        }
        return false;
    }

    public void printWordsFoundByUser(String username) {
        List<String> foundWords = wordsFoundByPlayer.getOrDefault(username, new ArrayList<>());
        System.out.println(username + " has found: " + foundWords);
    }

    public void printAllWordsAndTheirStatus() {
        for (Map.Entry<String, Boolean> entry : wordsPlaced.entrySet()) {
            System.out.println("Word: " + entry.getKey() + ", Available: " + entry.getValue());
        }
    }

    public interface GameEventListener {
        void onGameFinished(String gameId);
    }

    private boolean areAllWordsFound() {
        // Check if all words are found by checking the size of the remaining words set
        return getRemainingWords().isEmpty();
    }

    // USER SCORE
    public Map<String, Integer> getPlayerScores() {
        Map<String, Integer> scores = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : wordsFoundByPlayer.entrySet()) {
            scores.put(entry.getKey(), entry.getValue().size());
        }
        return scores;
    }

    public void reset() {
        // Reset the game's properties
        System.out.println("Resetting game in lobby: " + lobbyName);

        // Reset the game's properties and states for a new game
        for (Map.Entry<String, Boolean> entry : wordsPlaced.entrySet()) {
            entry.setValue(false); // Reset each word to not found
        }
        wordsFound.clear();
        wordsFoundByPlayer.clear();
        this.player1 = null;
        this.player2 = null;
        this.isFinished = false;
        this.wordsPlaced.clear();
        initializeGrid();
        placeWords();
        fillRemainingCells();
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
        String gridJson = getGridAsJson(); // Serialize the grid to JSON
        String wordsJson = gson.toJson(new ArrayList<>(wordsPlaced.keySet()));

        try {
            conn.send("update_grid:" + roomId + ":" + gridJson); // Send grid
            conn.send("update_words:" + roomId + ":" + wordsJson); // Send words
        } catch (Exception e) {
            System.err.println("Error sending game details: " + e.getMessage());
        }
    }

    private void initializeGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            Arrays.fill(grid[i], '_'); // Initialize all cells to '_'
        }
    }

    private void fillRemainingCells() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == '_') { // Only fill cells that have not been filled with a word
                    grid[i][j] = (char) ('A' + random.nextInt(26));
                }
                // For Saad working on Test, Uncomment this and comment the upper for debug.. to
                // see the grid without random words
                // if (grid[i][j] == '3') { // Only fill cells that have not been filled with a
                // word
                // grid[i][j] = (char) ('A' + random.nextInt(26));
                // }
            }
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
                wordsPlaced.put(word, true);
            }
        }

        // DEBUG
        // System.out.println("IN placewords()");
        // for (Map.Entry<String, Boolean> entry : wordsPlaced.entrySet()) {
        // System.out.println("\"" + entry.getKey() + "\":" + entry.getValue());
        // }
    }

    public double calculateWordDensity() {
        int totalCells = GRID_SIZE * GRID_SIZE;
        int wordCells = placedLetters.size(); // The set size gives us the number of unique cells with words

        double density = (double) wordCells / totalCells;
        System.out.printf("Density of valid words in the grid: %.5f\n", density);
        return density;
    }

    public int getRowIncrement(int orientation, int i) {
        switch (orientation) {
            case 0: // Horizontal right
            case 2: // Horizontal left
                return 0;
            case 1: // Vertical down
            case 7: // Diagonal right down
            case 5: // Diagonal left down
                return i;
            case 3: // Vertical up
            case 6: // Diagonal right up
            case 4: // Diagonal left up
                return -i;
            default:
                return 0;
        }
    }

    public int getColIncrement(int orientation, int i) {
        switch (orientation) {
            case 0: // Horizontal right
            case 7: // Diagonal right down
            case 6: // Diagonal right up
                return i;
            case 1: // Vertical down
            case 3: // Vertical up
                return 0;
            case 2: // Horizontal left
            case 5: // Diagonal left down
            case 4: // Diagonal left up
                return -i;
            default:
                return 0;
        }
    }

    // Attempts to place a single word in the grid randomly
    public boolean placeWordInGrid(String word) {
        int orientation = random.nextInt(8);
        List<Integer[]> positions = new ArrayList<>();
        for (int attempts = 0; attempts < 100; attempts++) {
            int row = random.nextInt(GRID_SIZE);
            int col = random.nextInt(GRID_SIZE);
            if (canPlaceWord(word, row, col, orientation)) {
                for (int i = 0; i < word.length(); i++) {
                    int newRow = row + getRowIncrement(orientation, i);
                    int newCol = col + getColIncrement(orientation, i);
                    grid[newRow][newCol] = word.charAt(i);
                    positions.add(new Integer[] { newRow, newCol });
                    placedLetters.add(newRow * GRID_SIZE + newCol); // Storing the index as a single integer
                }
                wordPositions.put(word, positions);
                return true;
            }
        }
        return false;
    }

    // Checks if a word can be placed at the specified position
    private boolean canPlaceWord(String word, int row, int col, int orientation) {
        for (int i = 0; i < word.length(); i++) {
            int newRow = row + getRowIncrement(orientation, i);
            int newCol = col + getColIncrement(orientation, i);
            if (newRow < 0 || newRow >= GRID_SIZE || newCol < 0 || newCol >= GRID_SIZE || grid[newRow][newCol] != '_') {
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

    // ensures a balanced and random selection from all parts of the list
    private List<String> structuredShuffle(List<String> words, int segments) {
        List<String> shuffled = new ArrayList<>(); // This will hold the shuffled list of words.
        int segmentSize = words.size() / segments; // Calculate how many words each segment will contain.

        // Set to track which indices have been added to the shuffled list to avoid
        // duplicating words.
        Set<Integer> addedIndices = new HashSet<>();

        // Continue looping until the shuffled list contains all words from the original
        // list.
        while (shuffled.size() < words.size()) {
            for (int i = 0; i < segments; i++) {
                int start = i * segmentSize; // Calculate the start index of the current segment.
                int end = (i + 1) * segmentSize; // Calculate the end index of the current segment.
                if (i == segments - 1) {
                    end = words.size(); // Ensure the last segment includes all remaining words.
                }

                // Generate a random index within the current segment to pick a word.
                int randomIndex = start + random.nextInt(end - start);
                // Check if the index has already been used to prevent duplicate selections.
                if (!addedIndices.contains(randomIndex)) {
                    shuffled.add(words.get(randomIndex)); // Add the selected word to the shuffled list.
                    addedIndices.add(randomIndex); // Mark this index as used.
                }

                // Break out of the loop if all words are already added to the shuffled list.
                if (shuffled.size() >= words.size()) {
                    break;
                }
            }
        }

        return shuffled; // Return the uniformly shuffled list of words.
    }

    // Loads the words from a text file into the allWords list
    public void loadWords() {
        try {
            List<String> loadedWords = Files.readAllLines(Paths.get("words.txt")).stream()
                    .map(String::toUpperCase) // Convert each word to uppercase
                    .filter(word -> word.length() > 3) // Filter out words that are too short
                    .collect(Collectors.toList());

            // New method to shuffle words by picking from different segments
            allWords = structuredShuffle(loadedWords, 10); // Shuffle with desired number of segments

            System.out.println("Words loaded successfully. Total words read in words.txt: " + allWords.size());
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
        System.out.println("----------PLACED WORDS-------------");
        for (String word : wordsPlaced.keySet()) {
            System.out.print(word + " ");
        }

        // print the count of words
        System.out.println("\n\nTotal words: " + wordsPlaced.size());
        System.out.println("------------------------------------\n\n");

        // Debug
        // System.out.println("\nDEBUG : \n"+wordsPlaced.get("Buggati"));
        // System.out.println("\n------------------------------------\n");
        // for (Map.Entry<String, Boolean> entry : wordsPlaced.entrySet()) {
        // System.out.println("\"" + entry.getKey() + "\":" + entry.getValue());
        // }

    }

    // Method to add a player to the game
    public boolean addPlayer(Player player) {
        if (player1 == null) {
            player1 = player;
            return true; // Player added successfully
        } else if (player2 == null) {
            if (player1.getUsername().equals(player.getUsername())) {
                return false; // Prevent adding a player with the same username as player1
            }
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

    public long getGridGenerationTime() {
        return gridGenerationTime; // Getter for grid generation time
    }

    public void startGame() {
        long startTime = System.currentTimeMillis(); // Start timing
        initializeGrid(); // first fill with dashes
        placeWords(); // then place words
        fillRemainingCells(); // then fill the dahses with the remaining letters
        gridGenerationTime = (long) ((System.currentTimeMillis() - startTime) / 1000.0); // Calculate elapsed time in
                                                                                         // seconds
        printGrid();
        printWords();
        printGameHealth();
        System.out.println("\n\n\n");
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
        return new Player[] { player1, player2 };
    }

    public Map<String, Boolean> getWordsPlaced() {
        return wordsPlaced;
    }

    public void printGameHealth() {
        calculateWordDensity();  // Already implemented to print grid density
    
        Map<Integer, Integer> orientationCounts = new HashMap<>();
        int totalWords = 0;
        for (List<Integer[]> positions : wordPositions.values()) {
            Integer[] firstPos = positions.get(0);
            Integer[] lastPos = positions.get(positions.size() - 1);
            int orientation = determineOrientation(firstPos, lastPos);
            orientationCounts.put(orientation, orientationCounts.getOrDefault(orientation, 0) + 1);
            totalWords++;
        }
    
        // Print orientation frequencies
        System.out.printf("Horizontal Orientation: %.17f\n", 
            (double) (orientationCounts.getOrDefault(0, 0) + orientationCounts.getOrDefault(2, 0)) / totalWords);
        System.out.printf("VerticalUp Orientation: %.17f\n", 
            (double) orientationCounts.getOrDefault(3, 0) / totalWords);
        System.out.printf("VerticalDown Orientation: %.17f\n", 
            (double) orientationCounts.getOrDefault(1, 0) / totalWords);
        System.out.printf("DiagonalUp Orientation: %.17f\n", 
            (double) (orientationCounts.getOrDefault(4, 0) + orientationCounts.getOrDefault(6, 0)) / totalWords);
        System.out.printf("DiagonalDown Orientation: %.17f\n", 
            (double) (orientationCounts.getOrDefault(5, 0) + orientationCounts.getOrDefault(7, 0)) / totalWords);
    
        // Calculate frequency of letters
        int[] letterCounts = new int[26];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                letterCounts[grid[i][j] - 'A']++;
            }
        }
    
        System.out.print("Random Letter Frequency (A-Z): ");
        for (int count : letterCounts) {
            System.out.print(count + ",");
        }
        System.out.println();
    
        // Print the board formation time
        System.out.println("Grid Generation Time: " + getGridGenerationTime() + " s");
    
    }
    
    private int determineOrientation(Integer[] firstPos, Integer[] lastPos) {
        // Calculate orientation based on position change
        int rowDiff = lastPos[0] - firstPos[0];
        int colDiff = lastPos[1] - firstPos[1];
        if (rowDiff == 0 && colDiff > 0) return 0; // Horizontal right
        if (rowDiff > 0 && colDiff == 0) return 1; // Vertical down
        if (rowDiff == 0 && colDiff < 0) return 2; // Horizontal left
        if (rowDiff < 0 && colDiff == 0) return 3; // Vertical up
        if (rowDiff < 0 && colDiff < 0) return 4; // Diagonal left up
        if (rowDiff > 0 && colDiff < 0) return 5; // Diagonal left down
        if (rowDiff < 0 && colDiff > 0) return 6; // Diagonal right up
        if (rowDiff > 0 && colDiff > 0) return 7; // Diagonal right down
        return -1; // Should not occur
    }
    

    
}
