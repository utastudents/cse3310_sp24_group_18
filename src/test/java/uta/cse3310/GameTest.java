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

    Game g = new Game("TestLobby", "TestRoom"); // New test Room
    Player p1 = new Player("josh", null); // new player named josh
    Player p2 = new Player("adam", null); // new player named adam

        //adding players to game
    g.addPlayer(p1);
    g.addPlayer(p1);
    g.setPlayer1(p1);
    g.setPlayer2(p2);
    String players = String.valueOf(g.getPlayers());
    assertEquals(players, "[uta.cse3310.Player@5f375618, uta.cse3310.Player@1810399e]");

        //checkking to see if game is full, adn ready to start
    assertTrue(g.isReadyToStart());
    assertEquals(g.getCurrentNumberOfPlayers(),"2");

    //check to see if words can be correctlr chosen by players
    assertTrue(g.checkWord(p1.getUsername(), "Pagani")); // these next few words need to be changed if TXT file changes
    g.checkWord(p1.getUsername(), "Pagani"); // josh finds word pagani
    g.checkWord(p2.getUsername(), "Maserati"); // adam finds word Maserati
    g.checkWord(p2.getUsername(), "Aspark"); // adam finds word Aspark

    // retrieve player scores
    String string = String.valueOf(g.getPlayerScores());
    assertEquals(string, "{adam=2, josh=1}"); // check player scores.

    // reset game and kick players out
    g.reset();
    
    //make sure there are no players left in the lobby
    assertEquals(g.getCurrentNumberOfPlayers(),"0");

    }
}
