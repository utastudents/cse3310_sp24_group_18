import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.junit.Test;

import uta.cse3310.HttpServer;

public class HttpServerTest {

    @Test
    public void testStart() throws IOException, InterruptedException {
        // Create a HttpServer
        int port = 8080;
        String dirname = "src/main/webapp/html";
        HttpServer httpServer = new HttpServer(port, dirname);

        // Start the server in a separate thread
        Thread serverThread = new Thread(() -> {
            httpServer.start();
        });
        serverThread.start();

        // Wait for the server to start (give it some time)
        Thread.sleep(1000);

        // Check if the server is running by connecting to it
        boolean isServerRunning = isServerRunning(port);



        // Print the result or throw an exception based on the server's running status
        if (isServerRunning) {
            System.out.println("Server started successfully.");
        } else {
            throw new AssertionError("Server failed to start.");
        }
    }

    private boolean isServerRunning(int port) {
        try (SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", port))) {
            // If the connection is successful, the server is running
            return true;
        } catch (IOException e) {
            // If an exception occurs, the server is not running
            return false;
        }
    }
}