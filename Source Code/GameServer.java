import java.net.ServerSocket;

public class GameServer {
    private ServerSocket serverSocket;
    private GameEngine gameEngine;

    public void start() {
        // Start the server
    }

    public void stopServer() {
        // Stop the server
    }
    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        gameServer.start(); // Start the server when the program is executed
    }
}