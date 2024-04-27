package uta.cse3310;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;


public class AppTest extends TestCase {

    public AppTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    
    public void testApp() {
    //     // Test case 1: Ensure playerMap is empty initially
    //     App app = new App(8080);
    //     assertTrue(app.getPlayerMap().isEmpty());

    //     // Test case 2: Ensure gameMap is empty initially
    //     assertTrue(app.getGameMap().isEmpty());

    //     // You can add more test cases here to test various functionalities of the App class
    }
}