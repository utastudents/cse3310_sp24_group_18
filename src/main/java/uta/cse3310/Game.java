package uta.cse3310;
public class Game {
    private Player player1;
    private Player player2;
    private boolean isFinished;

    public Game(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.isFinished = false;
    }

    // Getters and setters
    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    // Other methods as needed...
}
