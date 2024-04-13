// Import necessary classes from the TooTallNate WebSocket library
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;

// Define the main class for the WebSocket server
public class App {
    // Main method that starts the WebSocket server
    public static void main(String[] args) {
        int port = 4070; // Specify the desired port number
        
        // Create a WebSocketServer instance with the desired port
        WebSocketServer server = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                // Method called when a client connects to the server
                System.out.println("Player connected: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                // Method called when a client disconnects from the server
                System.out.println("Player disconnected: " + conn.getRemoteSocketAddress()); // Also display the username Here
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                // Method called when a message is received from a client
                System.out.println("Received message from client: " + message);

                // If the received message is "bark", create a Dog object and send back a response
                if ("bark".equals(message)) {
                    Game G = new Game();
                    String Game_test_response = G.test();
                    conn.send(Game_test_response);
                }
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                // Method called when an error occurs
                ex.printStackTrace();
            }

            @Override
            public void onStart() {
                // Method called when the server starts
                System.out.println("WebSocket server started at ws://localhost:" + port + "/websocket");
            }
        };

        // Start the WebSocket server
        server.start();
    }
}
