package uta.cse3310;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;


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
    
        // App Testing
        App A = new App(8070);
        assertTrue(A.getGameMap().size() == 5);
        assertTrue(A.getPlayerMap().size() == 0);
        assertTrue(A.getConnectionUserMap().isEmpty());

        // Player testing
        Player P = new Player("abcd", null);
        P.setInGameScore(0);
        P.incrementScore();
        P.setColorChoice("Blue");
        P.setGamesWon(1);
        assertTrue(P.getUsername() != null);
        assertTrue(P.getGamesWon() != 0);
        assertTrue(P.getInGameScore() == 1); // checks if score has been
        
        // Chat testing
        Chat C = new Chat();
        String message = "Yo";
        C.addMessage(message);
        assertTrue(C.getMessages().contains(message));

    }
}    
