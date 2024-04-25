package uta.cse3310;
import java.util.ArrayList;
import java.util.List;

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
    
        //checkking to see if game is full, adn ready to start
    assertTrue(g.isReadyToStart());
    assertEquals(g.getCurrentNumberOfPlayers(),"2");

    //insert 3 word into the words placed function
     List<String> wordList = new ArrayList<>();
        for (String word : g.getWordsPlaced().keySet()) {
            // Add each word to the list
            wordList.add(word);
        }


    //check to see if words can be correctly chosen by players
    assertTrue(g.checkWord(p1.getUsername(), wordList.get(0)));// test to see if player josh can find first word in grid
    g.checkWord(p1.getUsername(),wordList.get(0)); // josh finds first word
    g.checkWord(p2.getUsername(), wordList.get(1)); // adam finds second word
    g.checkWord(p2.getUsername(), wordList.get(2)); // adam finds third word


    // retrieve player scores
    String string = String.valueOf(g.getPlayerScores());
    assertEquals(string, "{adam=2, josh=1}"); // check player scores.

    // reset game and kick players out
    g.reset();
    
    //make sure there are no players left in the lobby
    assertEquals(g.getCurrentNumberOfPlayers(),"0");

    }
        }