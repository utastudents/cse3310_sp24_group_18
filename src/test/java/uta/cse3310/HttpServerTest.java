package uta.cse3310;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class HttpServerTest extends TestCase {

        /**
     * Create the test case
     *
     * @param testName name of the test case
     */

    public HttpServerTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */

    public static Test suite() {
        return new TestSuite(HttpServerTest.class);
    }

    
    public void testStart() {
        // Create a HttpServer
        int port = 8080;
        String dirname = "src/main/webapp/html";
        HttpServer httpServer = new HttpServer(port, dirname);

        httpServer.start();
        
        assertTrue(isServerRunning(8080));
    }
    
    private boolean isServerRunning(int port) {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress("localhost", port));
            return true; // Server is running
        } catch (IOException e) {
            return false; // Server is not running
        }
    }
}