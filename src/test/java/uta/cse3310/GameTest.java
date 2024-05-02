package uta.cse3310;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uta.cse3310.Game.GameEventListener;

/**
 * Unit test for simple App.
 */
public class GameTest
        extends TestCase {

            class MockGameEventListener implements GameEventListener {
                boolean onGameFinishedCalled = false;
        
                @Override
                public void onGameFinished(String gameId) {
                    onGameFinishedCalled = true;
                }
            }

            
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
    MockGameEventListener mockListener = new MockGameEventListener();

    Game g = new Game("TestLobby", "TestRoom", mockListener); // New test Room
    Player p1 = new Player("josh", null); // new player named josh
    Player p2 = new Player("adam", null); // new player named adam
    
    //adding players to game
    g.addPlayer(p1);
    g.addPlayer(p1);
    g.setPlayer1(p1);
    g.setPlayer2(p2);
    
    //checking to see if game is full, and ready to start
    assertTrue(g.isReadyToStart());
    assertEquals(g.getCurrentNumberOfPlayers(),"2");

    //start game
    g.startGame();

    //check grid generation time
    long time= g.getGridGenerationTime();
    assertEquals(time, 1);
    //insert 3 word into the words placed function
     List<String> wordList = new ArrayList<>();
        for (String word : g.getWordsPlaced().keySet()) {
            // Add each word to the list
            wordList.add(word);
        }
    //calclate word density
    double density = g.calculateWordDensity();
    boolean validDensity = false;
    if (density >= 0.80) validDensity = true;
    assertTrue(validDensity);

    //check if placed words can correctly be traced
    assertTrue(g.isWordCorrect(wordList.get(0)));

    //check to see if words can be correctly chosen by players
    assertTrue(g.checkWord(p1.getUsername(), wordList.get(0)));// test to see if player josh can find first word in grid
    g.checkWord(p1.getUsername(),wordList.get(0)); // josh finds first word
    assertFalse(g.checkWord(p1.getUsername(), wordList.get(0)));
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
