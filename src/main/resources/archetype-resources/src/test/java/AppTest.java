package uta.cse3310;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


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

        App A = new App(8070);

        Player P = new Player("abcd", null);
        P.setInGameScore(0);
        P.incrementScore();
        assertTrue(P.getInGameScore() != 0);

        Chat C = new Chat();
        String message = "Yo";
        C.addMessage(message);
        assertTrue(C.getMessages().contains(message));

    }
}   
