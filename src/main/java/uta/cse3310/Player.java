package uta.cse3310;

import org.java_websocket.WebSocket; 


public class Player {
    private String username;
    private String colorChoice; // Default or chosen color
    private int gamesWon;
    private int gamesLost;
    private int inGameScore;
    private WebSocket webSocket;

    public Player(String username, WebSocket webSocket) {
        this.username = username;
        this.colorChoice = "Default"; 
        this.gamesWon = 0;
        this.gamesLost = 0;
        this.inGameScore = 0;
        this.webSocket = webSocket;

    }
    public void incrementScore() {
        this.inGameScore += 1;
    }
    

    // Getters and Setters for the webSocket...
    public WebSocket getWebSocket() {
        return webSocket;
    }

    public void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }
    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getColorChoice() {
        return colorChoice;
    }

    public void setColorChoice(String colorChoice) {
        this.colorChoice = colorChoice;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public int getGamesLost() {
        return gamesLost;
    }

    public void setGamesLost(int gamesLost) {
        this.gamesLost = gamesLost;
    }

    public int getInGameScore() {
        return inGameScore;
    }

    public void setInGameScore(int inGameScore) {
        this.inGameScore = inGameScore;
    }

}