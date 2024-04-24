package uta.cse3310;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class GameTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public GameTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(GameTest.class);
    }

    public void testCase(){
    Game g = new Game("TestLobby", "TestRoom");
    Player p1 = new Player("josh", null);
    Player p2 = new Player("adam", null);

    g.addPlayer(p1);
    g.addPlayer(p1);
    g.setPlayer1(p1);
    g.setPlayer2(p2);

    assertTrue(g.isReadyToStart());
    assertEquals(g.getCurrentNumberOfPlayers(),"2");
    }
}
