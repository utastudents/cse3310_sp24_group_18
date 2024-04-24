package uta.cse3310;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AppTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    public void testApp()
    {
    Game g = new Game("TestLobby", "TestRoom"); // New test Room
    Player p1 = new Player("josh", null); // new player named josh
    Player p2 = new Player("adam", null); // new player named adam

    App a = new App(0);

    a.tryJoinGame("TestLobby", p2);
    a.tryJoinGame("TestLobby", p2);

    }
}