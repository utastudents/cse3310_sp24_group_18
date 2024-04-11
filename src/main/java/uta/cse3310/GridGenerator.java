package uta.cse3310;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class GridGenerator {
    private static final int GRID_SIZE = 25; // Adjust for different grid sizes
    private static final char[][] grid = new char[GRID_SIZE][GRID_SIZE];
    private static final List<String> words = new ArrayList<>();
    private static final List<String> placedWords = new ArrayList<>();
    private static final boolean disguise = true;
    private static final Random random = new Random();

    private enum Orientation {
        HORIZONTAL, VERTICAL_UP, VERTICAL_DOWN, DIAGONAL_DOWN, DIAGONAL_UP
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        readWordsFromFile("words.txt");
        placeWordsInGridRandomly();
        fillEmptySpaces();
        saveGridAsJson(); // New method to save the grid as JSON
        displayGrid();
      

        long endTime = System.currentTimeMillis();
        System.out.println("\nTime taken to generate the grid: " + (endTime - startTime) / 1000.0 + " seconds");
        System.out.println("Grid dimension: " + GRID_SIZE + "x" + GRID_SIZE);
        System.out.println("Words placed in the grid: " + placedWords.size());

        System.out.print("\nPlaced Words: \n");
        for (String word : placedWords) {
            System.out.print(word + " ");
        }
        System.out.println();
    }

    private static void readWordsFromFile(String filePath) {
        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                words.add(scanner.nextLine().toUpperCase());
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        }
        Collections.shuffle(words); // Shuffle to ensure random selection
    }

    private static void placeWordsInGridRandomly() {
        int totalPlacements = GRID_SIZE * GRID_SIZE / 10; // Simplified calculation for word placements
        int placementsPerOrientation = (int) Math.ceil(totalPlacements * 0.15);

        for (Orientation orientation : Orientation.values()) {
            int placedCount = 0;
            for (String word : words) {
             orientation = Orientation.values()[random.nextInt(Orientation.values().length)];
                if (placedCount >= placementsPerOrientation) break;
                if (placeWord(word, orientation)) {
                    placedWords.add(word);
                    placedCount++;
                }
            }
        }
    }

    private static boolean placeWord(String word, Orientation orientation) {
        int attempts = 50; // Limit attempts to place a word to prevent infinite loops
        while (attempts-- > 0) {
            int x = random.nextInt(GRID_SIZE);
            int y = random.nextInt(GRID_SIZE);
            int dx = 0, dy = 0;

            switch (orientation) {
                case HORIZONTAL:
                    dx = 1;
                    break;
                case VERTICAL_UP:
                    dy = -1;
                    break;
                case VERTICAL_DOWN:
                    dy = 1;
                    break;
                case DIAGONAL_DOWN:
                    dx = 1;
                    dy = 1;
                    break;
                case DIAGONAL_UP:
                    dx = 1;
                    dy = -1;
                    break;
            }

            boolean fits = true;
            for (int i = 0; i < word.length(); i++) {
                int nx = x + i * dx;
                int ny = y + i * dy;
                if (nx < 0 || nx >= GRID_SIZE || ny < 0 || ny >= GRID_SIZE || grid[ny][nx] != '\u0000') {
                    fits = false;
                    break;
                }
            }

            if (fits) {
                for (int i = 0; i < word.length(); i++) {
                    int nx = x + i * dx;
                    int ny = y + i * dy;
                    grid[ny][nx] = word.charAt(i);
                }
                return true; // Word placed successfully
            }
        }
        return false; // Couldn't place the word
    }

    private static void fillEmptySpaces() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == '\u0000') {
                    char randomChar = (char) ('A' + random.nextInt(26));
                    grid[i][j] = disguise ? Character.toLowerCase(randomChar) : randomChar;
                }
            }
        }
    }

    private static void displayGrid() {
        for (char[] row : grid) {
            for (char c : row) {
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }

    private static void saveGridAsJson() {
        String json = gridToJson();
        saveJsonToFile(json, "grid.json");
    }

    private static String gridToJson() {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        for (int i = 0; i < GRID_SIZE; i++) {
            jsonBuilder.append("[\"");
            for (int j = 0; j < GRID_SIZE; j++) {
                jsonBuilder.append(grid[i][j]);
                if (j < GRID_SIZE - 1) {
                    jsonBuilder.append("\", \"");
                }
            }
            jsonBuilder.append("\"]");
            if (i < GRID_SIZE - 1) {
                jsonBuilder.append(",\n");
            }
        }
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }

    private static void saveJsonToFile(String json, String filename) {
        try (FileWriter file = new FileWriter(filename)) {
            file.write(json);
            System.out.println("Successfully saved JSON to " + filename);
        } catch (IOException e) {
            System.err.println("Failed to save JSON to file: " + e.getMessage());
        }
    }
}
